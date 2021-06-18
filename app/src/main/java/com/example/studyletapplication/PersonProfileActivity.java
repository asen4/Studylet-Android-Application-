package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference, mFriendsReference;
    private String currentUserID;

    private CircleImageView profileImage;
    private ImageButton backButton;
    private TextView name, phoneNumber, emailAddress, profileStatus;
    private RecyclerView listOfFriends, listOfPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        initializeVariables();
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        profileImage = findViewById(R.id.personProfileImage);
        profileImage.bringToFront();
        profileImage.requestLayout();

        name = findViewById(R.id.personProfileFullName);
        phoneNumber = findViewById(R.id.personProfilePhoneNumber);
        emailAddress = findViewById(R.id.personProfileEmailAddress);
        profileStatus = findViewById(R.id.personProfileStatus);

        listOfFriends = findViewById(R.id.personProfileListOfFriends);
        listOfFriends.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this, LinearLayoutManager.HORIZONTAL, false));

        listOfPosts = findViewById(R.id.personProfileListOfPosts);
        listOfPosts.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this, LinearLayoutManager.VERTICAL, false));

        backButton = findViewById(R.id.personProfileBackBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();
                    String phoneNum = dataSnapshot.child("phoneNumber").getValue().toString();
                    String profileStat = dataSnapshot.child("profileStatus").getValue().toString();
                    String profilePicture = dataSnapshot.child("profileImage").getValue().toString();

                    if (! profilePicture.equals("-1"))
                        Picasso.get().load(profilePicture).placeholder(R.drawable.ic_baseline_person_75).into(profileImage);

                    name.setText(firstName + " " + lastName);

                    if (mAuth.getCurrentUser() != null)
                        emailAddress.setText(mAuth.getCurrentUser().getEmail());

                    phoneNumber.setText(phoneNum);
                    profileStatus.setText(profileStat);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Friends> friendsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mUsersReference, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(friendsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull final Friends friends) {
                final String receiverUserID = getRef(position).getKey();

                if (! currentUserID.equals(receiverUserID)) {
                    mFriendsReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserID)) {
                                if (! dataSnapshot.child(currentUserID).hasChild(receiverUserID)) {
                                    ViewGroup.LayoutParams layoutParams = friendsViewHolder.itemView.getLayoutParams();
                                    layoutParams.height = 0;
                                    layoutParams.width = 0;
                                    friendsViewHolder.itemView.setLayoutParams(layoutParams);
                                }

                                else {
                                    friendsViewHolder.firstName.setText(friends.getFirstName());

                                    if (! friends.getProfileImage().equals("-1"))
                                        Picasso.get().load(friends.getProfileImage()).placeholder(R.drawable.ic_baseline_person_black_250).into(friendsViewHolder.profileImage);
                                }
                            }

                            else {
                                ViewGroup.LayoutParams layoutParams = friendsViewHolder.itemView.getLayoutParams();
                                layoutParams.height = 0;
                                layoutParams.width = 0;
                                friendsViewHolder.itemView.setLayoutParams(layoutParams);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                else {
                    ViewGroup.LayoutParams layoutParams = friendsViewHolder.itemView.getLayoutParams();
                    layoutParams.height = 0;
                    layoutParams.width = 0;
                    friendsViewHolder.itemView.setLayoutParams(layoutParams);
                }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private TextView firstName;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.friendsProfileImage);
            firstName = itemView.findViewById(R.id.friendsFirstName);
        }


    }
}