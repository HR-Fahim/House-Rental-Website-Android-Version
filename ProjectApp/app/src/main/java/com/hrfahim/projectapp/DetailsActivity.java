package com.hrfahim.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private EditText adsNameEditText;
    private EditText numberEditText;
    private EditText locationEditText;
    private EditText costEditText;
    private Button postButton;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        // Get reference to the database
        databaseRef = firebaseDatabase.getReference("Details");

        // Map UI elements
        adsNameEditText = findViewById(R.id.editTextAdsName);
        numberEditText = findViewById(R.id.editTextNumber);
        locationEditText = findViewById(R.id.editTextLocation);
        costEditText = findViewById(R.id.editTextCost);
        postButton = findViewById(R.id.buttonPost);

        // Set OnClickListener for the post button
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = adsNameEditText.getText().toString().trim();
                String phoneNumber = numberEditText.getText().toString().trim();
                String location = locationEditText.getText().toString().trim();
                String cost = costEditText.getText().toString().trim();

                // Check for empty fields
                if (name.isEmpty() || phoneNumber.isEmpty() || location.isEmpty() || cost.isEmpty()) {
                    Toast.makeText(DetailsActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check for negative cost
                int costValue = Integer.parseInt(cost);
                if (costValue < 0) {
                    Toast.makeText(DetailsActivity.this, "Cost cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }

                String imageName = getIntent().getStringExtra("imageName"); // Get the image name from the previous activity

                if (!name.equals(imageName)) {
                    Toast.makeText(DetailsActivity.this, "Ad name must be the same as the image name", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference adRef = databaseRef.push();
                adRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        DatabaseReference newAdRef = databaseRef.child(name);
                        String adKey = newAdRef.getKey();
                        Map<String, Object> adData = new HashMap<>();
                        adData.put("location", location);
                        adData.put("phone_number", phoneNumber);
                        adData.put("cost", cost);
                        adRef.child(adKey).setValue(adData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(DetailsActivity.this, "Posted ad successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(DetailsActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DetailsActivity.this, "Failed to post ad!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
    }
}

