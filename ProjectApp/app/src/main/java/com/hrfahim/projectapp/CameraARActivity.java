package com.hrfahim.projectapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.List;

public class CameraARActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private VideoCapture videoCapture;
    private boolean isProcessing = false;

    // Constants for corner detection
    private static final int MAX_CORNERS = 100;
    private static final double QUALITY_LEVEL = 0.01;
    private static final double MIN_DISTANCE = 10;
    private static final int BLOCK_SIZE = 3;
    private static final boolean USE_HARRIS_DETECTOR = false;
    private static final double K = 0.04;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_ar);

        surfaceView = findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                checkCameraPermission();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            initializeCamera();
        }
    }

    private void initializeCamera() {
        videoCapture = new VideoCapture();
        videoCapture.open(0); // 0 represents the rear camera, change it if necessary

        if (videoCapture.isOpened()) {
            Toast.makeText(this, "Camera opened successfully", Toast.LENGTH_SHORT).show();
            startProcessing();
        } else {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void startProcessing() {
        if (!isProcessing) {
            isProcessing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isProcessing) {
                        Mat frame = new Mat();
                        if (videoCapture.read(frame)) {
                            processFrame(frame);
                        }
                        frame.release();
                    }
                }
            }).start();
        }
    }

    private void processFrame(Mat frame) {
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

        MatOfPoint2f corners = new MatOfPoint2f();
        Imgproc.goodFeaturesToTrack(grayFrame, corners, MAX_CORNERS, QUALITY_LEVEL, MIN_DISTANCE, new Mat(), BLOCK_SIZE, USE_HARRIS_DETECTOR, K);

        List<Point> cornerPoints = corners.toList();
        for (Point cornerPoint : cornerPoints) {
            Imgproc.circle(frame, cornerPoint, 5, new Scalar(255, 0, 0), -1);
        }

        grayFrame.release();
        corners.release();

        Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bitmap);

        // Display the bitmap on the SurfaceView
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void stopProcessing() {
        isProcessing = false;
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startProcessing();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopProcessing();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopProcessing();
    }
}


