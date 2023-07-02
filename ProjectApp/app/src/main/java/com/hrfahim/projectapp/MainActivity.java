package com.hrfahim.projectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hrfahim.projectapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE_EMAIL = 1;
    private static final int SPEECH_REQUEST_CODE_PASSWORD = 2;

    private EditText emailEditText, passwordEditText;
    private Button voiceEmailInputButton, voicePasswordInputButton, loginButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        voiceEmailInputButton = findViewById(R.id.voiceEmailInputButton);
        voicePasswordInputButton = findViewById(R.id.voicePasswordInputButton);
        loginButton = findViewById(R.id.loginButton);

        voiceEmailInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition(SPEECH_REQUEST_CODE_EMAIL);
            }
        });

        voicePasswordInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition(SPEECH_REQUEST_CODE_PASSWORD);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                signInWithEmailAndPassword(email, password);
            }
        });
    }

    private void startSpeechRecognition(int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (requestCode == SPEECH_REQUEST_CODE_EMAIL) {
            startActivityForResult(intent, SPEECH_REQUEST_CODE_EMAIL);
        } else if (requestCode == SPEECH_REQUEST_CODE_PASSWORD) {
            startActivityForResult(intent, SPEECH_REQUEST_CODE_PASSWORD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String spokenText = result.get(0);
                parseSpokenText(requestCode, spokenText);
            }
        }
    }

    private void parseSpokenText(int requestCode, String spokenText) {
        switch (requestCode) {
            case SPEECH_REQUEST_CODE_EMAIL:
                emailEditText.setText(spokenText.trim());
                break;
            case SPEECH_REQUEST_CODE_PASSWORD:
                passwordEditText.setText(spokenText.trim());
                break;
            default:
                Toast.makeText(this, "Invalid request code", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithEmailAndPassword(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Open new activity or perform further actions
                            openNewActivity();
                            Toast.makeText(this, "Authentication succeeded. Welcome, " + user.getEmail() + "!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openNewActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}