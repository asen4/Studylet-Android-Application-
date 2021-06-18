package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ImageView postImage;
    private MaterialButton cancelButton, editPostButton, deletePostButton;
    private TextView postTitle, postDescription;

    private FirebaseAuth mAuth;
    private DatabaseReference mClickPostReference;
    private String currentUserID, postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postKey = getIntent().getExtras().get("postKey").toString();
        mClickPostReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        backButton = findViewById(R.id.clickPostBackButton);
        postImage = findViewById(R.id.clickPostPicture);
        postDescription = findViewById(R.id.clickPostDescription);
        editPostButton = findViewById(R.id.editPostButton);
        deletePostButton = findViewById(R.id.deletePostButton);
        cancelButton = findViewById(R.id.clickPostCancelButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mClickPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String postMessage = dataSnapshot.child("postDescription").getValue().toString();
                    String postPicture = dataSnapshot.child("postImage").getValue().toString();
                    String userID = dataSnapshot.child("userID").getValue().toString();

                    postDescription.setText(postMessage);
                    Picasso.get().load(postPicture).placeholder(R.drawable.ic_baseline_image_150).into(postImage);

                    if (currentUserID.equals(userID)) {
                        editPostButton.setVisibility(View.VISIBLE);
                        deletePostButton.setVisibility(View.VISIBLE);
                    }

                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editPost(postMessage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });
    }

    private void deletePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Delete Post?");
        builder.setMessage("This action cannot be undone.");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickPostReference.removeValue();
                sendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Your post has been successfully deleted!", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void editPost (String postDescription) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post?");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(postDescription);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickPostReference.child("postDescription").setValue(inputField.getText().toString());
                sendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Your post has been updated successfully!", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}