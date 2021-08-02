package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference, mUsersReference;
    private StorageReference mUserProfileImageReference;
    private String currentUserID, downloadUrl;

    private CircleImageView profileImage;
    private CountryCodePicker countryCodePicker;
    private ImageButton backButton;
    private MaterialButton saveButton;
    private ProgressBar profileImageLoadingBar;
    private ProgressDialog loadingBar;
    private TextInputLayout textInputLayoutUsername;
    private TextInputEditText firstName, lastName, emailAddress, username, phoneNumber, profileStatus;

    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person_profile);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        currentUserID = mAuth.getCurrentUser().getUid();

        profileImage = findViewById(R.id.editPersonProfileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        firstName = findViewById(R.id.editTextFirstName);
        lastName = findViewById(R.id.editTextLastName);
        emailAddress = findViewById(R.id.editTextEmailAddress);
        textInputLayoutUsername = findViewById(R.id.textInputEditPersonProfileUsername);
        username = findViewById(R.id.editTextUsername);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        profileStatus = findViewById(R.id.editPersonProfileStatus);

        countryCodePicker = findViewById(R.id.editPersonProfileCountryCodePicker);
        countryCodePicker.registerCarrierNumberEditText(phoneNumber);

        loadingBar = new ProgressDialog(EditPersonProfileActivity.this);
        profileImageLoadingBar = findViewById(R.id.editPersonProfileLoadingBar);

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    textInputLayoutUsername.setErrorEnabled(false);
                }

                else if (charSequence.length() < 3) {
                    textInputLayoutUsername.setErrorEnabled(true);
                    textInputLayoutUsername.setError("The minimum length is 3 characters.");
                }

                else {
                    textInputLayoutUsername.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileI = dataSnapshot.child("profileImage").getValue().toString();
                    downloadUrl = profileI;

                    String firstN = dataSnapshot.child("firstName").getValue().toString();
                    String lastN = dataSnapshot.child("lastName").getValue().toString();
                    String emailA = dataSnapshot.child("emailAddress").getValue().toString();
                    String userN = dataSnapshot.child("username").getValue().toString();
                    String phoneN = dataSnapshot.child("phoneNumber").getValue().toString();
                    String profileS = dataSnapshot.child("profileStatus").getValue().toString();

                    firstName.setText(firstN);
                    lastName.setText(lastN);
                    emailAddress.setText(emailA);
                    username.setText(userN);
                    phoneNumber.setText(phoneN);
                    profileStatus.setText(profileS);

                    if (!profileI.equals("-1"))
                        Picasso.get().load(profileI).placeholder(R.drawable.ic_baseline_person_75).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveButton = findViewById(R.id.cirSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInformation();
            }
        });

        backButton = findViewById(R.id.editPersonProfileBackBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETFirstName = firstName.getText().toString().trim();
                        final String mETLastName = lastName.getText().toString().trim();
                        final String mETUsername = username.getText().toString().trim();
                        final String mETPhoneNumber = phoneNumber.getText().toString().trim();
                        final String mETProfileStatus = profileStatus.getText().toString().trim();

                        EditPersonProfileActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (! mETFirstName.isEmpty() && ! mETLastName.isEmpty() && ! mETUsername.isEmpty() && mETUsername.length() >= 3 && ! mETPhoneNumber.isEmpty() && ! mETProfileStatus.isEmpty()) {
                                    saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorTurquoise));
                                    saveButton.setEnabled(true);
                                }

                                else {
                                    saveButton.setBackgroundTintList(EditPersonProfileActivity.this.getColorStateList(R.color.colorGray));
                                    saveButton.setEnabled(false);
                                }
                            }
                        });
                    }

                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();
    }

    private void saveInformation() {
        loadingBar.setTitle("Saving...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        String firstNameET = firstName.getText().toString().trim();
        final String revisedFirstNameET  = firstNameET.substring(0, 1).toUpperCase() + firstNameET.substring(1).toLowerCase();
        String lastNameET = lastName.getText().toString().trim();
        final String revisedLastNameET = lastNameET.substring(0, 1).toUpperCase() + lastNameET.substring(1).toLowerCase();
        final String usernameET = username.getText().toString().trim();
        final String phoneNumberET = countryCodePicker.getFullNumberWithPlus().trim();
        final String profileStatusET = profileStatus.getText().toString().trim();

        HashMap userMap = new HashMap();
            userMap.put("username", usernameET);
            userMap.put("firstName", revisedFirstNameET);
            userMap.put("lastName", revisedLastNameET);
            userMap.put("profileImage", downloadUrl);
            userMap.put("phoneNumber", phoneNumberET);
            userMap.put("profileStatus", profileStatusET);

        mUsersReference.child(currentUserID).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(EditPersonProfileActivity.this, "Your changes were saved successfully!", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }

                else {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(EditPersonProfileActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Setting...");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                profileImageLoadingBar.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();
                StorageReference filePath = mUserProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().getDownloadUrl().toString();

                            Toast.makeText(EditPersonProfileActivity.this, "Your profile image was saved successfully!", Toast.LENGTH_LONG).show();
                            Picasso.get().load(downloadUrl).into(profileImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            profileImageLoadingBar.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError(Exception e) {

                                        }
                                    });

                                    loadingBar.dismiss();
                        }

                        else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(EditPersonProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }

            else {
                Toast.makeText(EditPersonProfileActivity.this, "Your image cannot be cropped! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(EditPersonProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}