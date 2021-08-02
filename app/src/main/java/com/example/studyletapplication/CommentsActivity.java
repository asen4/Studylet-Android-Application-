package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private ArrayList<Comments> commentsList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;

    private FirebaseAuth mAuth;
    private String currentUserID, postKey, saveCurrentDate, saveCurrentTime, commentID;
    private DatabaseReference mRootReference, mUsersReference, mPostsReference;

    private RelativeLayout myRelativeLayout;
    private CircleImageView profileImage;
    private ImageButton sendCommentButton, backButton;
    private MaterialButton hideShowPostButton;
    private ImageView postImage;
    private RecyclerView commentsListRV;
    private TextInputEditText inputComment;
    private TextView postTitle, postDateAndTime, postName, postDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        initializeVariables();

        fetchComments();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        commentsAdapter = new CommentsAdapter(commentsList);
        commentsListRV = findViewById(R.id.commentsList);
        commentsListRV.setHasFixedSize(true);
        commentsListRV.setAdapter(commentsAdapter);
        commentsListRV.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));

        myRelativeLayout = findViewById(R.id.relativeLayout);
        profileImage = findViewById(R.id.commentsPostProfileImage);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        hideShowPostButton = findViewById(R.id.hideShowPostButton);
        postImage = findViewById(R.id.commentsPostImage);
        inputComment = findViewById(R.id.inputComment);
        inputComment.setMaxHeight(100);
        postTitle = findViewById(R.id.commentsPostTitle);
        postDateAndTime = findViewById(R.id.commentsPostDateAndTime);
        postName = findViewById(R.id.commentsPostName);
        postDescription = findViewById(R.id.commentsPostDescription);

        postKey = getIntent().getExtras().get("postKey").toString();

        backButton = findViewById(R.id.commentsBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        hideShowPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hideShowPostButton.getText().equals("Hide")) {
                    myRelativeLayout.setVisibility(View.GONE);
                    hideShowPostButton.setText("Show");
                }

                else {
                    myRelativeLayout.setVisibility(View.VISIBLE);
                    hideShowPostButton.setText("Hide");
                }
            }
        });

        mPostsReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePicture = dataSnapshot.child("profileImage").getValue().toString();

                    if (! profilePicture.equals("-1"))
                        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_baseline_person_60).into(profileImage);

                    final String firstName = dataSnapshot.child("firstName").getValue().toString();
                    final String lastName = dataSnapshot.child("lastName").getValue().toString();

                    mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String firstN = dataSnapshot.child("firstName").getValue().toString();
                                String lastN = dataSnapshot.child("lastName").getValue().toString();

                                if (firstN.equals(firstName) && lastN.equals(lastName))
                                    postName.setText("You");
                                else
                                    postName.setText(firstName + " " + lastName);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    String title = dataSnapshot.child("postTitle").getValue().toString();
                    String date = dataSnapshot.child("date").getValue().toString();
                    String time = dataSnapshot.child("time").getValue().toString();
                    String description = dataSnapshot.child("postDescription").getValue().toString();
                    final String image = dataSnapshot.child("postImage").getValue().toString();

                    postImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(CommentsActivity.this, ImageViewerActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra("URL", image);
                            startActivity(intent);
                        }
                    });

                    postTitle.setText(title);
                    postDescription.setText(description);
                    Picasso.get().load(image).placeholder(R.drawable.ic_baseline_image_150).into(postImage);

                    DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, -1);
                    String yesterdayDate = dateFormat.format(calendar.getTime());

                    String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                    if (date.equals(currentDate))
                        postDateAndTime.setText("today at " + time);

                    else if (date.equals(yesterdayDate))
                        postDateAndTime.setText("yesterday at " + time);

                    else {
                        try {
                            if (isDateInCurrentWeek(dateFormat.parse(date))) {
                                Date temp = dateFormat.parse(date);
                                SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                                postDateAndTime.setText(dateFormat2.format(temp) + " at " + time);
                            }

                            else
                                postDateAndTime.setText(date);
                        }

                        catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    sendCommentButton.setBackground(getResources().getDrawable(R.drawable.gray_round_button));
                    sendCommentButton.setEnabled(false);
                }

                else {
                    sendCommentButton.setBackground(getResources().getDrawable(R.drawable.round_button));
                    sendCommentButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }

    private void fetchComments() {
        mRootReference.child("Comments").child(postKey)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {
                            Comments comment = dataSnapshot.getValue(Comments.class);
                            commentsList.add(comment);
                            commentsAdapter.notifyDataSetChanged();
                            commentsListRV.smoothScrollToPosition(commentsListRV.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendComment() {
        final String comment = inputComment.getText().toString();

        DatabaseReference pushedCommentReference = mRootReference.child("Comments").child(postKey).push();
        commentID = pushedCommentReference.getKey();

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();

                    Map commentMap = new HashMap();
                        commentMap.put("comment", comment);
                        commentMap.put("commentID", commentID);
                        commentMap.put("date", saveCurrentDate);
                        commentMap.put("time", saveCurrentTime);
                        commentMap.put("firstName", firstName);
                        commentMap.put("lastName", lastName);

                    String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                    if (! profileImage.equals("-1"))
                        commentMap.put("profileImage", profileImage);
                    else
                        commentMap.put("profileImage", "-1");

                    mRootReference.child("Comments").child(postKey).child(commentID).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (! task.isSuccessful()) {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }

                            inputComment.setText("");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}