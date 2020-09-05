package com.connectors.connector;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;


public class settings extends AppCompatActivity {
    Button settings_updateButton;
    EditText settings_status,settings_userName;
    CircleImageView profile_image;
    //DocumentReference rootref;
    private FirebaseAuth mAuth;
    //FirebaseFirestore db;
    final int galPick=1;
    StorageReference profileImageStorage;
    DatabaseReference databseRef;
    ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        //db = FirebaseFirestore.getInstance();
        databseRef = FirebaseDatabase.getInstance().getReference();
        loadingBar=new ProgressDialog(this);
        profileImageStorage = FirebaseStorage.getInstance().getReference().child("Profile Images");


        //rootref= db.collection("Users").document(mAuth.getCurrentUser().getUid());

        settings_updateButton=findViewById(R.id.settings_updateButton);
        settings_status=findViewById(R.id.settings_status);
        settings_userName=findViewById(R.id.settings_userName);
        profile_image=findViewById(R.id.profile_image);

        databseRef.child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    settings_userName.setText(dataSnapshot.child("Full Name").getValue().toString());
                    settings_status.setText(dataSnapshot.child("Status").getValue().toString());
                    Glide.with(settings.this).load(dataSnapshot.child("image").getValue().toString()).into(profile_image);

                // Picasso.get().load(retrieveProfileImage).into(profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



//        databseRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if(documentSnapshot.exists()){
//                    settings_userName.setText(documentSnapshot.getString("Full Name"));
//                    settings_status.setText(documentSnapshot.getString("Status"));
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(settings.this,"Update Data",Toast.LENGTH_LONG).show();
//            }
//        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galPick);
            }
        });
    }
    public void updateSettings(View view) {
        databseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Full Name").setValue(settings_userName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(settings.this,"User Name Updated",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(settings.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
        databseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Status").setValue(settings_status.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(settings.this,"Status Updated",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(settings.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
//        rootref.update("Full Name",settings_userName.getText().toString());
//        rootref.update("Status",settings_status.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==galPick && resultCode==RESULT_OK && data!=null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if(resultCode==RESULT_OK){
                    loadingBar.setTitle("Updating");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.setMessage("Your Profile Image is Updating...");
                    loadingBar.show();
                    Uri resultUri=result.getUri();
                    final StorageReference filePath= profileImageStorage.child(mAuth.getCurrentUser().getUid()+".jpg");

                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                //final String downloadUri =filePath.getDownloadUrl().toString();
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        databseRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("image").setValue(String.valueOf(uri)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(settings.this,"Profile pic Updated",Toast.LENGTH_LONG).show();
                                                    loadingBar.dismiss();
                                                }else{
                                                    Toast.makeText(settings.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                                    }
                                });
                            }else{
                                Toast.makeText(settings.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }
    }
}