package com.connectors.connector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class splashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity );
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(splashActivity.this, loginActivity.class);
                    splashActivity.this.startActivity(mainIntent);
                    splashActivity.this.finish();
                }
            }, 3000);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intentFirst = new Intent(splashActivity.this, mainDashboard.class);
                    intentFirst.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentFirst);
                }
            }, 3000);

        }
    }
}