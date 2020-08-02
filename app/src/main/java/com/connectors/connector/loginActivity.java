package com.connectors.connector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.connectors.connector.helperClass.countryData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class loginActivity extends AppCompatActivity {

    EditText emailText, passwordText;
    private FirebaseAuth mAuth;
    private static final String TAG = "loginActivity";
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";
    Button button5, phoneLogin;
    private String mVerificationId;
    TextView forgetpass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog,progressDialog1;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                // bottomSheetOTPText.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(loginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        phoneLogin = findViewById(R.id.phoneLogin);
        emailText=findViewById(R.id.emailText);
        passwordText=findViewById(R.id.passwordText);
        button5=findViewById(R.id.button5);
        forgetpass=findViewById(R.id.forgetPass);

        emailText.addTextChangedListener(new gTextWatcher(emailText));
        passwordText.addTextChangedListener(new gTextWatcher(passwordText));

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validateEmail()) {
                    return;
                } else if (!validatePassword()) {
                    return;
                } else {
                    firebaseLogin();
                }
                progressDialog1.show();
            }
        });

        forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog1 = new ProgressDialog(this);
        progressDialog1.setTitle("Please Wait");
        progressDialog1.setMessage("Logging In");
        progressDialog1.setCanceledOnTouchOutside(true);

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(loginActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog);
                bottomSheetDialog.show();

                TextInputLayout bottomSheetNumberInput = bottomSheetDialog.findViewById(R.id.bottomSheetNumberInput);
                final TextInputLayout bottomSheetOTPInput = bottomSheetDialog.findViewById(R.id.bottonSheetOTPInput);
                final EditText bottomSheetPhoneNumber = bottomSheetDialog.findViewById(R.id.bottomSheetPhoneNumber);
                final EditText bottomSheetOTPText = bottomSheetDialog.findViewById(R.id.bottomSheetOTPText);
                Button button8 = bottomSheetDialog.findViewById(R.id.button8);

                final Spinner registerSpinner = bottomSheetDialog.findViewById(R.id.registerSpinner);
                registerSpinner.setAdapter(new ArrayAdapter<String>(loginActivity.this, android.R.layout.
                        simple_spinner_dropdown_item, countryData.countryNames));

                bottomSheetOTPInput.setVisibility(View.GONE);

                button8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String code = countryData.countryAreaCodes[registerSpinner.getSelectedItemPosition()];
                        String number = bottomSheetPhoneNumber.getText().toString();
                        String phoneNumberFinal = "+" + code + number;
                        bottomSheetOTPInput.setVisibility(View.VISIBLE);
                        sendVerificationCode(phoneNumberFinal);

                        String code1 = bottomSheetOTPText.getText().toString().trim();
                        if (code1.isEmpty() || code1.length() < 6) {
                            bottomSheetOTPText.requestFocus();
                            progressDialog1.dismiss();
                            return;
                        } else {
                            sendVerificationCode(phoneNumberFinal);
                        }
                        progressDialog1.show();
                    }
                });
            }
        });
    }

    private void sendVerificationCode(String phoneNumberFinal) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberFinal,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("BuyyaPref", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("IMPUID", documentSnapshot.getString("UID"));
                                        editor.apply();
                                        Intent intent = new Intent(loginActivity.this, mainDashboard.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {

                                    }
                                }
                            });


                            progressDialog1.dismiss();
                        } else {
                            Toast.makeText(loginActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            progressDialog1.dismiss();
                        }
                    }
                });
    }




    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Registered Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending Email");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(loginActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(loginActivity.this, "Failed!!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(loginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    public void firebaseLogin() {

        mAuth.signInWithEmailAndPassword(emailText.getText().toString().trim(), passwordText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("BuyyaPref", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("IMPUID", documentSnapshot.getString("UID"));
                                        editor.apply();
                                        Toast.makeText(loginActivity.this, "Welcome Back", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(loginActivity.this, mainDashboard.class);
                                        startActivity(intent);
                                        finish();

                                    } else {

                                    }
                                }
                            });

                            progressDialog1.dismiss();

                        } else {
                            Toast.makeText(loginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog1.dismiss();

                        }
                    }
                });
    }

    public boolean validateEmail() {

        String email = emailText.getText().toString();

        if (!email.trim().matches(emailPattern) && email.length() > 0) {

            emailText.setError("Please enter correct id");
            emailText.requestFocus();
            return false;
        } else if (email.trim().isEmpty()) {
            emailText.setError("Please enter id");
            emailText.requestFocus();
            return false;

        } else {
            return true;
        }

    }

    public boolean validatePassword() {
        String password = passwordText.getText().toString().trim();
        if (password.isEmpty()) {
            passwordText.setError("Enter valid password");
            passwordText.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void moveToRegisterActivity(View view) {
        Intent intent=new Intent(loginActivity.this,registerActivity.class);
        startActivity(intent);
    }


    private class gTextWatcher implements TextWatcher {

        private View view;

        public gTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {

                case R.id.emailText:
                    validateEmail();
                    break;

                case R.id.passwordText:
                    validatePassword();
                    break;
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}