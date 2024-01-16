package com.soloak.telegramclone.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.soloak.telegramclone.R;

public class VerifyOtpActivity extends AppCompatActivity {

    private PinView pinView;
    private ProgressBar progressBar;
    private String verificationId;
    private String mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Get data from the intent
        Intent intent = getIntent();
        verificationId = intent.getStringExtra("verificationId");
        mobile = intent.getStringExtra("mobile");

        // Initialize views
        pinView = findViewById(R.id.pinview);
        progressBar = findViewById(R.id.progressBar);

        // Set onClickListener for the verify button
        findViewById(R.id.buttonVerify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = pinView.getText().toString().trim();
                if (!code.isEmpty()) {
                    verifyOtp(code);
                } else {
                    Toast.makeText(VerifyOtpActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set onClickListener for the back arrow
        ImageView arrowBack = findViewById(R.id.arrowBack);
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Display mobile number for verification
        TextView textMobile = findViewById(R.id.textMobile);
        textMobile.setText(getString(R.string.sent_code1) + " " + mobile + " " + getString(R.string.sent_code2));
    }

    private void verifyOtp(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = task.getResult().getUser();

                        // Save the login status and phone number in SharedPreferences
                        saveLoginStatus(user.getPhoneNumber());

                        // Now you can start the home activity or perform any other action
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(VerifyOtpActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        // Example: Start HomePageActivity
                        Intent intent = new Intent(VerifyOtpActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(VerifyOtpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void saveLoginStatus(String phoneNumber) {
        // Save the login status and phone number in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userPhoneNumber", phoneNumber);
        editor.apply();
    }

}
