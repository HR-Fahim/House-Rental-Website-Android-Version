package com.hrfahim.projectapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_SELECT_PICTURE = 3;
    private static final int REQUEST_SPEECH_INPUT = 4;

    private Button takePictureButton;
    private Button selectPictureButton;
    private EditText pictureNameTextField;
    private Button setNameButton;
    private Button homePageButton;
    private Button detailsPageButton;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    private boolean isImageSelected = false;
    private boolean detailsButtonClicked = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        takePictureButton = findViewById(R.id.take_picture_button);
        selectPictureButton = findViewById(R.id.select_picture_button);
        pictureNameTextField = findViewById(R.id.picture_name_text_field);
        homePageButton = findViewById(R.id.main_home_page);
        detailsPageButton = findViewById(R.id.details_button);

        homePageButton.setOnClickListener(v -> {
            openNewActivityHome();
        });

        detailsPageButton.setOnClickListener(v -> {
            detailsButtonClicked = true;
            Intent intent = new Intent(CameraActivity.this, DetailsActivity.class);
            String imageName = pictureNameTextField.getText().toString().trim();
            intent.putExtra("imageName", imageName);
            startActivity(intent);
        });

        takePictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            } else if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                dispatchTakePictureIntent();
            }
        });

        selectPictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_SELECT_PICTURE);
            } else {
                openGallery();
            }
        });

        Button uploadButton = findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> {
            String imageFileName = pictureNameTextField.getText().toString().trim();
            if (imageFileName.isEmpty()) {
                Toast.makeText(this, "Please enter an image file name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isImageSelected) {
                Toast.makeText(this, "Please capture or select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!detailsButtonClicked) {
                Toast.makeText(this, "Please add details by clicking the 'Details' button", Toast.LENGTH_SHORT).show();
                return;
            }

            // Proceed with uploading the image
            // ...
        });
    }

    private void openNewActivityHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    /*private void openNewActivityDetails() {
        Intent intent = new Intent(this, DetailsActivity.class);
        startActivity(intent);
    }*/

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    // Handle captured image bitmap
                    isImageSelected = true;
                    uploadImage(imageBitmap);
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_SELECT_PICTURE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    // Handle selected image bitmap
                    isImageSelected = true;
                    uploadImage(selectedImageBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to retrieve selected image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(Bitmap imageBitmap) {
        if (!isImageSelected) {
            Toast.makeText(this, "Please capture or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert image to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        String imageFileName = pictureNameTextField.getText().toString().trim();

        // Set the storage reference path (e.g., "images/picture.jpg")
        String storagePath = imageFileName;
        StorageReference imageRef = storageRef.child(storagePath);

        // Check if the image already exists in Firebase Storage
        imageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            // Image with the same name already exists, show an error message
            Toast.makeText(this, "An image with the same name already exists", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            // Image does not exist, proceed with uploading

            // Set content type to JPEG
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();

            // Upload the image byte array to Firebase Storage
            UploadTask uploadTask = imageRef.putBytes(imageData, metadata);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image upload success
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

                // Retrieve the download URL for the uploaded image
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Display the preview using the image URL
                    String imageUrl = uri.toString();
                    // Use the image URL for displaying the preview (e.g., set it to an ImageView)
                }).addOnFailureListener(e -> {
                    // Failed to retrieve the download URL
                    Toast.makeText(this, "Failed to retrieve image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                // Image upload failed
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission denied. Cannot take picture", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SELECT_PICTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select picture", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission denied. Cannot capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

