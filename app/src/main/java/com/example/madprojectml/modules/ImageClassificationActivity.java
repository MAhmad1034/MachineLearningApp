package com.example.madprojectml.modules;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madprojectml.Adapters.ImageAdapter;
import com.example.madprojectml.R;
import com.example.madprojectml.mlmodels.ImageClassificationModel;
import com.example.madprojectml.models.ImageUpload;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ImageClassificationActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private List<ImageUpload> imageDataList; // Change to List<ImageUpload>
    private ImageAdapter imageAdapter;
    private FloatingActionButton fabAddImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_classification);

        imageDataList = new ArrayList<>();
        fabAddImage = findViewById(R.id.fabAddImage);

        databaseReference = FirebaseDatabase.getInstance().getReference("image_classification_images");

        fabAddImage.setOnClickListener(v -> openImageSelectionForm());

        initializeRecyclerView();
        loadImagesFromFirebase();
    }

    private void openImageSelectionForm() {
        Intent intent = new Intent(this, ImageClassificationModel.class);
        intent.putExtra("MODULE_TYPE", "ImageClassification");
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
                        imageUpload.setKey(postSnapshot.getKey()); // Set the key
                        imageDataList.add(imageUpload);
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImageClassificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageAdapter = new ImageAdapter(this, imageDataList, "ImageClassification");
        recyclerView.setAdapter(imageAdapter);
    }
}
