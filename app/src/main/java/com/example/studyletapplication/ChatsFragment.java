package com.example.studyletapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference mUsersReference, mMessagesReference, mFriendsReference;

    private CircleImageView chatsHeaderProfileImage;
    private EditText searchBar;
    private FloatingActionButton findOtherPeopleBtn;
    private ProgressBar loadingBar;
    private RecyclerView listOfFriends;
    private TextView noResultsFound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeVariables(view);

        searchForFriends();

        /*

        mobileScreenShareButton = (FloatingActionButton) view.findViewById(R.id.mobileScreenShareBtn);
        mobileScreenShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("android.settings.CAST_SETTINGS"));
            }
        });

        <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/mobileScreenShareBtn"
        android:src="@drawable/ic_baseline_cast_24"
        android:backgroundTint="@color/colorHotPink"
        android:layout_alignParentBottom="true"
        android:layout_alignParentEnd="true"
        android:layout_marginBottom="75dp"
        android:layout_marginEnd="25dp" />

         */

        return view;
    }

    private void sendUserToFindOtherPeopleActivity() {
        Intent findOtherPeopleIntent = new Intent(getActivity(), FindOtherPeopleActivity.class);
        findOtherPeopleIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(findOtherPeopleIntent);
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        mFriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        chatsHeaderProfileImage = view.findViewById(R.id.chatsHeaderProfileImage);
        searchBar = view.findViewById(R.id.editTextSetupSearchBar);
        findOtherPeopleBtn = view.findViewById(R.id.chatsFindOtherPeople);
        noResultsFound = view.findViewById(R.id.chatsNoResultsFound);
        loadingBar = view.findViewById(R.id.chatsLoadingBar);

        listOfFriends = view.findViewById(R.id.listOfFriends);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        listOfFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (! dataSnapshot.child("profileImage").getValue().equals("-1")) {
                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_person_white_30).into(chatsHeaderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        findOtherPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFindOtherPeopleActivity();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchForFriends();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchForFriends() {
        FirebaseRecyclerOptions<FindFriends> findFriendsFirebaseRecyclerOptions;

        if (searchBar.getText().toString().equals("")) {
            findFriendsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FindFriends>()
                    .setQuery(mUsersReference, FindFriends.class)
                    .build();
        }

        else {
            findFriendsFirebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<FindFriends>()
                    .setQuery(mUsersReference.orderByChild("firstName").startAt(searchBar.getText().toString()).endAt(searchBar.getText().toString() + "\uf8ff"), FindFriends.class)
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

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> findFriendsFirebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(findFriendsFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FindFriendsViewHolder findFriendsViewHolder, final int position, @NonNull final FindFriends findFriends) {
                final String receiverUserID = getRef(position).getKey();

                if (! currentUserID.equals(receiverUserID)) {
                    mFriendsReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserID)) {
                                if (! dataSnapshot.child(currentUserID).hasChild(receiverUserID)) {
                                    ViewGroup.LayoutParams layoutParams = findFriendsViewHolder.itemView.getLayoutParams();
                                    layoutParams.height = 0;
                                    layoutParams.width = 0;
                                    findFriendsViewHolder.itemView.setLayoutParams(layoutParams);
                                }

                                else {
                                    findFriendsViewHolder.friendFullName.setText(findFriends.getFirstName() + " " + findFriends.getLastName());

                                    if (! findFriends.getProfileImage().equals("-1"))
                                        Picasso.get().load(findFriends.getProfileImage()).placeholder(R.drawable.ic_baseline_person_black_250).into(findFriendsViewHolder.friendProfileImage);
                                }
                            }

                            else {
                                ViewGroup.LayoutParams layoutParams = findFriendsViewHolder.itemView.getLayoutParams();
                                layoutParams.height = 0;
                                layoutParams.width = 0;
                                findFriendsViewHolder.itemView.setLayoutParams(layoutParams);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                else {
                    ViewGroup.LayoutParams layoutParams = findFriendsViewHolder.itemView.getLayoutParams();
                    layoutParams.height = 0;
                    layoutParams.width = 0;
                    findFriendsViewHolder.itemView.setLayoutParams(layoutParams);
                }

                findFriendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserID = getRef(position).getKey();
                        Intent messagesIntent = new Intent(getActivity(), MessagesActivity.class);
                        messagesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        messagesIntent.putExtra("visitUserID", visitUserID);
                        startActivity(messagesIntent);
                    }
                });

                mUsersReference.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("userState").child("type").getValue().equals("online"))
                                findFriendsViewHolder.friendOnline.setVisibility(View.VISIBLE);
                            else
                                findFriendsViewHolder.friendOnline.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query lastQuery = mMessagesReference.child(currentUserID).child(receiverUserID).orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String from = childSnapshot.child("from").getValue().toString();
                            final String message = childSnapshot.child("message").getValue().toString();
                            final String type = childSnapshot.child("type").getValue().toString();
                            String introMessage = "You: ";

                            if (from.equals(currentUserID)) {
                                if (type.equals("text")) {
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + message);
                                    StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                    spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                }

                                else if (type.equals("image")) {
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "🖼️ Image");
                                    StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                    spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                }

                                else {
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "📁 File");
                                    StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                    spannableStringBuilder.setSpan(boldStyle, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                }
                            }

                            else {
                                mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String firstName = dataSnapshot.child("firstName").getValue().toString();
                                        String lastName = dataSnapshot.child("lastName").getValue().toString();
                                        String introMessage = firstName + " " + lastName + ": ";

                                        if (type.equals("text")) {
                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + message);
                                            StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                            spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                            findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                        }

                                        else if (type.equals("image")) {
                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "🖼️ Image");
                                            StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                            spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                            findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                        }

                                        else {
                                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(introMessage + "📁 File");
                                            StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
                                            spannableStringBuilder.setSpan(boldStyle, 0, introMessage.indexOf(":"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                            findFriendsViewHolder.friendProfileStatus.setText(spannableStringBuilder);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                loadingBar.setVisibility(View.GONE);
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_layout_of_users, parent, false);
                return new FindFriendsViewHolder(view);
            }
        };

        listOfFriends.setAdapter(findFriendsFirebaseRecyclerAdapter);
        findFriendsFirebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView friendProfileImage;
        private ImageView friendOnline;
        private TextView friendFullName, friendProfileStatus;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);

            friendProfileImage = itemView.findViewById(R.id.all_users_profile_image);
            friendOnline = itemView.findViewById(R.id.ic_all_users_online);
            friendFullName = itemView.findViewById(R.id.all_users_full_name);
            friendProfileStatus = itemView.findViewById(R.id.all_users_status);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateUserStatus("online");
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
}
