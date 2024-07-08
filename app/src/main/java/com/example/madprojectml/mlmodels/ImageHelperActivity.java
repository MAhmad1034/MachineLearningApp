package com.example.madprojectml.mlmodels;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;

import com.example.madprojectml.R;
import com.example.madprojectml.models.BoxWithLabel;
import com.example.madprojectml.models.ImageUpload;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ImageHelperActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 1000;
    private static final int REQUEST_CAPTURE_IMAGE = 1001;
    private ImageView ivInput;
    private TextView tvOutput;
    private AppCompatButton btnPickImage, btnOpenCamera, btnSave;


    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String moduleType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        Intent intent = getIntent();
        moduleType = intent.getStringExtra("MODULE_TYPE");

        switch (moduleType) {
            case "ImageClassification":
                storageReference = FirebaseStorage.getInstance().getReference("image_classification_images");
                databaseReference = FirebaseDatabase.getInstance().getReference("image_classification_images");
                break;
            case "FlowerClassification":
                storageReference = FirebaseStorage.getInstance().getReference("flower_classification_images");
                databaseReference = FirebaseDatabase.getInstance().getReference("flower_classification_images");
                break;
            case "ObjectDetection":
                storageReference = FirebaseStorage.getInstance().getReference("object_detection_images");
                databaseReference = FirebaseDatabase.getInstance().getReference("object_detection_images");
                break;
            case "FaceDetection":
                storageReference = FirebaseStorage.getInstance().getReference("face_detection_images");
                databaseReference = FirebaseDatabase.getInstance().getReference("face_detection_images");
                break;
            default:
                throw new IllegalArgumentException("Invalid module type");
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAPTURE_IMAGE);}


        init();

        if (btnPickImage != null) {
            btnPickImage.setOnClickListener(this::pickImage);
        }

        if (btnOpenCamera != null) {
            btnOpenCamera.setOnClickListener(v->
            {if (isCameraAvailable()) {
                        onStartCamera(v);
            } else {
                        Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
                    }

            });
        }
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (tvOutput == null) {
                    Toast.makeText(ImageHelperActivity.this, "Couldn't find the image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage(tvOutput.getText().toString());
                    Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void init() {
        ivInput = findViewById(R.id.ivInput);
        tvOutput = findViewById(R.id.tvOutput);
        btnPickImage = findViewById(R.id.buttonSelectImage);
        btnOpenCamera = findViewById(R.id.buttonOpenCamera);
        btnSave = findViewById(R.id.buttonSave);

    }

    private void pickImage(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
    }
    private boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void onStartCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.madprojectml.provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Bitmap bitmap = loadFromUri(imageUri);
            ivInput.setImageURI(imageUri);
            runClassification(bitmap);
        }
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = loadFromUri(imageUri);
            ivInput.setImageURI(imageUri);
            runClassification(imageBitmap);
        }
    }


    protected Bitmap loadFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(ImageHelperActivity.class.getSimpleName(), "Grant result for " + permissions[0] + " is " + grantResults[0]);
    }

    protected void runClassification(Bitmap bitmap) {
        // This method should be overridden by subclasses
    }

    protected TextView getOutputTextView() {
        return tvOutput;
    }

    protected ImageView getInputImageView() {
        return ivInput;
    }

    protected void drawDetectionResult(List<BoxWithLabel> boxes, Bitmap bitmap) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);

        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);

        for (BoxWithLabel box : boxes) {
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.rect, pen);

            Rect tagSize = new Rect(0, 0, 0, 0);
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.YELLOW);
            pen.setStrokeWidth(2F);

            pen.setTextSize(96F);
            pen.getTextBounds(box.label, 0, box.label.length(), tagSize);
            float fontSize = pen.getTextSize() * box.rect.width() / tagSize.width();

            if (fontSize < pen.getTextSize()) pen.setTextSize(fontSize);

            float margin = (box.rect.width() - tagSize.width()) / 2.0F;
            if (margin < 0F) margin = 0F;
            canvas.drawText(box.label, box.rect.left + margin, box.rect.top + tagSize.height(), pen);
        }

        ivInput.setImageBitmap(outputBitmap);
    }

    private void uploadImage(String result) {
        if (imageUri != null) {

            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                ImageUpload imageUpload = new ImageUpload(uri.toString(), result);
                String uploadId = databaseReference.push().getKey();
                databaseReference.child(uploadId).setValue(imageUpload);
                Toast.makeText(ImageHelperActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            })).addOnFailureListener(e -> Toast.makeText(ImageHelperActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    private Uri getImageUri(Bitmap bitmap) {
        File tempDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        tempDir = new File(tempDir.getAbsolutePath() + "/temp/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File tempFile = new File(tempDir, UUID.randomUUID() + ".jpg");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            tempFile.createNewFile();
            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
