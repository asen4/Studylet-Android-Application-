package com.example.studyletapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private boolean likeChecker, dislikeChecker;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAddNewPost;
    private DatabaseReference mUsersReference, mPostReference, mLikesReference, mDislikesReference;
    private ImageView homeHeaderProfileImage;
    private TextView noPostsFoundMessage;
    private ProgressBar loadingBar;
    private RecyclerView postsList;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeVariables(view);

        displayAllUsersPosts();

        mPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    noPostsFoundMessage.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.GONE);
                }

                else {
                    noPostsFoundMessage.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        mLikesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDislikesReference = FirebaseDatabase.getInstance().getReference().child("Dislikes");

        noPostsFoundMessage = view.findViewById(R.id.noPostsFound);

        postsList = view.findViewById(R.id.all_users_post_list);
        postsList.setHasFixedSize(true);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        postsList.setLayoutManager(mLinearLayoutManager);

        loadingBar = view.findViewById(R.id.postsLoadingBar);

        fabAddNewPost = view.findViewById(R.id.fabAddNewPost);

        fabAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        // updateUserStatus("online");

        homeHeaderProfileImage = view.findViewById(R.id.homeHeaderProfileImage);
        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (! dataSnapshot.child("profileImage").getValue().equals("-1")) {
                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_white_30).into(homeHeaderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    private void displayAllUsersPosts() {
        FirebaseRecyclerOptions<Posts> postsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(mPostReference.orderByChild("counter"), Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> postsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(postsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int position, @NonNull final Posts posts) {
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

                if (! posts.getProfileImage().equals("-1"))
                    postsViewHolder.setProfileImage(posts.getProfileImage());

                mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String firstName = dataSnapshot.child("firstName").getValue().toString();
                            String lastName = dataSnapshot.child("lastName").getValue().toString();

                            String temp = firstName + " " + lastName;
                            String fullName = posts.getFirstName() + " " + posts.getLastName();

                            if (temp.equals(fullName)) {
                                postsViewHolder.setFullName("You");
                            }
                            else {
                                postsViewHolder.setFullName(posts.getFirstName() + " " + posts.getLastName());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                postsViewHolder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("URL", posts.getPostImage());
                        startActivity(intent);
                    }
                });

                postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(getActivity(), ClickPostActivity.class);
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
                        Intent commentsIntent = new Intent(getActivity(), CommentsActivity.class);
                        commentsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });

                loadingBar.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                return new PostsViewHolder(view);
            }
        };

        postsList.setAdapter(postsFirebaseRecyclerAdapter);
        postsFirebaseRecyclerAdapter.startListening();
    }

    private void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
            currentStateMap.put("time", saveCurrentTime);
            currentStateMap.put("date", saveCurrentDate);
            currentStateMap.put("type", state);

        mUsersReference.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }

    private void sendUserToPostActivity() {
        Intent postIntent = new Intent(getActivity(), PostActivity.class);
        postIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(postIntent);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout linearLayoutLikePostButton, linearLayoutDislikePostButton, linearLayoutCommentPostButton;
        public ImageView likeIcon, dislikeIcon, profileImage, postImage;
        public TextView postName, displayNumberOfLikes, displayNumberOfDislikes;
        public int countNumberOfLikes, countNumberOfDislikes;
        public String currentUserID;
        public DatabaseReference mLikesReference, mDislikesReference;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayoutLikePostButton = itemView.findViewById(R.id.likeButton);
            linearLayoutDislikePostButton = itemView.findViewById(R.id.dislikeButton);
            linearLayoutCommentPostButton = itemView.findViewById(R.id.commentButton);
            displayNumberOfLikes = itemView.findViewById(R.id.displayNumberOfLikes);
            displayNumberOfDislikes = itemView.findViewById(R.id.displayNumberOfDislikes);
            likeIcon = itemView.findViewById(R.id.likeImageButton);
            dislikeIcon = itemView.findViewById(R.id.dislikeImageButton);
            postName = itemView.findViewById(R.id.postName);
            profileImage = itemView.findViewById(R.id.postProfileImage);
            postImage = itemView.findViewById(R.id.postImage);

            mLikesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
            mDislikesReference = FirebaseDatabase.getInstance().getReference().child("Dislikes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus (final String postKey) {
            mLikesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                        countNumberOfLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeIcon.setImageResource(R.drawable.ic_baseline_thumb_up_20);
                        displayNumberOfLikes.setText("Likes (" + countNumberOfLikes + ")");
                    }

                    else {
                        countNumberOfLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeIcon.setImageResource(R.drawable.ic_outline_thumb_up_20);
                        displayNumberOfLikes.setText("Likes (" + countNumberOfLikes + ")");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setDislikeButtonStatus (final String postKey) {
            mDislikesReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserID)) {
                        countNumberOfDislikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        dislikeIcon.setImageResource(R.drawable.ic_baseline_thumb_down_20);
                        displayNumberOfDislikes.setText("Dislikes (" + countNumberOfDislikes + ")");
                    }

                    else {
                        countNumberOfDislikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        dislikeIcon.setImageResource(R.drawable.ic_outline_thumb_down_20);
                        displayNumberOfDislikes.setText("Dislikes (" + countNumberOfDislikes + ")");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setPostTitle (String postTitle) {
            TextView title = itemView.findViewById(R.id.postTitle);
            title.setText(postTitle);
        }

        public void setFullName (String fullName) {
            TextView name = itemView.findViewById(R.id.postName);
            name.setText(fullName);
        }

        public void setProfileImage (String profileImage) {
            CircleImageView image = itemView.findViewById(R.id.postProfileImage);
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_60).into(image);
        }

        public void setDateAndTime (String dateAndTime) {
            TextView postDateAndTime = itemView.findViewById(R.id.postDateAndTime);
            postDateAndTime.setText(dateAndTime);
        }

        public void setPostDescription(String postDescription) {
            TextView description = itemView.findViewById(R.id.postDescription);
            description.setText(postDescription);
        }

        public void setPostImage (String postImage) {
            ImageView image = itemView.findViewById(R.id.postImage);
            Picasso.get().load(postImage).placeholder(R.drawable.ic_baseline_image_150).into(image);
        }
    }
}