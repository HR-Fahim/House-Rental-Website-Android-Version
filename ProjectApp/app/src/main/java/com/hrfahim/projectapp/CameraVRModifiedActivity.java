package com.hrfahim.projectapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import org.opencv.android.OpenCVLoader;

public class CameraVRModifiedActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private VrPanoramaView panoramaView;
    private boolean vrViewInitialized = false;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            // You might want to display a message or take other actions
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_vr_modified);

        panoramaView = findViewById(R.id.panoramaView);

        panoramaView.setEventListener(new VrPanoramaViewEventListener());

        if (ContextCompat.checkSelfPermission(CameraVRModifiedActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraVRModifiedActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            initializeVrView();
        }
    }

    private void initializeVrView() {
        VrPanoramaView.Options options = new VrPanoramaView.Options();
        options.inputType = VrPanoramaView.Options.TYPE_MONO;

        // Load the VR panorama image from a resource file
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.panorama_image);
        panoramaView.loadImageFromBitmap(bitmap, options);

        vrViewInitialized = true;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (detectRoom(photo)) {
                savePhotoToGallery(photo);
            } else {
                Toast.makeText(this, "Room not detected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean detectRoom(Bitmap photo) {
        // Use OpenCV to detect a room in the photo
        // Implement your room detection logic using OpenCV

        // Placeholder implementation
        // Always returns true
        return true;
    }

    private void savePhotoToGallery(Bitmap photo) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                photo,
                "Room Photo",
                "Photo of a detected room"
        );

        if (savedImageURL != null) {
            Uri savedImageURI = Uri.parse(savedImageURL);
            Toast.makeText(this, "Photo saved to gallery.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save photo.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeVrView();
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vrViewInitialized) {
            panoramaView.resumeRendering();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vrViewInitialized) {
            panoramaView.pauseRendering();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vrViewInitialized) {
            panoramaView.shutdown();
        }
    }

    private class VrPanoramaViewEventListener extends VrPanoramaEventListener {
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
            // The VR panorama image is successfully loaded
        }

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
            // Handle the VR panorama image loading error
        }
    }
}


