package com.example.madprojectml;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madprojectml.Adapters.DashboardAdapter;
import com.example.madprojectml.models.DashboardItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DashboardAdapter adapter;
    private List<DashboardItem> dashboardItems;
    private ImageView ivLogout;
    private FirebaseAuth mAuth;

    //ads
    AdView ad;
    InterstitialAd mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recycler_view);
        ivLogout = findViewById(R.id.ivLogout);

        mAuth = FirebaseAuth.getInstance();

        dashboardItems = new ArrayList<>();
        // Add your dashboard items here
        dashboardItems.add(new DashboardItem(R.drawable.ic_image_classification, "Image Classification"));
        dashboardItems.add(new DashboardItem(R.drawable.ic_flower_classification, "Flower Classification"));
        dashboardItems.add(new DashboardItem(R.drawable.ic_object_dtetection, "Object Detection"));
        dashboardItems.add(new DashboardItem(R.drawable.ic_face_detection, "Face Detection"));
        dashboardItems.add(new DashboardItem(R.drawable.ic_face_detection, "Real Time Object Detection"));

        adapter = new DashboardAdapter(this, dashboardItems);
        recyclerView.setAdapter(adapter);

        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        //showAds();

    }

    private void showAds()
    {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });

        AdRequest request1 = new AdRequest.Builder().build();

//        InterstitialAd.load(Home.this, "ca-app-pub-3940256099942544/1033173712",
//                request1, new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        super.onAdFailedToLoad(loadAdError);
//
//                    }
//
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        super.onAdLoaded(interstitialAd);
//                        mAd = interstitialAd;
//                        mAd.show(Home.this);
//                        mAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdClicked() {
//                                super.onAdClicked();
//                                Toast.makeText(Home.this, "Interstitial ad clicked", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                super.onAdDismissedFullScreenContent();
//                                Toast.makeText(Home.this, "bye bye", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//                    }
//                });
//
//        ad = findViewById(R.id.ad);
//        AdRequest request = new AdRequest.Builder().build();
//        ad.loadAd(request);
//
//        ad.setAdListener(new AdListener() {
//            @Override
//            public void onAdClicked() {
//                super.onAdClicked();
//                Toast.makeText(Home.this, "adClicked", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAdClosed() {
//                super.onAdClosed();
//                Toast.makeText(Home.this, "adClosed", Toast.LENGTH_SHORT).show();
//            }
//        });
    }



    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(Home.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}