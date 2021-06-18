package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private String currentUserID, mVerificationID, downloadUrl;
    private EditText editTextUsername, editTextFirstName, editTextLastName, editTextPhoneNumber, editTextVerifcationCode;
    private TextInputLayout textInputLayoutUsername;
    private CountryCodePicker countryCodePicker;
    private MaterialButton buttonSubmitPhoneNumber, buttonSumbitVerificationCode, buttonNotNowButton, buttonSaveInformation;
    private CircleImageView profileImage;
    private LinearLayout linearLayoutPhoneNumber, linearLayoutCode, linearLayoutProfileImage, linearLayoutOtherCredentials;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;
    private StorageReference mUserProfileImageReference;

    private ProgressDialog loadingBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    final static int GALLERY_PICK = 1;

    private static final String TAG = "SetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initializeVariables();

        buttonSubmitPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = countryCodePicker.getFullNumberWithPlus();

                loadingBar.setTitle("Verifying your Phone Number");
                loadingBar.setMessage("Please wait while we are processing your request.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthProvider.getInstance().verifyPhoneNumber (
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        SetupActivity.this,
                        mCallbacks
                );
            }
        });

        buttonSumbitVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verificationCode = editTextVerifcationCode.getText().toString();
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationID, verificationCode);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        });

        buttonNotNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayoutProfileImage.setVisibility(View.GONE);
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
                if (TextUtils.isEmpty(s)) {
                    textInputLayoutUsername.setErrorEnabled(false);
                }

                else if (s.toString().length() < 3) {
                    textInputLayoutUsername.setErrorEnabled(true);
                    textInputLayoutUsername.setError("Your username is too short! The minimum length is 3 characters.");
                }

                else {
                    textInputLayoutUsername.setErrorEnabled(false);
                }
            }
        });

        editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    editTextPhoneNumber.setBackgroundResource(R.drawable.rectangular_edittext);
                }

                else if (s.toString().length() == 14) {
                    editTextPhoneNumber.setBackgroundResource(R.drawable.correct_rectangular_edittext);
                }

                else {
                    editTextPhoneNumber.setBackgroundResource(R.drawable.incorrect_rectangular_edittext);
                }
            }
        });

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (! isInterrupted()) {
                    try {
                        Thread.sleep(1000);

                        final String mETUsername = editTextUsername.getText().toString().trim();
                        final String mETFirstName = editTextFirstName.getText().toString().trim();
                        final String mETLastName = editTextLastName.getText().toString().trim();
                        final String mETPhoneNumber = editTextPhoneNumber.getText().toString().trim();
                        final String mETVerificationCode = editTextVerifcationCode.getText().toString().trim();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (! mETPhoneNumber.isEmpty()) {
                                    buttonSubmitPhoneNumber.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorTurquoise));
                                    buttonSubmitPhoneNumber.setEnabled(true);
                                }

                                else {
                                    buttonSubmitPhoneNumber.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorGray));
                                    buttonSubmitPhoneNumber.setEnabled(false);
                                }

                                if (! mETVerificationCode.isEmpty()) {
                                    buttonSumbitVerificationCode.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorTurquoise));
                                    buttonSumbitVerificationCode.setEnabled(true);
                                }

                                else {
                                    buttonSumbitVerificationCode.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorGray));
                                    buttonSumbitVerificationCode.setEnabled(false);
                                }

                                if (mETUsername.length() >= 3 && mETUsername.length() <= 8 && ! mETFirstName.isEmpty() && ! mETLastName.isEmpty() && mETPhoneNumber.length() == 14) {
                                    buttonSaveInformation.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorTurquoise));
                                    buttonSaveInformation.setEnabled(true);
                                }

                                else {
                                    buttonSaveInformation.setBackgroundTintList(ContextCompat.getColorStateList(SetupActivity.this, R.color.colorGray));
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

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                Toast.makeText(SetupActivity.this, "Your phone number was automatically verified!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(SetupActivity.this, "Invalid Phone Number! Please try again!", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationID = s;

                linearLayoutPhoneNumber.setVisibility(View.GONE);
                linearLayoutCode.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(SetupActivity.this, "The verification code has been sent! Check your messages!", Toast.LENGTH_LONG).show();
            }
        };
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
        editTextPhoneNumber = findViewById(R.id.phoneText);
        editTextVerifcationCode = findViewById(R.id.editTextVerificatoinCode);

        textInputLayoutUsername = findViewById(R.id.textInputSetupUsername);

        buttonSubmitPhoneNumber = findViewById(R.id.submitPhoneNumberButton);
        buttonSubmitPhoneNumber.setEnabled(false);
        buttonSumbitVerificationCode = findViewById(R.id.submitCodeButton);
        buttonSumbitVerificationCode.setEnabled(false);

        buttonSaveInformation = findViewById(R.id.saveCredentialsButton);
        buttonSaveInformation.setEnabled(false);
        buttonNotNowButton = findViewById(R.id.notNowProfileImageButton);
        profileImage = findViewById(R.id.setupProfileImage);

        linearLayoutPhoneNumber = findViewById(R.id.inputPhoneNumberLayout);
        linearLayoutCode = findViewById(R.id.inputCodeLayout);
        linearLayoutProfileImage = findViewById(R.id.inputProfileImageLayout);
        linearLayoutOtherCredentials = findViewById(R.id.inputOtherCredentialsLayout);

        countryCodePicker = findViewById(R.id.countryCodePicker);
        countryCodePicker.registerCarrierNumberEditText(editTextPhoneNumber);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SetupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            linearLayoutCode.setVisibility(View.GONE);
                            linearLayoutProfileImage.setVisibility(View.VISIBLE);
                            loadingBar.dismiss();

                        }

                        else {
                            Toast.makeText(SetupActivity.this, "Your verification code is incorrect! Try again!", Toast.LENGTH_LONG).show();
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
                    .start(SetupActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Setting your Profile Image");
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
                            Picasso.get().load(downloadUrl).placeholder(R.drawable.ic_baseline_person_black_250).into(profileImage);
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

        loadingBar.setTitle("Saving your Credentials");
        loadingBar.setMessage("Please wait while we are processing your request.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("firstName", revisedFirstName);
            userMap.put("lastName", revisedLastName);
            userMap.put("profileImage", downloadUrl);
            userMap.put("phoneNumber", phoneNumber);
            userMap.put("profileStatus", "Hey there, I am also using this social networking app.");
            userMap.put("Gender", "Gender");
            userMap.put("Month (DOB)", "Month (DOB)");
            userMap.put("Year (DOB)", "Year (DOB)");

        mUsersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(SetupActivity.this, "Your account was created successfully!", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }

                else {
                    String errorMessage = task.getException().toString();
                    Toast.makeText(SetupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });

    }

    public void onBackPressed() {

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}