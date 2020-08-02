package com.connectors.connector;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.connectors.connector.helperClass.countryData;

public class registerActivity extends AppCompatActivity {

    private EditText registerEmailText, registerFirstNameText, registerLastNameText,
            registerPasswordText, registerRePasswordText, registerPhoneText;
    private Button button6;
    private Spinner registerSpinner;
    ProgressDialog dialog;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$";
    private boolean passMatch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmailText = findViewById(R.id.registerEmailText);
        registerFirstNameText = findViewById(R.id.registerFirstNameText);
        registerLastNameText = findViewById(R.id.registerLastNameText);
        registerPasswordText = findViewById(R.id.registerPasswordText);
        registerRePasswordText = findViewById(R.id.registerRePasswordText);
        registerPhoneText = findViewById(R.id.registerPhoneText);
        button6 = findViewById(R.id.button6);
        registerSpinner = findViewById(R.id.registerSpinner);

        registerEmailText.addTextChangedListener(new genTextWatcher(registerEmailText));
        registerPasswordText.addTextChangedListener(new genTextWatcher(registerPasswordText));
        registerRePasswordText.addTextChangedListener(validateTextWatcher);
        registerPhoneText.addTextChangedListener(new genTextWatcher(registerPhoneText));
        dialog= new ProgressDialog(this);
        dialog.setTitle("Please Wait");
        dialog.setMessage("Registering");
        dialog.setCanceledOnTouchOutside(true);

        registerSpinner.setAdapter(new ArrayAdapter<String>(registerActivity.this, android.R.layout.
                simple_spinner_dropdown_item, countryData.countryNames));

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateRegEmail()) {
                    return;
                } else if (!validateFirstName()) {
                    return;
                } else if (!validateLastName()) {
                    return;
                } else if (!validateRegPassword()) {
                    return;
                } else if (!validateConfirmPassword()) {
                    return;
                } else if (!validatePhoneNum()) {
                    return;
                } else {
                    onRegistrationRequest();
                }
                dialog.show();
            }
        });

    }

    public void onRegistrationRequest() {
        if (registerPasswordText.getText().toString().equals(registerRePasswordText.getText().toString())) {
            if (registerPhoneText.length() == 10) {
                Intent intent = new Intent(registerActivity.this, phoneVerificationActivity.class);
                String code = countryData.countryAreaCodes[registerSpinner.getSelectedItemPosition()];
                String phone = registerPhoneText.getText().toString().trim();
                String phoneNumber = "+" + code + phone;
                intent.putExtra("Email", registerEmailText.getText().toString().trim());
                intent.putExtra("First Name", registerFirstNameText.getText().toString().trim());
                intent.putExtra("Last Name", registerLastNameText.getText().toString().trim());
                intent.putExtra("Password", registerPasswordText.getText().toString().trim());
                intent.putExtra("Phone Number", phoneNumber);
                startActivity(intent);
            } else {
                registerPhoneText.requestFocus();
                Toast.makeText(registerActivity.this, "Phone number issue", Toast.LENGTH_LONG).show();
                //put Text watcher for 10 Digit phone number
            }

        } else {
            registerRePasswordText.requestFocus();
            Toast.makeText(registerActivity.this, "Password Dosent match", Toast.LENGTH_LONG).show();
            registerRePasswordText.setError("Password doesn't match");
        }
    }

    public boolean validateRegEmail() {
        String email = registerEmailText.getText().toString();

        if (!email.trim().matches(emailPattern) && email.length() > 0) {

            registerEmailText.setError("Please enter correct id");
            registerEmailText.requestFocus();
            return false;
        } else if (email.trim().isEmpty()) {
            registerEmailText.setError("Please enter correct id");
            registerEmailText.requestFocus();
            return false;

        } else {
            return true;
        }

    }

    public boolean validateFirstName() {
        String firstName = registerFirstNameText.getText().toString().trim();
        if (firstName.isEmpty()) {
            registerFirstNameText.setError("Please enter first name");
            registerFirstNameText.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    public boolean validateLastName() {
        String lastName = registerLastNameText.getText().toString().trim();
        if (lastName.isEmpty()) {
            registerLastNameText.setError("Please enter last name");
            registerLastNameText.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    public boolean validateRegPassword() {
        String password = registerPasswordText.getText().toString();

        if (!password.matches(passwordPattern) && password.length() > 0) {
            registerPasswordText.setError("should contain A-Z, a-z, 0-9, $/@/&");
            registerPasswordText.requestFocus();
            return false;
        } else if (password.trim().isEmpty()) {
            registerPasswordText.setError("Please enter password");
            registerPasswordText.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    public boolean validateConfirmPassword() {
        String confirmPass = registerRePasswordText.getText().toString().trim();

        if (confirmPass.isEmpty()) {
            registerRePasswordText.setError("Please enter password");
            registerRePasswordText.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public boolean validatePhoneNum() {
        String regPhoneNum = registerPhoneText.getText().toString().trim();
        if (regPhoneNum.length() != 10 && regPhoneNum.length() > 0) {
            registerPhoneText.setError("Please enter correct no");
            registerPhoneText.requestFocus();
            return false;
        } else if (regPhoneNum.isEmpty()) {
            registerPhoneText.setError("Please enter no");
            registerPhoneText.requestFocus();
            return false;
        } else {
            return true;
        }

    }


    private TextWatcher validateTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String reg = registerPasswordText.getText().toString();
            String reRe = registerRePasswordText.getText().toString();

            if (reRe.hashCode() != reg.hashCode()) {
                registerRePasswordText.setError("Password doesn't match");
                passMatch = false;
            }


        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private class genTextWatcher implements TextWatcher {
        private View view;

        public genTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch (view.getId()) {
                case R.id.registerEmailText:
                    validateRegEmail();
                    break;

                case R.id.registerFirstNameText:
                    validateFirstName();
                    break;

                case R.id.registerLastNameText:
                    validateLastName();
                    break;

                case R.id.registerPasswordText:
                    validateRegPassword();
                    break;

                case R.id.registerRePassword:
                    validateConfirmPassword();
                    break;

                case R.id.registerPhoneText:
                    validatePhoneNum();
                    break;

            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}