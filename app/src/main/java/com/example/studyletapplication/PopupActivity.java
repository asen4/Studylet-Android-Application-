package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
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

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PopupActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private DatabaseReference mUsersReference, mFriendsReference;
    private FirebaseAuth mAuth;
    private RecyclerView listOfFriends;
    private ImageButton backButton, forwardButton, cancelButton;
    private TextView name, phoneNumber, emailAddress, profileStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        initializeVariables();

        String visitUserID = getIntent().getStringExtra("visitUserID");
        mUsersReference.child(visitUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name.setText(dataSnapshot.child("firstName").getValue().toString() + " " + dataSnapshot.child("lastName").getValue().toString());
                    phoneNumber.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                    profileStatus.setText(dataSnapshot.child("profileStatus").getValue().toString());
                    emailAddress.setText(mAuth.getCurrentUser().getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<People> friendsFirebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<People>()
                        .setQuery(mFriendsReference, People.class)
                        .build();

        FirebaseRecyclerAdapter<People, FriendsViewHolder> friendsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<People, FriendsViewHolder>(friendsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder friendsViewHolder, int position, @NonNull People people) {
                if (! people.getProfileImage().equals("-1")) {
                    Picasso.get().load(people.getProfileImage()).into(friendsViewHolder.friendProfileImage);
                }

                friendsViewHolder.friendName.setText(people.getFirstName() + " " + people.getLastName());
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_items, parent, false);
                return new FriendsViewHolder(view);
            }
        };
    }

    private void initializeVariables() {
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.92), (int) (height * 0.85));

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        // layoutParams.x = 0;
        // layoutParams.y = 0;

        getWindow().setAttributes(layoutParams);

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.popupName);
        phoneNumber = findViewById(R.id.popupPhoneNumber);
        emailAddress = findViewById(R.id.popupEmailAddress);
        profileStatus = findViewById(R.id.popupProfileStatus);

        backButton = findViewById(R.id.popupBackBtn);
        forwardButton = findViewById(R.id.popupForwardBtn);
        cancelButton = findViewById(R.id.popupCancelBtn);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listOfFriends = findViewById(R.id.popupListOfFriends);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(PopupActivity.this, 4);
        listOfFriends.setLayoutManager(layoutManager);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView friendProfileImage;
        TextView friendName;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            friendProfileImage = itemView.findViewById(R.id.sourceFileImage);
            friendName = itemView.findViewById(R.id.sourceFileHeader);
        }

    }
}