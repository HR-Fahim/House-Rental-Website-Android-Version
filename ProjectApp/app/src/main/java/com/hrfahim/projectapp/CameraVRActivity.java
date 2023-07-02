package com.hrfahim.projectapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

public class CameraVRActivity extends AppCompatActivity implements SurfaceHolder.Callback, LocationListener {

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LocationManager locationManager;
    private Button cameraButton, mapButton;
    private Location selectedLocation;

    private final int REQUEST_CAMERA_PERMISSION = 100;
    private final int REQUEST_LOCATION_PERMISSION = 101;
    private final int REQUEST_MAP_LOCATION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_vr);

        surfaceView = findViewById(R.id.surfaceView);
        cameraButton = findViewById(R.id.cameraButton);
        mapButton = findViewById(R.id.mapButton);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationPermission();
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (camera == null) {
                try {
                    camera = Camera.open();
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    camera.setDisplayOrientation(90);
                    camera.setPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            // Calculate the direction from the current location to the selected location
                            if (selectedLocation != null) {
                                // Get the current location from the LocationManager
                                if (ActivityCompat.checkSelfPermission(CameraVRActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (currentLocation != null) {
                                        float direction = currentLocation.bearingTo(selectedLocation);

                                        // Use the 'direction' value to display an overlay on the camera preview indicating the direction
                                        // You can use custom graphics or annotations to display the direction
                                        drawDirectionOverlay(direction);

                                        // Calculate the coordinates for the virtual text 5 meters away from the current camera location
                                        LatLng currentCameraLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                        LatLng virtualTextLatLng = calculateLatLngFromDistanceAndDirection(currentCameraLatLng, 5, direction);
                                        // Show the virtual text or perform any desired action with the virtual text coordinates
                                    }
                                } else {
                                    requestLocationPermission();
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            requestCameraPermission();
        }
    }

    private void drawDirectionOverlay(float direction) {
        // Convert the direction to degrees for rotation
        float rotation = -direction;

        // Create a matrix for rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        // Create a bitmap for the overlay
        Bitmap overlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.direction_arrow_new);

        // Apply rotation to the overlay bitmap
        Bitmap rotatedBitmap = Bitmap.createBitmap(overlayBitmap, 0, 0, overlayBitmap.getWidth(), overlayBitmap.getHeight(), matrix, true);

        // Display the rotated bitmap on the camera preview
        // You can use a custom view or overlay to display the bitmap on the camera preview surface
        // For example, you can create a custom ImageView and set the rotated bitmap as its image
        // imageView.setImageBitmap(rotatedBitmap);
    }

    private LatLng calculateLatLngFromDistanceAndDirection(LatLng startPoint, double distance, float direction) {
        double earthRadius = 6371000; // in meters
        double angularDistance = distance / earthRadius;

        double startLatitudeRad = Math.toRadians(startPoint.latitude);
        double startLongitudeRad = Math.toRadians(startPoint.longitude);
        double startCosLat = Math.cos(startLatitudeRad);
        double startSinLat = Math.sin(startLatitudeRad);

        double latitudeRad = Math.asin(startSinLat * Math.cos(angularDistance) +
                startCosLat * Math.sin(angularDistance) * Math.cos(Math.toRadians(direction)));
        double longitudeRad = startLongitudeRad + Math.atan2(Math.sin(Math.toRadians(direction)) *
                        Math.sin(angularDistance) * startCosLat,
                Math.cos(angularDistance) - startSinLat * Math.sin(latitudeRad));

        double latitude = Math.toDegrees(latitudeRad);
        double longitude = Math.toDegrees(longitudeRad);

        return new LatLng(latitude, longitude);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    private void openMap() {
        Intent intent = new Intent(CameraVRActivity.this, MapActivity.class);
        startActivityForResult(intent, REQUEST_MAP_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            selectedLocation = new Location("");
            selectedLocation.setLatitude(latitude);
            selectedLocation.setLongitude(longitude);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Request the camera permission now
                requestCameraPermission();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Handle location updates if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}


