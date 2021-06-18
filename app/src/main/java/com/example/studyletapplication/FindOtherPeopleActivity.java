package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindOtherPeopleActivity extends AppCompatActivity {

    private EditText searchBar;
    private ImageButton backButton;
    private RecyclerView listOfPeople;
    private String senderUserID, receiverUserID, currentState, saveCurrentDate, senderFirstName, receiverFirstName;
    private TextView noResultsFound;

    private DatabaseReference mUsersReference, mFriendsReference, mFriendRequestsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_other_people);

        initializeVariables();

        searchForPeople();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForPeople();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchForPeople() {
        FirebaseRecyclerOptions<People> findPeopleFirebaseRecyclerOptions;

        if (searchBar.getText().toString().equals("")) {
            findPeopleFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<People>()
                    .setQuery(mUsersReference, People.class)
                    .build();
        }

        else {
            findPeopleFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<People>()
                    .setQuery(mUsersReference.orderByChild("firstName").startAt(searchBar.getText().toString()).endAt(searchBar.getText().toString() + "\uf8ff"), People.class)
                    .build();
        }

        Query searchFriendsQuery = mUsersReference.orderByChild("firstName")
                .startAt(searchBar.getText().toString()).endAt(searchBar.getText().toString() + "\uf8ff");

        searchFriendsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    noResultsFound.setVisibility(View.GONE);

                else
                    noResultsFound.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerAdapter<People, FindPeopleViewHolder> findPeopleFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<People, FindPeopleViewHolder>(findPeopleFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FindPeopleViewHolder findPeopleViewHolder, final int position, @NonNull People people) {
                if (! senderUserID.equals(getRef(position).getKey())) {
                    findPeopleViewHolder.personFullName.setText(people.getFirstName() + " " + people.getLastName());
                    findPeopleViewHolder.personUsername.setText("@" + people.getUsername());

                    if (! people.getProfileImage().equals("-1"))
                        Picasso.get().load(people.getProfileImage()).placeholder(R.drawable.ic_baseline_person_black_250).into(findPeopleViewHolder.personProfileImage);
                }

                else {
                    findPeopleViewHolder.itemView.setVisibility(View.GONE);
                    findPeopleViewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

                findPeopleViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserID = getRef(position).getKey();
                        Intent popupIntent = new Intent(FindOtherPeopleActivity.this, PopupActivity.class);
                        popupIntent.putExtra("visitUserID", visitUserID);
                        startActivity(popupIntent);
                    }
                });

                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        receiverUserID = getRef(position).getKey();

                        if (currentState.equals("not_friends"))
                            sendFriendRequestToAPerson(findPeopleViewHolder);

                        if (currentState.equals("request_sent"))
                            cancelFriendRequest(findPeopleViewHolder);

                        if (currentState.equals("request_received"))
                            acceptFriendRequest(findPeopleViewHolder);

                        if (currentState.equals("friends"))
                            unfriendAnExistingFriend(findPeopleViewHolder);

                    }
                });

                mFriendRequestsReference.child(senderUserID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(getRef(position).getKey())) {
                                    String requestType = dataSnapshot.child(getRef(position).getKey()).child("requestType").getValue().toString();

                                    if (requestType.equals("sent")) {
                                        currentState = "request_sent";
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Cancel Request");
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_red));
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_cancel_schedule_send_12, 0, 0, 0);
                                    }

                                    else if (requestType.equals("received")) {
                                        currentState = "request_received";
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Accept Request");
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_green));
                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_check_12, 0, 0, 0);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                mFriendsReference.child(senderUserID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(getRef(position).getKey())) {
                                    currentState = "friends";
                                    findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Unfriend?");
                                    findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_red));
                                    findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_clear_12, 0, 0, 0);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @NonNull
            @Override
            public FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_other_people_layout_of_users, parent, false);
                return new FindPeopleViewHolder(view);
            }
        };

        listOfPeople.setAdapter(findPeopleFirebaseRecyclerAdapter);
        findPeopleFirebaseRecyclerAdapter.startListening();
    }

    private void sendFriendRequestToAPerson(final FindPeopleViewHolder findPeopleViewHolder) {
        mFriendRequestsReference.child(senderUserID).child(receiverUserID)
                .child("requestType").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendRequestsReference.child(receiverUserID).child(senderUserID)
                                    .child("requestType").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentState = "request_sent";
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Cancel Request");
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_red));
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_cancel_schedule_send_12, 0, 0, 0);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest(final FindPeopleViewHolder findPeopleViewHolder) {
        mFriendRequestsReference.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendRequestsReference.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                currentState = "not_friends";
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Send Request");
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_green));
                                                findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_send_12, 0, 0, 0);
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest(final FindPeopleViewHolder findPeopleViewHolder) {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        mUsersReference.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    senderFirstName = dataSnapshot.child("firstName").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    receiverFirstName = dataSnapshot.child("firstName").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendsReference.child(senderUserID).child(receiverUserID).child("firstName").setValue(senderFirstName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendsReference.child(receiverUserID).child(senderUserID).child("firstName").setValue(receiverFirstName)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mFriendRequestsReference.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mFriendRequestsReference.child(senderUserID).child(receiverUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        currentState = "friends";
                                                                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Unfriend?");
                                                                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_clear_12, 0, 0, 0);
                                                                                        findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_red));
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void unfriendAnExistingFriend(final FindPeopleViewHolder findPeopleViewHolder) {
        mFriendsReference.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendsReference.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            currentState = "not_friends";
                                            findPeopleViewHolder.sendOrCancelFriendRequestBtn.setText("Send Request");
                                            findPeopleViewHolder.sendOrCancelFriendRequestBtn.setBackground(getResources().getDrawable(R.drawable.button_background_green));
                                            findPeopleViewHolder.sendOrCancelFriendRequestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_send_12, 0, 0, 0);
                                        }
                                    });
                        }
                    }
                });
    }

    private void initializeVariables() {
        searchBar = findViewById(R.id.editTextSetupSearchPeople);
        backButton = findViewById(R.id.findOtherPeopleBackBtn);
        listOfPeople = findViewById(R.id.listOfPeople);
        listOfPeople.setLayoutManager(new LinearLayoutManager(FindOtherPeopleActivity.this));
        senderUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentState = "not_friends";
        noResultsFound = findViewById(R.id.findOtherPeopleNoResultsFound);

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
    }

    public static class FindPeopleViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView personProfileImage;
        private CircularProgressButton sendOrCancelFriendRequestBtn;
        private TextView personFullName, personUsername;

        public FindPeopleViewHolder(View itemView) {
            super(itemView);

            personProfileImage = itemView.findViewById(R.id.allUsersProfileImage);
            sendOrCancelFriendRequestBtn = itemView.findViewById(R.id.sendOrDeclineFriendRequestBtn);
            personFullName = itemView.findViewById(R.id.allUsersFullName);
            personUsername = itemView.findViewById(R.id.allUsernames);
        }
    }
}