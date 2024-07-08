package com.example.madprojectml.modules;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madprojectml.Adapters.ImageAdapter;
import com.example.madprojectml.ImageSelectionActivity;
import com.example.madprojectml.R;
import com.example.madprojectml.mlmodels.ObjectDetectionModel;
import com.example.madprojectml.models.ImageData;
import com.example.madprojectml.models.ImageUpload;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ObjectDetection extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private List<ImageData> imageDataList;
    private ImageAdapter imageAdapter;
    private FloatingActionButton fabAddImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        imageDataList = new ArrayList<>();
        fabAddImage = findViewById(R.id.fabAddImage);

        databaseReference = FirebaseDatabase.getInstance().getReference("object_detection_images");

        fabAddImage.setOnClickListener(v -> openImageSelectionForm());

        initializeRecyclerView();
        loadImagesFromFirebase();
    }

    private void openImageSelectionForm() {
        Intent intent = new Intent(this, ObjectDetectionModel.class);
        intent.putExtra("MODULE_TYPE", "ObjectDetection");
        startActivity(intent);
    }

    private void loadImagesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageDataList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ImageUpload imageUpload = postSnapshot.getValue(ImageUpload.class);
                    if (imageUpload != null) {
                        String imageUrl = imageUpload.getImageUrl();
                        String result = imageUpload.getResult();
                        imageDataList.add(new ImageData(imageUrl, result));
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ObjectDetection.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this, imageDataList, "ObjectDetection");
        recyclerView.setAdapter(imageAdapter);
    }
}
