package com.example.madprojectml.mlmodels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.madprojectml.Home;
import com.example.madprojectml.R;
import com.example.madprojectml.ml.AutoModel1;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RealTimeObjectDetection extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 101;
    AppCompatButton btnCancel;
    private List<String> labels;
    private final List<Integer> colors = Arrays.asList(
            Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY, Color.BLACK,
            Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED);
    private final Paint paint = new Paint();
    private ImageProcessor imageProcessor;
    private Bitmap bitmap;
    private ImageView imageView;
    private CameraDevice cameraDevice;
    private Handler handler;
    private CameraManager cameraManager;
    private TextureView textureView;
    private AutoModel1 model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_object_detection);

        initializeComponents();
        requestCameraPermission();
    }

    private void initializeComponents() {
        try {
            labels = FileUtil.loadLabels(this, "labels.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
                .build();

        try {
            model = AutoModel1.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HandlerThread handlerThread = new HandlerThread("videoThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        imageView = findViewById(R.id.iv);
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v->{
            startActivity(new Intent(this, Home.class));
            finish();
        });

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            bitmap = textureView.getBitmap();
            if (bitmap != null) {
                detectObjects(bitmap);
            }
        }
    };

    private void detectObjects(Bitmap bitmap) {
        TensorImage image = TensorImage.fromBitmap(bitmap);
        image = imageProcessor.process(image);

        AutoModel1.Outputs outputs = model.process(image);
        float[] locations = outputs.getLocationsAsTensorBuffer().getFloatArray();
        float[] classes = outputs.getClassesAsTensorBuffer().getFloatArray();
        float[] scores = outputs.getScoresAsTensorBuffer().getFloatArray();

        drawDetectionResult(bitmap, locations, classes, scores);
    }

    private void drawDetectionResult(Bitmap bitmap, float[] locations, float[] classes, float[] scores) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        int h = mutableBitmap.getHeight();
        int w = mutableBitmap.getWidth();
        paint.setTextSize(h / 15f);
        paint.setStrokeWidth(h / 85f);

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > 0.65) {
                paint.setColor(colors.get(i % colors.size()));
                paint.setStyle(Paint.Style.STROKE);
                RectF rectF = new RectF(
                        locations[i * 4 + 1] * w,
                        locations[i * 4] * h,
                        locations[i * 4 + 3] * w,
                        locations[i * 4 + 2] * h);
                canvas.drawRect(rectF, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(labels.get((int) classes[i]) + " " + scores[i],
                        rectF.left, rectF.top, paint);
            }
        }

        runOnUiThread(() -> imageView.setImageBitmap(mutableBitmap));
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            cameraManager.openCamera(cameraManager.getCameraIdList()[0], cameraStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            Surface surface = new Surface(surfaceTexture);

            final CaptureRequest.Builder captureRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                    }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textureView.setSurfaceTextureListener(surfaceTextureListener);
            } else {
                // Handle permission denied
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (model != null) {
            model.close();
        }
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }
}