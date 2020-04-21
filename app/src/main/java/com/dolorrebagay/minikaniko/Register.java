package com.dolorrebagay.minikaniko;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullname, mEmail, mPassword, mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth; //to register the user in the firebase
    ProgressBar progressBar;
    FirebaseFirestore fStore; //create collection, documents and store data
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullname = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createText);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);

        //check if the user has already logged in
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        }

        //registerbtn clicked  -> validate
        //RETRIEVE DATA
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail
                        .getText()
                        .toString()
                        .trim();
                String password = mPassword
                        .getText()
                        .toString()
                        .trim();
                final String fullname = mFullname
                        .getText()
                        .toString();

                final String phone = mPhone
                        .getText()
                        .toString();

                if (TextUtils.isEmpty(email)) { //pass email string
                    mEmail.setError("Email Is Required.");// bawal empty
                    return;
                }
                if (TextUtils.isEmpty(password)) { //pass password string
                    mPassword.setError("Password Is Required.");
                    return;
                }

                if (password.length() < 6) {// need more than 6 characters
                    mPassword.setError("Password must have at least 5 characters.");
                    return;

                }

                progressBar.setVisibility(View.VISIBLE); //para kita progressbar

                //register user in firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this, "User Created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();//retrieve the userid of the current registered user
                            DocumentReference documentReference = fStore.collection("users").document(userID); //automatically create users and pass to userID
                            Map<String, Object> user = new HashMap<>();
                            //insert data
                            user.put("fName", fullname); //fName is the attribute name && fullname yung variable
                            user.put("email", email);
                            user.put("phone", phone);
                            //insert now to cloud
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "on  Success: user Profile is created for " + userID); //display if successfully stored
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "on Failure: " + e.toString()); //display the problem

                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); //para mawala na kapag nag nag error yung paka login

                        }
                    }
                });


            }
        });


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class)); //redirect to login activity pag pinindot mloginbtn

            }
        });
    }
}
