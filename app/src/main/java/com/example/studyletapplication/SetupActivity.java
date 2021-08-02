package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private String currentUserID, downloadUrl;
    private EditText editTextUsername, editTextFirstName, editTextLastName, editTextPhoneNumber;
    private TextInputLayout textInputLayoutUsername;
    private CardView profileImageInstructions;
    private CountryCodePicker countryCodePicker;
    private MaterialButton buttonNotNowButton, buttonSaveInformation;
    private CircleImageView profileImage;
    private LinearLayout linearLayoutOtherCredentials;
    private RelativeLayout relativeLayoutProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;
    private StorageReference mUserProfileImageReference;

    private ProgressDialog loadingBar;
    private ProgressBar profileImageLoadingBar;

    final static int GALLERY_PICK = 1;

    private static final String TAG = "SetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initializeVariables();

        buttonNotNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageInstructions.setVisibility(View.GONE);
                relativeLayoutProfileImage.setVisibility(View.GONE);
                linearLayoutOtherCredentials.setVisibility(View.VISIBLE);
            }
        });

        buttonSaveInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s))
                    textInputLayoutUsername.setErrorEnabled(false);


                else if (s.toString().length() < 3) {
                    textInputLayoutUsername.setErrorEnabled(true);
                    textInputLayoutUsername.setError("The minimum length is 3 characters.");
                }

                else
                    textInputLayoutUsername.setErrorEnabled(false);
            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETUsername = editTextUsername.getText().toString().trim();
                        final String mETFirstName = editTextFirstName.getText().toString().trim();
                        final String mETLastName = editTextLastName.getText().toString().trim();
                        final String mETPhoneNumber = editTextPhoneNumber.getText().toString().trim();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mETUsername.length() >= 3 && mETUsername.length() <= 8 && !mETFirstName.isEmpty() && !mETLastName.isEmpty() && ! mETPhoneNumber.isEmpty()) {
                                    buttonSaveInformation.setBackgroundTintList(SetupActivity.this.getColorStateList(R.color.colorTurquoise));
                                    buttonSaveInformation.setEnabled(true);
                                }

                                else {
                                    buttonSaveInformation.setBackgroundTintList(SetupActivity.this.getColorStateList( R.color.colorGray));
                                    buttonSaveInformation.setEnabled(false);
                                }
                            }
                        });
                    }

                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        thread.start();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID =  mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mUserProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        downloadUrl = "-1";

        loadingBar = new ProgressDialog(SetupActivity.this);

        editTextUsername = findViewById(R.id.editTextSetupUsername);
        editTextFirstName = findViewById(R.id.editTextSetupFirstName);
        editTextLastName = findViewById(R.id.editTextSetupLastName);
        editTextPhoneNumber = findViewById(R.id.editTextSetupPhoneNumber);

        textInputLayoutUsername = findViewById(R.id.textInputSetupUsername);

        buttonSaveInformation = findViewById(R.id.saveCredentialsButton);
        buttonSaveInformation.setEnabled(false);
        buttonNotNowButton = findViewById(R.id.notNowProfileImageButton);
        profileImageInstructions = findViewById(R.id.instructionsCardView);
        profileImage = findViewById(R.id.setupProfileImage);
        profileImageLoadingBar = findViewById(R.id.profileImageLoadingBar);

        relativeLayoutProfileImage = findViewById(R.id.inputProfileImageLayout);
        linearLayoutOtherCredentials = findViewById(R.id.inputOtherCredentialsLayout);

        countryCodePicker = findViewById(R.id.countryCodePicker);
        countryCodePicker.registerCarrierNumberEditText(editTextPhoneNumber);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(SetupActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                profileImageLoadingBar.setVisibility(View.VISIBLE);

                loadingBar.setTitle("Setting...");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                StorageReference filePath = mUserProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().getDownloadUrl().toString();

                            Toast.makeText(SetupActivity.this, "Your profile image was saved successfully!", Toast.LENGTH_LONG).show();
                            Picasso.get().load(downloadUrl).into(profileImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    profileImageLoadingBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });

                            buttonNotNowButton.setText("Continue");
                            loadingBar.dismiss();
                        }
                    }
                });
            }

            else {
                Toast.makeText(SetupActivity.this, "Your image cannot be cropped! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveAccountSetupInformation() {
        String username = editTextUsername.getText().toString().trim();
        String firstName = editTextFirstName.getText().toString().trim();
        String revisedFirstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
        String lastName = editTextLastName.getText().toString().trim();
        String revisedLastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        String phoneNumber = countryCodePicker.getFullNumberWithPlus().trim();

        loadingBar.setTitle("Saving...");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("firstName", revisedFirstName);
            userMap.put("lastName", revisedLastName);
            userMap.put("emailAddress", mAuth.getCurrentUser().getEmail());
            userMap.put("profileImage", downloadUrl);
            userMap.put("phoneNumber", phoneNumber);
            userMap.put("profileStatus", "Hi there, I am also using this social networking app.");

        mUsersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(SetupActivity.this, "Your account was created successfully!", Toast.LENGTH_LONG).show();
                }

                else {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(SetupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                loadingBar.dismiss();
            }
        });
    }

    public void onBackPressed() {
        Toast.makeText(SetupActivity.this, "Back button is unavailable!", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}