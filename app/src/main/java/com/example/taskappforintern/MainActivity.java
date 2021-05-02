package com.example.taskappforintern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_REQUST_CODE = 7171;
    private Button verifyBtn;
    private FirebaseAuth auth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseRef;
    private LinearLayout verifyLayout;
    private TextView mName, mEmail, DText;
    private CircleImageView mImage;
    private Uri uri;
    private UserData userData;
    private StorageReference storageReference;
    private StorageTask storageTask;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_REQUST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            if (storageTask != null && storageTask.isInProgress()){
                Toast.makeText(getApplicationContext(),"Uploading is in Progress",Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }

        }
    }

    private void uploadImage() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Upload a Image");
        dialog.show();
        if (uri != null){
            Bitmap bitmap = null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
            byte [] imageFileToByte = byteArrayOutputStream.toByteArray();
            final StorageReference imageReference= storageReference.child(userData.getName()+System.currentTimeMillis()+".jpg");
            storageTask = imageReference.putBytes(imageFileToByte);
            storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri =task.getResult();
                        String sDownloadURI = downloadUri.toString();
                        Map<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageURI",sDownloadURI);
                        mDatabaseRef.updateChildren(hashMap);
                        final DatabaseReference profileImagesReferences = FirebaseDatabase.getInstance()
                                .getReference("profile_images").child(mUser.getUid());
                        profileImagesReferences.push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    dialog.dismiss();

                                }
                                else {
                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                        });

                    }
                    else{
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mName = findViewById(R.id.userFullName);
        mImage = findViewById(R.id.profile_image);
        mEmail = findViewById(R.id.user_email_address);
        DText = findViewById(R.id.defaultText);

        auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
        storageReference= FirebaseStorage.getInstance().getReference("Profile_images");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userData = snapshot.getValue(UserData.class);
                assert userData != null;
                mName.setText(userData.getName());
                mEmail.setText(userData.getEmail());
                if (userData.getImageURI().equals("default")) {
                    DText.setVisibility(View.VISIBLE);
                    mImage.setImageResource(R.drawable.iconuser);
                } else {
                    Glide.with(getApplicationContext()).load(userData.getImageURI()).into(mImage);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        verifyLayout = findViewById(R.id.verify_layout);
        verifyBtn = findViewById(R.id.verify_btn);


        if (!Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
            verifyLayout.setVisibility(View.VISIBLE);
        }
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Verification Email sent", Toast.LENGTH_SHORT).show();
                        verifyLayout.setVisibility(View.GONE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery,"Choose Profile Pic"),PICK_REQUST_CODE);
            }
        });


        findViewById(R.id.logoutUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

}