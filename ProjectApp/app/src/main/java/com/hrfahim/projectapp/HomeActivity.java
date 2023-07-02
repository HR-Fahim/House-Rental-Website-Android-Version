package com.hrfahim.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private Button voiceSearchButton;
    private LinearLayout adsLinearLayout;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Button cameraVRButton;
    private Button cameraARButton;
    private static final int REQUEST_CODE_VOICE_SEARCH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        voiceSearchButton = findViewById(R.id.voiceSearchButton);
        adsLinearLayout = findViewById(R.id.adsLinearLayout);

        cameraVRButton = findViewById(R.id.camera_vr_button);
        cameraARButton = findViewById(R.id.camera_ar_button);

        cameraVRButton.setOnClickListener(v -> {

            Intent intent = new Intent(HomeActivity.this, CameraVRActivity.class);
            startActivity(intent);
        });

        cameraARButton.setOnClickListener(v -> {

            Intent intent = new Intent(HomeActivity.this, CameraARActivity.class);
            startActivity(intent);
        });
        cameraVRButton.setOnClickListener(v -> {

            Intent intent = new Intent(HomeActivity.this, CameraVRActivity.class);
            startActivity(intent);
        });

        FirebaseApp.initializeApp(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Details");
        storageReference = FirebaseStorage.getInstance().getReference();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString().toLowerCase();
                getData(searchQuery);
            }
        });

        voiceSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleVoiceSearch();
            }
        });

        Button postAdButton = findViewById(R.id.postAdButton);
        postAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Post Ad Now!" button click
                openNewActivity();
            }
        });
    }

    private void openNewActivity() {
        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleVoiceSearch() {
        // Start speech recognition intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your search query");
        startActivityForResult(intent, REQUEST_CODE_VOICE_SEARCH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VOICE_SEARCH && resultCode == RESULT_OK && data != null) {
            // Retrieve the recognized speech text
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                String searchQuery = results.get(0);
                searchEditText.setText(searchQuery);
                getData(searchQuery);
            }
        }
    }

    public class Ad {
        private String name;
        private String location;
        private String phoneNumber;
        private String cost;

        public Ad() {
            // Default constructor required for Firebase
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getCost() {
            return cost;
        }

        public void setCost(String cost) {
            this.cost = cost;
        }
    }

    private void getData(final String searchQuery) {
        databaseReference.orderByKey().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                List<Ad> ads = new ArrayList<>();
                for (DataSnapshot adSnapshot : dataSnapshot.getChildren()) {
                    String adName = adSnapshot.getChildren().iterator().next().getKey();
                    //DataSnapshot adDataSnapshot = adSnapshot.child(adName);
                    DataSnapshot adDataSnapshot = adSnapshot;
                    Ad ad = new Ad();
                    ad.setName(adName);
                    ad.setLocation(adDataSnapshot.child("location").getValue(String.class));
                    ad.setPhoneNumber(adDataSnapshot.child("phone_number").getValue(String.class));
                    ad.setCost(adDataSnapshot.child("cost").getValue(String.class));
                    ads.add(ad);
                }
                displayAds(ads, searchQuery);
            }
        });
    }

    private void displayAds(List<Ad> ads, String searchQuery) {
        adsLinearLayout.removeAllViews();
        int count = 1;
        for (Ad ad : ads) {
            if (searchQuery.isEmpty() || ad.getName().toLowerCase().contains(searchQuery)) {
                LinearLayout adLinearLayout = new LinearLayout(this);
                adLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                adLinearLayout.setPadding(30, 30, 30, 30);

                TextView numberTextView = new TextView(this);
                numberTextView.setText(count + ". ");
                adLinearLayout.addView(numberTextView);

                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                imageView.setLayoutParams(params);
                adLinearLayout.addView(imageView);

                TextView nameTextView = new TextView(this);
                nameTextView.setText(ad.getName());
                adLinearLayout.addView(nameTextView);

                TextView locationTextView = new TextView(this);
                locationTextView.setText(ad.getLocation());
                adLinearLayout.addView(locationTextView);

                TextView phoneNumberTextView = new TextView(this);
                phoneNumberTextView.setText(ad.getPhoneNumber());
                adLinearLayout.addView(phoneNumberTextView);

                TextView costTextView = new TextView(this);
                costTextView.setText(ad.getCost());
                adLinearLayout.addView(costTextView);

                adsLinearLayout.addView(adLinearLayout);

                StorageReference imageRef = storageReference.child(ad.getName());
                imageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String contentType = storageMetadata.getContentType();
                    imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                        String encodedData = Base64.encodeToString(bytes, Base64.DEFAULT);
                        String imageDataUri = "data:" + contentType + ";base64," + encodedData;

                        RequestOptions requestOptions = new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL);

                        Glide.with(this)
                                .load(imageDataUri)
                                .apply(requestOptions)
                                .into(imageView);

                    }).addOnFailureListener(exception -> {
                        // Handle image download failure
                        // You can set a placeholder image or show an error message in the ImageView
                        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    });
                }).addOnFailureListener(exception -> {
                    // Handle metadata retrieval failure
                    // You can set a placeholder image or show an error message in the ImageView
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                });

                count++;
            }
        }
    }
}



