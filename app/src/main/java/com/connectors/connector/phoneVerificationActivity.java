package com.connectors.connector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class phoneVerificationActivity extends AppCompatActivity {

    TextView phoneNumberEndingWith;
    private String mVerificationId;
    private EditText inputOTPText;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private FirebaseAuth userAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userAuth = FirebaseAuth.getInstance();
        inputOTPText = findViewById(R.id.inputOTPText);
        progressBar=findViewById(R.id.progressbar);

        phoneNumberEndingWith = findViewById(R.id.phoneNumberEndingWith);
        Intent intent = getIntent();
        String mobile = intent.getStringExtra("Phone Number");
        sendVerificationCode(mobile);

        phoneNumberEndingWith.setText("Enter OTP sent on the number ending with ******" + mobile.substring(9, 13));
    }

    public void finalRegistration(View view) {
        String code = inputOTPText.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            inputOTPText.setError("Enter valid code");
            inputOTPText.requestFocus();
            return;
        }
        verifyVerificationCode(code);
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                progressBar.setVisibility(View.VISIBLE);
                inputOTPText.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(phoneVerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
        }
    };
    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(phoneVerificationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            registerWithEmail();
                        } else {
                            Toast.makeText(phoneVerificationActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void registerWithEmail() {
        Intent intent1 = getIntent();
        String email = intent1.getStringExtra("Email");
        String password = intent1.getStringExtra("Password");
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intentExtra = getIntent();
                            String userId = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("Users").document(userId);
                            Map<String, Object> user = new HashMap<>();
                            user.put("EmailId", intentExtra.getStringExtra("Email"));
                            user.put("Full Name", intentExtra.getStringExtra("First Name") + " " +
                                    intentExtra.getStringExtra("Last Name"));
                            user.put("Mobile Number", intentExtra.getStringExtra("Phone Number"));

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMhhmmssSSyy");
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("BuyyaPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            String uidHolder = simpleDateFormat.format(calendar.getTime());
                            editor.putString("IMPUID", uidHolder);
                            editor.apply();
                            user.put("UID", uidHolder);

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(intentExtra.getStringExtra("First Name") + " " +
                                            intentExtra.getStringExtra("Last Name"))
                                    .build();
                            firebaseUser.updateProfile(userProfileChangeRequest);



                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(phoneVerificationActivity.this, mainDashboard.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(phoneVerificationActivity.this, "fail",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}