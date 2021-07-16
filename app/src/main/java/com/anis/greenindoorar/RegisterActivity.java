package com.anis.greenindoorar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    String TAG;
    EditText mFirstName, mLastName, mEmail, mPassword, mFullName;
    Button mRegBtn;
    TextView mLoginBtn; //check here
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstName  = findViewById(R.id.editTextFirstName);
        mLastName   = findViewById(R.id.editTextLastName);
        mEmail      = findViewById(R.id.editTextEmail);
        mPassword   = findViewById(R.id.editTextPassword);
        mRegBtn     = findViewById(R.id.regBtn);
        mLoginBtn   = findViewById(R.id.textAlready);

        fAuth       = FirebaseAuth.getInstance();
        fStore      = FirebaseFirestore.getInstance();

        if (fAuth.getCurrentUser() != null ) {
            //then, redirect user to ~mainactivity !!!!!JAP FOR NOW I'M CHANGING TI TO PROFILE ACTIVITY
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
            finish();
        }

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String firstname = mFirstName.getText().toString();
                final String lastname = mLastName.getText().toString();
                //  String fullname =

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required");
                    return;
                }

                if (password.length() < 7) {
                    mPassword.setError("Password must be more than 6 characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT).show();
                            userId = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userId);
                            //store the data using hashmap
                            Map<String, Object> user = new HashMap<>();
                            user.put("First Name" , firstname);
                            user.put("Last Name", lastname);
                            user.put("Full Name", firstname + " " + lastname);
                            user.put("Email", email);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: user profile is created for " + userId);
                                }
                            });

                            //then, redirect user to mainactivity
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when user click on the "Already have an account? Sign in here" text
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));


            }
        });

    }
}
