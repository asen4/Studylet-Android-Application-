package com.example.studyletapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mRootReference, mUsersReference;

    private ArrayList optionsList = new ArrayList<>();
    private final List<Messages> messagesList = new ArrayList<>();

    private Button backBtn;
    private CircleImageView receiverProfileImageCIV;
    private EditText userMessageET;
    private GridView addFileOptions;
    private TextView receiverNameTV, receiverLastSeenTV;

    private ImageButton sendMessageButton, addFileButton;
    private ImageButton backButton;

    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private ProgressBar loadingBar;
    private RelativeLayout textContainer;
    private RecyclerView messagesListRV;
    private Uri fileUri;

    private StorageTask uploadTask;
    private String receiverUserID, senderUserID, saveCurrentDate, saveCurrentTime, myURL="", fileType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initializeVariables();

        displayReceiverInformation();

        fetchMessages();

        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContainer.setVisibility(View.GONE);
                backBtn.setVisibility(View.VISIBLE);
                addFileOptions.setVisibility(View.VISIBLE);
                closeKeyboard(addFileOptions);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textContainer.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.GONE);
                addFileOptions.setVisibility(View.GONE);
            }
        });

        addFileOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent.createChooser(intent, "Select Document"), 438);
                    fileType = "docx";
                }

                else if (position == 1) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                    fileType = "image";
                }

                else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                    fileType = "pdf";
                }
            }
        });
    }

    private void closeKeyboard(GridView gridView) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(gridView.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadingBar.setVisibility(View.VISIBLE);
            fileUri = data.getData();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            if (! fileType.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderReference = "Messages/" + senderUserID + "/" + receiverUserID;
                final String messageReceiverReference = "Messages/" + receiverUserID + "/" + senderUserID;

                DatabaseReference userMessageKey = mRootReference.child("Messages").child(senderUserID)
                        .child(receiverUserID).push();

                final String messagePushID = userMessageKey.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + fileType);

                textContainer.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.GONE);
                addFileOptions.setVisibility(View.GONE);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map messageTextBody = new HashMap();
                                messageTextBody.put("message", task.getResult().getDownloadUrl().toString());
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("date", saveCurrentDate);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("type", fileType);
                                messageTextBody.put("from", senderUserID);
                                messageTextBody.put("recipient", receiverUserID);

                            Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageTextBody);

                            mRootReference.updateChildren(messageBodyDetails);
                            loadingBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.setVisibility(View.GONE);
                        Toast.makeText(MessagesActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderReference = "Messages/" + senderUserID + "/" + receiverUserID;
                final String messageReceiverReference = "Messages/" + receiverUserID + "/" + senderUserID;

                DatabaseReference userMessageKey = mRootReference.child("Messages").child(senderUserID)
                        .child(receiverUserID).push();

                final String messagePushID = userMessageKey.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + ".jpg");

                textContainer.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.GONE);
                addFileOptions.setVisibility(View.GONE);

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (! task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadURL = task.getResult();
                            myURL = downloadURL.toString();

                            Map messageImageBody = new HashMap();
                                messageImageBody.put("message", myURL);
                                messageImageBody.put("messageID", messagePushID);
                                messageImageBody.put("name", fileUri.getLastPathSegment());
                                messageImageBody.put("date", saveCurrentDate);
                                messageImageBody.put("time", saveCurrentTime);
                                messageImageBody.put("type", "image");
                                messageImageBody.put("from", senderUserID);
                                messageImageBody.put("recipient", receiverUserID);

                            Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageImageBody);
                                messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageImageBody);

                            mRootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (! task.isSuccessful()) {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(MessagesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }

                                    userMessageET.setText("");
                                    loadingBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        userMessageET = findViewById(R.id.inputMessage);

        receiverProfileImageCIV = findViewById(R.id.messagesProfileImage);
        receiverNameTV = findViewById(R.id.messagesFullName);
        receiverLastSeenTV = findViewById(R.id.messagesLastSeen);
        backButton = findViewById(R.id.messagesBackBtn);
        addFileButton = findViewById(R.id.addFileBtn);

        loadingBar = findViewById(R.id.messagesLoadingBar);

        TypedValue addFileValue = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, addFileValue, true);
        addFileButton.setBackgroundResource(addFileValue.resourceId);
        addFileButton.setBackground(getResources().getDrawable(R.drawable.hot_pink_round_button));

        sendMessageButton = findViewById(R.id.sendMessageBtn);
        sendMessageButton.setEnabled(false);

        backBtn = findViewById(R.id.backBtn);
        textContainer = findViewById(R.id.myRelativeLayout);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        messagesAdapter = new MessagesAdapter(messagesList);
        messagesListRV = findViewById(R.id.messagesListOfUsers);
        linearLayoutManager = new LinearLayoutManager(MessagesActivity.this);
        messagesListRV.setAdapter(messagesAdapter);
        messagesListRV.setHasFixedSize(true);
        messagesListRV.setLayoutManager(linearLayoutManager);

        receiverUserID = getIntent().getExtras().get("visitUserID").toString();

        addFileOptions = findViewById(R.id.addFileOptions);
        optionsList.add(new Item("Documents", R.drawable.ic_baseline_insert_drive_file_24));
        optionsList.add(new Item("Images", R.drawable.ic_baseline_image_24));
        optionsList.add(new Item("PDFs", R.drawable.ic_baseline_picture_as_pdf_24));

        FileOptionsAdapter fileOptionsAdapter = new FileOptionsAdapter(MessagesActivity.this, R.layout.grid_view_items, optionsList);
        addFileOptions.setAdapter(fileOptionsAdapter);

        userMessageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    sendMessageButton.setBackground(getResources().getDrawable(R.drawable.gray_round_button));
                    sendMessageButton.setEnabled(false);
                }

                else {
                    sendMessageButton.setBackground(getResources().getDrawable(R.drawable.round_button));
                    sendMessageButton.setEnabled(true);
                }

                int height = userMessageET.getHeight();

                if (height >= 200) {
                    userMessageET.setHeight(200);
                    userMessageET.setVerticalScrollBarEnabled(true);
                    userMessageET.setMovementMethod(new ScrollingMovementMethod());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void displayReceiverInformation() {
        mUsersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileImage")) {
                        String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                        if (! profileImage.equals("-1")) {
                            Picasso.get().load(profileImage).into(receiverProfileImageCIV);
                        }
                    }

                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();

                    receiverNameTV.setText(firstName + " " + lastName);

                    String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    String lastSeenDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    String lastSeenTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (type.equals("online")) {
                        receiverLastSeenTV.setText("online");
                    }

                    else {
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -1);
                        String yesterdayDate = dateFormat.format(calendar.getTime());

                        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                        if (lastSeenDate.equals(currentDate))
                            receiverLastSeenTV.setText("last active today at " + lastSeenTime);

                        else if (lastSeenDate.equals(yesterdayDate))
                            receiverLastSeenTV.setText("last active yesterday at " + lastSeenTime);

                        else {
                            try {
                                if (isDateInCurrentWeek(dateFormat.parse(lastSeenDate))) {
                                    Date date = dateFormat.parse(lastSeenDate);
                                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                                    receiverLastSeenTV.setText("last active on " + dateFormat2.format(date));
                                }

                                else
                                    receiverLastSeenTV.setText("last active on " + lastSeenDate);

                            }

                            catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
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

    private void fetchMessages() {
        mRootReference.child("Messages").child(senderUserID).child(receiverUserID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                            messagesListRV.smoothScrollToPosition(messagesListRV.getAdapter().getItemCount());
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

    private void sendMessage() {
        updateUserStatus("online");

        String messageText = userMessageET.getText().toString();

        String messageSenderReference = "Messages/" + senderUserID + "/" + receiverUserID;
        String messageReceiverReference = "Messages/" + receiverUserID + "/" + senderUserID;

        DatabaseReference userMessageKey = mRootReference.child("Messages").child(senderUserID)
                .child(receiverUserID).push();
        String messagePushID = userMessageKey.getKey();

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm aa");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", senderUserID);
            messageTextBody.put("recipient", receiverUserID);

        Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderReference + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverReference + "/" + messagePushID, messageTextBody);

        mRootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (! task.isSuccessful()) {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(MessagesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }

                userMessageET.setText("");
            }
        });
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

        mUsersReference.child(senderUserID).child("userState").updateChildren(currentStateMap);
    }
}