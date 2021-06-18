package com.example.studyletapplication;

public class Posts {
    private String userID, time,date, postTitle, postImage, postDescription, profileImage, firstName, lastName;

    public Posts() {

    }

    public Posts (String userID, String time, String date, String postTitle, String postImage, String postDescription, String profileImage, String firstName, String lastName) {
        this.userID = userID;
        this.time = time;
        this.date = date;
        this.postTitle = postTitle;
        this.postImage = postImage;
        this.postDescription = postDescription;
        this.profileImage = profileImage;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getUserID() {
        return userID;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getPostImage() {
        return postImage;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date =date;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}