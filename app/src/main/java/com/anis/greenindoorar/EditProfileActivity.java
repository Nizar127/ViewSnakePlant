package com.anis.greenindoorar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText profilefirstName, profilelastName, profileEmail, profilefullName;
    ImageView profileImageView;
    Button saveProfile;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    TextView deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Intent data = getIntent();
        String firstName = data.getStringExtra("First Name");
        String lastName = data.getStringExtra("Last Name");
        String fullName = data.getStringExtra("Full Name");
        String email = data.getStringExtra("Email");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        profilefirstName = findViewById(R.id.editTextTextFirstName);
        profilelastName = findViewById(R.id.editTextTextLastName);
        profileEmail = findViewById(R.id.editTextTextEmailAddress);
        profileImageView = findViewById(R.id.imageView6);
        saveProfile = findViewById(R.id.saveBtn);
        deleteAccount = findViewById(R.id.deleteText);

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(EditProfileActivity.this);
                dialog.setTitle("Are you sure?");
                dialog.setMessage("This action will remove all your data from the application" +
                        " and you won't be able to access the app");
                dialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(EditProfileActivity.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                dialog.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
            }
        });

        StorageReference profileRef = storageReference.child("users/"+ fAuth.getCurrentUser().getUid() + "profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });


        profileImageView.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when user click on image, it will open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        }));

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profilefirstName.getText().toString().isEmpty() || profilelastName.getText().toString().isEmpty() || profileEmail.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "One or many fields are empty", Toast.LENGTH_SHORT).show();
                }

                //change email
                final String email = profileEmail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        DocumentReference docRef = fStore.collection("users").document(user.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("Email", email);
                        edited.put("First Name", profilefirstName.getText().toString());
                        edited.put("Last Name", profilelastName.getText().toString());
                        edited.put("Full Name", profilefirstName.getText().toString() + " " + profilelastName.getText().toString() );
                        //edited.put("Full Name", profilefullName.getText().toString());
                        docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), ContactsContract.Profile.class));
                                finish();
                            }
                        });
                        Toast.makeText(EditProfileActivity.this, "Email has been successfully changed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        profileEmail.setText(email);
        profilefirstName.setText(firstName);
        profilelastName.setText(lastName);




        Log.d(TAG, "onCreate: " + firstName + " " + lastName + " " + email);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();

                //profileImage.setImageURI(imageUri);

                uploadImagetoFirebase(imageUri);
            }
        }
    }

    private void uploadImagetoFirebase(Uri imageUri) {
        final StorageReference fileRef = storageReference.child("users/"+ fAuth.getCurrentUser().getUid() + "profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImageView);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image failed to upload", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

