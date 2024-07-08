package com.example.madprojectml.modules;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.madprojectml.R;
import com.example.madprojectml.mlmodels.RealTimeObjectDetection;

public class linkerRealTimeObjectDetection extends AppCompatActivity {
    AppCompatButton btnRealTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_linker_real_time_object_detection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnRealTime = findViewById(R.id.btnRealTime);
        btnRealTime.setOnClickListener(v->{
            startActivity(new Intent(this, RealTimeObjectDetection.class));
            finish();
        });
    }
}