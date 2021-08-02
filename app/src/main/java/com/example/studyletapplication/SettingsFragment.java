package com.example.studyletapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
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

public class SettingsFragment extends Fragment {

    private CircleImageView profileImage;
    private FloatingActionButton editPersonProfileButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;
    private String currentUserID;
    private MaterialButton logoutButton, personProfileButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeVariables(view);

        return view;
    }

    private void initializeVariables(View view) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        logoutButton = view.findViewById(R.id.cirLogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserStatus("offline");
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LRContainerActivity.class));
            }
        });

        profileImage = view.findViewById(R.id.settingsHeaderProfileImage);

        editPersonProfileButton = view.findViewById(R.id.fabEditPersonProfile);
        editPersonProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToEditPersonProfileActivity();
            }
        });
        personProfileButton = view.findViewById(R.id.personProfileBtn);
        personProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPersonProfileActivity();
            }
        });

        mUsersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    if (! profileImage.equals("-1"))
                        Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_white_30).into(profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToEditPersonProfileActivity() {
        Intent editPersonProfileIntent = new Intent(getActivity(), EditPersonProfileActivity.class);
        editPersonProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(editPersonProfileIntent);
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

    private void sendUserToPersonProfileActivity() {
        Intent personProfileIntent = new Intent(getActivity(), PersonProfileActivity.class);
        personProfileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        personProfileIntent.putExtra("visitUserID", currentUserID);
        startActivity(personProfileIntent);
    }
}