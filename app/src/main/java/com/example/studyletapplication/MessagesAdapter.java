package com.example.studyletapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private Context context;
    private FirebaseAuth mAuth;
    private List<Messages> messagesList;

    public MessagesAdapter (List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout senderMessageContainer, receiverMessageContainer;
        private ImageView senderMessagePicture, receiverMessagePicture;
        private TextView senderMessageText, senderMessageTimeSent, receiverMessageText, receiverMessageTimeSent, dateContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageContainer = itemView.findViewById(R.id.senderMessageContainer);
            receiverMessageContainer = itemView.findViewById(R.id.receiverMessageContainer);

            senderMessageText = itemView.findViewById(R.id.senderMessageText);
            senderMessageTimeSent = itemView.findViewById(R.id.senderMessageTimeSent);
            receiverMessageText = itemView.findViewById(R.id.receiverMessageText);
            senderMessagePicture = itemView.findViewById(R.id.senderMessageImage);
            receiverMessagePicture = itemView.findViewById(R.id.receiverMessageImage);
            receiverMessageTimeSent = itemView.findViewById(R.id.receiverMessageTimeSent);
            dateContainer = itemView.findViewById(R.id.messagesDividerLine);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_users, parent, false);
        context = parent.getContext();
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messagesViewHolder, final int position) {
        String senderUserID = mAuth.getCurrentUser().getUid();

        final Messages messages = messagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        if (position == 0) {
            messagesViewHolder.dateContainer.setVisibility(View.VISIBLE);

            DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            String yesterdayDate = dateFormat.format(calendar.getTime());

            String currentDate = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(new Date());

            if (messages.getDate().equals(currentDate))
                messagesViewHolder.dateContainer.setText("TODAY");

            else if (messages.getDate().equals(yesterdayDate))
                messagesViewHolder.dateContainer.setText("YESTERDAY");

            else {
                try {
                    if (isDateInCurrentWeek(dateFormat.parse(messages.getDate()))) {
                        Date date = dateFormat.parse(messages.getDate());
                        SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                        messagesViewHolder.dateContainer.setText(dateFormat2.format(date));
                    }

                    else
                        messagesViewHolder.dateContainer.setText(messages.getDate());

                }

                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        else {
            messagesViewHolder.dateContainer.setVisibility(View.GONE);
        }

        if (position > 1) {
            if (! messagesList.get(position).getDate().equals(messagesList.get(position - 1).getDate())) {
                messagesViewHolder.dateContainer.setVisibility(View.VISIBLE);

                DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -1);
                String yesterdayDate = dateFormat.format(calendar.getTime());

                String currentDate = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(new Date());

                if (messages.getDate().equals(currentDate))
                    messagesViewHolder.dateContainer.setText("TODAY");

                else if (messages.getDate().equals(yesterdayDate))
                    messagesViewHolder.dateContainer.setText("YESTERDAY");

                else {
                    try {
                        if (isDateInCurrentWeek(dateFormat.parse(messages.getDate()))) {
                            Date date = dateFormat.parse(messages.getDate());
                            SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEEE");
                            messagesViewHolder.dateContainer.setText(dateFormat2.format(date));
                        }

                        else
                            messagesViewHolder.dateContainer.setText(messages.getDate());

                    }

                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            else {
                messagesViewHolder.dateContainer.setVisibility(View.GONE);
            }
        }

        messagesViewHolder.receiverMessageContainer.setVisibility(View.GONE);
        messagesViewHolder.receiverMessageText.setVisibility(View.GONE);
        messagesViewHolder.receiverMessagePicture.setVisibility(View.GONE);
        messagesViewHolder.receiverMessageTimeSent.setVisibility(View.GONE);

        messagesViewHolder.senderMessageContainer.setVisibility(View.GONE);
        messagesViewHolder.senderMessageText.setVisibility(View.GONE);
        messagesViewHolder.senderMessagePicture.setVisibility(View.GONE);
        messagesViewHolder.senderMessageTimeSent.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(senderUserID)) {
                messagesViewHolder.senderMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessageTimeSent.setVisibility(View.VISIBLE);

                messagesViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messagesViewHolder.senderMessageText.setText(messages.getMessage());
                messagesViewHolder.senderMessageTimeSent.setText(messages.getTime());
            }

            else {
                messagesViewHolder.receiverMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageTimeSent.setVisibility(View.VISIBLE);

                messagesViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messagesViewHolder.receiverMessageText.setText(messages.getMessage());
                messagesViewHolder.receiverMessageTimeSent.setText(messages.getTime());
            }
        }

        else if (fromMessageType.equals("image")) {
            messagesViewHolder.receiverMessageText.setVisibility(View.GONE);
            messagesViewHolder.senderMessageText.setVisibility(View.GONE);

            if (fromUserID.equals(senderUserID)) {
                messagesViewHolder.senderMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessageTimeSent.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessagePicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messagesViewHolder.senderMessagePicture);
                messagesViewHolder.senderMessageTimeSent.setText(messages.getTime());

                messagesViewHolder.senderMessagePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messagesViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("URL", messagesList.get(position).getMessage());
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }

            else {
                messagesViewHolder.receiverMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageTimeSent.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessagePicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messagesViewHolder.receiverMessagePicture);
                messagesViewHolder.receiverMessageTimeSent.setText(messages.getTime());

                messagesViewHolder.receiverMessagePicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(messagesViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        intent.putExtra("URL", messagesList.get(position).getMessage());
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }

        else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(senderUserID)) {
                messagesViewHolder.senderMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessagePicture.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessagePicture.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);
                messagesViewHolder.senderMessageTimeSent.setVisibility(View.VISIBLE);
                messagesViewHolder.senderMessageTimeSent.setText(messages.getTime());

                messagesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }

            else {
                messagesViewHolder.receiverMessageContainer.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessagePicture.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessagePicture.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);
                messagesViewHolder.receiverMessageTimeSent.setVisibility(View.VISIBLE);
                messagesViewHolder.receiverMessageTimeSent.setText(messages.getTime());

                messagesViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messagesList.get(position).getMessage()));
                        messagesViewHolder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        
        if (fromUserID.equals(senderUserID)) {
            messagesViewHolder.senderMessageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("docx")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSentMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessageForEveryone(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }

                    else if (messagesList.get(position).getType().equals("text")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSentMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessageForEveryone(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }

                    else if (messagesList.get(position).getType().equals("image")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteSentMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessageForEveryone(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }
                }
            });
        }

        else {
            messagesViewHolder.receiverMessageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messagesList.get(position).getType().equals("pdf") || messagesList.get(position).getType().equals("docx")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReceivedMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }

                    else if (messagesList.get(position).getType().equals("text")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReceivedMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }

                    else if (messagesList.get(position).getType().equals("image")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messagesViewHolder.itemView.getContext());
                        final AlertDialog alertDialog = builder.create();
                        builder.setTitle("Delete message?");
                        builder.setMessage("This action cannot be undone.");

                        builder.setPositiveButton("DELETE FOR ME", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteReceivedMessage(position, messagesViewHolder);

                                Intent intent = new Intent(messagesViewHolder.itemView.getContext(), MainActivity.class);
                                messagesViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });

                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public List<Messages> getMessages() {
        return messagesList;
    }

    private void deleteSentMessage(final int position, final MessageViewHolder messageViewHolder) {
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        mRootReference.child("Messages")
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getRecipient())
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    messagesList.remove(messagesList.get(position));
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Your message was successfully deleted!", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error! Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder messageViewHolder) {
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        mRootReference.child("Messages")
                .child(messagesList.get(position).getRecipient())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    messagesList.remove(messagesList.get(position));
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Your message was successfully deleted!", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error! Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder messageViewHolder) {
        final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        mRootReference.child("Messages")
                .child(messagesList.get(position).getRecipient())
                .child(messagesList.get(position).getFrom())
                .child(messagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRootReference.child("Messages")
                            .child(messagesList.get(position).getFrom())
                            .child(messagesList.get(position).getRecipient())
                            .child(messagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                messagesList.remove(messagesList.get(position));
                                Toast.makeText(messageViewHolder.itemView.getContext(), "Your message was successfully deleted!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error! Please try again!", Toast.LENGTH_SHORT).show();
                }
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
}
