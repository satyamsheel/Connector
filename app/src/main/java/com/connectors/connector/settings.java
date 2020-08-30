package com.connectors.connector;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;


public class settings extends AppCompatActivity {
    Button settings_updateButton;
    EditText settings_status,settings_userName;
    CircleImageView profile_image;
    DocumentReference rootref;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        rootref= db.collection("Users").document(mAuth.getUid());

        settings_updateButton=findViewById(R.id.settings_updateButton);
        settings_status=findViewById(R.id.settings_status);
        settings_userName=findViewById(R.id.settings_userName);
        profile_image=findViewById(R.id.profile_image);

        rootref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    settings_userName.setText(documentSnapshot.getString("Full Name"));
                    settings_status.setText(documentSnapshot.getString("Status"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(settings.this,"Update Data",Toast.LENGTH_LONG).show();
            }
        });




    }

    public void updateSettings(View view) {
        rootref.update("Full Name",settings_userName.getText().toString());
        rootref.update("Status",settings_status.getText().toString());
    }
}