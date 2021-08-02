package com.example.studyletapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private FirebaseAuth mAuth;
    private List<Comments> commentsList;

    public CommentsAdapter(List<Comments> commentsList) {
        this.commentsList = commentsList;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImage;
        private TextView name, comment, dateAndTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.all_comments_profile_image);
            name = itemView.findViewById(R.id.all_comments_full_name);
            comment = itemView.findViewById(R.id.all_comments);
            dateAndTime = itemView.findViewById(R.id.commentsDateAndTime);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder commentsViewHolder, int position) {
        Comments comment = commentsList.get(position);

        commentsViewHolder.name.setText(comment.getFirstName() + " " + comment.getLastName());
        commentsViewHolder.comment.setText(comment.getComment());

        if (! comment.getProfileImage().equals("-1"))
            Picasso.get().load(comment.getProfileImage()).into(commentsViewHolder.profileImage);

        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterdayDate = dateFormat.format(calendar.getTime());

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

        if (comment.getDate().equals(currentDate))
            commentsViewHolder.dateAndTime.setText(comment.getTime());

        else if (comment.getDate().equals(yesterdayDate))
            commentsViewHolder.dateAndTime.setText("yesterday\n" + comment.getTime());

        else {
            try {
                if (isDateInCurrentWeek(dateFormat.parse(comment.getDate()))) {
                    Date date = dateFormat.parse(comment.getDate());
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("EEE");
                    commentsViewHolder.dateAndTime.setText(dateFormat2.format(date));
                }

                else
                    commentsViewHolder.dateAndTime.setText(comment.getDate());
            }

            catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
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
