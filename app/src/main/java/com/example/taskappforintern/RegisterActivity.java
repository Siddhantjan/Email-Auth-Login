package com.example.taskappforintern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText userFullName, UserEmailAddress, UserPassword, UserConfirmPassword;
    private Button mRegisterBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userFullName = findViewById(R.id.user_name);
        UserEmailAddress = findViewById(R.id.register_email_address);
        UserPassword = findViewById(R.id.register_mail_password);
        UserConfirmPassword = findViewById(R.id.mail_password_confirm);
        mRegisterBtn = findViewById(R.id.registerBtn);

        //Initialize Auth Variable
        mAuth = FirebaseAuth.getInstance();

        //Click on RegisterBtn
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract all the data from the Layout
                String userName = userFullName.getText().toString();
                String userMail = UserEmailAddress.getText().toString();
                String userPass = UserPassword.getText().toString();
                String userConfirmPass = UserConfirmPassword.getText().toString();

                if (userName.isEmpty()) {
                    userFullName.setError("Full name is Required");
                    return;
                }
                if (userMail.isEmpty()) {
                    UserEmailAddress.setError("mail address is required");
                    return;
                }
                if (userPass.isEmpty()) {
                    UserPassword.setError("password is required");
                    return;
                }
                if (userConfirmPass.isEmpty()) {
                    UserConfirmPassword.setError("re enter your password");
                    return;
                }
                if (!userPass.equals(userConfirmPass)) {
                    UserConfirmPassword.setError("Password do not match");
                    return;
                }
                //  Now Data is Validated
                // register the user using firebase
                Toast.makeText(getApplicationContext(), "All Set", Toast.LENGTH_SHORT).show();
                mAuth.createUserWithEmailAndPassword(userMail,userPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Send User to next process
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();

                    }
                }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser=mAuth.getCurrentUser();
                            String user_id = firebaseUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
                            HashMap<String,String> profile = new HashMap<>();
                            profile.put("userID",user_id);
                            profile.put("Name",userName);
                            profile.put("Email",userMail);
                            profile.put("imageURI","default");
                            databaseReference.setValue(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else
                        {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        findViewById(R.id.login_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }
}