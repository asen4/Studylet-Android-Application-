package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference, mFriendsReference, mPostsReference, mLikesReference, mDislikesReference;
    private String currentUserID, receiverUserID;
    private boolean likeChecker, dislikeChecker;
    private int countFriends = 0, countPosts = 0;

    private CircleImageView profileImage;
    private ImageButton backButton;
    private TextView name, phoneNumber, emailAddress, profileStatus, friends, posts;
    private RecyclerView listOfFriends, listOfPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initializeVariables();

        displayAllOfMyFriends();

        displayAllOfMyPosts();
    }

    private void displayAllOfMyPosts() {
        FirebaseRecyclerOptions<Posts> postsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(mPostsReference.orderByChild("userID").startAt(receiverUserID).endAt(receiverUserID + "\uf8ff"), Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, HomeFragment.PostsViewHolder> postsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, HomeFragment.PostsViewHolder>(postsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final HomeFragment.PostsViewHolder postsViewHolder, int position, @NonNull final Posts posts) {
                final String postKey = getRef(position).getKey();

                DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
                String yesterdayDate = dateFormat.format(calendar.getTime());

                String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                if (posts.getDate().equals(currentDate))
                    postsViewHolder.setDateAndTime("today at " + posts.getTime());


                else if (posts.getDate().equals(yesterdayDate))
                    postsViewHolder.setDateAndTime("yesterday at " + posts.getTime());


                else {
                    try {
                        if (isDateInCurrentWeek(dateFormat.parse(posts.getDate()))) {
                            Date date = dateFormat.parse(posts.getDate());
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                            postsViewHolder.setDateAndTime(dateFormat2.format(date) + " at " + posts.getTime());
                        }

                        else
                            postsViewHolder.setDateAndTime(posts.getDate());
                    }

                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                postsViewHolder.setPostTitle(posts.getPostTitle());
                postsViewHolder.setPostDescription(posts.getPostDescription());
                postsViewHolder.setPostImage(posts.getPostImage());
                postsViewHolder.profileImage.setVisibility(View.GONE);
                postsViewHolder.postName.setVisibility(View.GONE);

                postsViewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PersonProfileActivity.this, ImageViewerActivity.class);
                        intent.putExtra("URL", posts.getPostImage());
                        startActivity(intent);
                    }
                });

                postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(PersonProfileActivity.this, ClickPostActivity.class);
                        clickPostIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        clickPostIntent.putExtra("postKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });

                postsViewHolder.setLikeButtonStatus(postKey);

                postsViewHolder.linearLayoutLikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;
                        mLikesReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (likeChecker) {
                                    if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                                        mLikesReference.child(postKey).child(currentUserID).removeValue();
                                        postsViewHolder.linearLayoutDislikePostButton.setEnabled(true);
                                        likeChecker = false;
                                    }

                                    else {
                                        mLikesReference.child(postKey).child(currentUserID).setValue(true);
                                        postsViewHolder.linearLayoutDislikePostButton.setEnabled(false);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                postsViewHolder.setDislikeButtonStatus(postKey);

                postsViewHolder.linearLayoutDislikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dislikeChecker = true;
                        mDislikesReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dislikeChecker) {
                                    if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                                        mDislikesReference.child(postKey).child(currentUserID).removeValue();
                                        postsViewHolder.linearLayoutLikePostButton.setEnabled(true);
                                        dislikeChecker = false;
                                    }

                                    else {
                                        mDislikesReference.child(postKey).child(currentUserID).setValue(true);
                                        postsViewHolder.linearLayoutLikePostButton.setEnabled(false);
                                        dislikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                postsViewHolder.linearLayoutCommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(PersonProfileActivity.this, CommentsActivity.class);
                        commentsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });
            }

            @NonNull
            @Override
            public HomeFragment.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                return new HomeFragment.PostsViewHolder(view);
            }
        };

        listOfPosts.setAdapter(postsFirebaseRecyclerAdapter);
        postsFirebaseRecyclerAdapter.startListening();
    }

    private void displayAllOfMyFriends() {
        FirebaseRecyclerOptions<Friends> friendsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsReference.child(receiverUserID), Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull final Friends friends) {
                final String personKey = getRef(position).getKey();

                if (! currentUserID.equals(personKey)) {
                    mUsersReference.child(personKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String firstName = dataSnapshot.child("firstName").getValue().toString();
                                String lastName = dataSnapshot.child("lastName").getValue().toString();
                                String displayName = firstName + " " + lastName;
                                friendsViewHolder.firstName.setText(displayName);

                                String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                                if (! profileImage.equals("-1"))
                                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_black_250).into(friendsViewHolder.profileImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                else {
                    friendsViewHolder.firstName.setText("You");

                    mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                                if (! profileImage.equals("-1"))
                                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_70).into(friendsViewHolder.profileImage);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent personProfileIntent = new Intent(PersonProfileActivity.this, PersonProfileActivity.class);
                        personProfileIntent.putExtra("visitUserID", personKey);
                        startActivity(personProfileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_friends_display_layout, parent, false);
                return new FriendsViewHolder(view);
            }
        };

        listOfFriends.setAdapter(friendsFirebaseRecyclerAdapter);
        friendsFirebaseRecyclerAdapter.startListening();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getStringExtra("visitUserID").toString();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mLikesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDislikesReference = FirebaseDatabase.getInstance().getReference().child("Dislikes");
        mPostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        profileImage = findViewById(R.id.personProfileImage);
        profileImage.bringToFront();
        profileImage.requestLayout();

        friends = findViewById(R.id.friends);
        posts = findViewById(R.id.posts);

        name = findViewById(R.id.personProfileFullName);
        phoneNumber = findViewById(R.id.personProfilePhoneNumber);
        emailAddress = findViewById(R.id.personProfileEmailAddress);
        profileStatus = findViewById(R.id.personProfileStatus);

        listOfFriends = findViewById(R.id.personProfileListOfFriends);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(PersonProfileActivity.this, LinearLayoutManager.HORIZONTAL);
        listOfFriends.addItemDecoration(dividerItemDecoration);
        listOfFriends.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

        listOfPosts = findViewById(R.id.personProfileListOfPosts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PersonProfileActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        listOfPosts.setLayoutManager(linearLayoutManager);

        backButton = findViewById(R.id.personProfileBackBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();
                    String email = dataSnapshot.child("emailAddress").getValue().toString();
                    String phoneNum = dataSnapshot.child("phoneNumber").getValue().toString();
                    String profileStat = dataSnapshot.child("profileStatus").getValue().toString();
                    String profilePicture = dataSnapshot.child("profileImage").getValue().toString();

                    if (! profilePicture.equals("-1"))
                        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_baseline_person_75).into(profileImage);

                    name.setText(firstName + " " + lastName);
                    emailAddress.setText(email);

                    phoneNumber.setText(phoneNum);
                    profileStatus.setText(profileStat);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendsReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    String displayText = "Friends (" + countFriends + ")";
                    friends.setText(displayText);
                }

                else
                    friends.setText("Friends (0)");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPostsReference.orderByChild("userID")
                .startAt(receiverUserID).endAt(receiverUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            String displayText = "Posts (" + countPosts + ")";
                            posts.setText(displayText);
                        }

                        else
                            posts.setText("Posts (0)");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage;
        public TextView firstName;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.friendsProfileImage);
            firstName = itemView.findViewById(R.id.friendsFirstName);
        }
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
}