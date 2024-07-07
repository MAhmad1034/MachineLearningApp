package com.example.madprojectml.mlmodels;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.madprojectml.models.BoxWithLabel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.ArrayList;
import java.util.List;


public class ObjectDetectionModel extends ImageHelperActivity {

    private ObjectDetector objectDetector;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions
                        .Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();
        objectDetector = com.google.mlkit.vision.objects.ObjectDetection.getClient(options);
    }


    @Override
    protected  void runClassification(Bitmap bitmap)
    {
        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
        objectDetector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                    @Override
                    public void onSuccess(List<DetectedObject> detectedObjects) {
                        if(!detectedObjects.isEmpty()){
                            StringBuilder builder = new StringBuilder();
                            List<BoxWithLabel> boxes = new ArrayList<>();
                            for(DetectedObject object:detectedObjects){
                                if(!object.getLabels().isEmpty())
                                {

                                    String label = object.getLabels().get(0).getText();
                                    int confidence = (int) (object.getLabels().get(0).getConfidence() * 100);
                                    builder.append(label).append(" : ")
                                            .append(confidence).append("%")
                                            .append("\n");

                                    boxes.add(new BoxWithLabel(object.getBoundingBox(),label));
                                    Log.d("Object Detetion","Object Detected");

                                }
                                else{
                                    builder.append("Unknown").append("\n");
                                }

                            }

                            getOutputTextView().setText(builder.toString());
                            drawDetectionResult(boxes,bitmap);

                        }
                        else{
                            getOutputTextView().setText("Unable to Detect Objects");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}

