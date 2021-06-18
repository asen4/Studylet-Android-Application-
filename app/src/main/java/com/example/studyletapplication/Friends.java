package com.example.studyletapplication;

public class Friends {

    public String profileImage, firstName;

    public Friends() {

    }

    public Friends(String profileImage, String firstName) {
        this.profileImage = profileImage;
        this.firstName = firstName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setUsername(String firstName) {
        this.firstName = firstName;
    }
}
