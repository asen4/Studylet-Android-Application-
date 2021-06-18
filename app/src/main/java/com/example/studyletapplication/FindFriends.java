package com.example.studyletapplication;

public class FindFriends {

    public String profileImage, firstName, lastName, profileStatus;

    public FindFriends() {

    }

    public FindFriends (String profileImage, String firstName, String lastName, String profileStatus) {
        this.profileImage = profileImage;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileStatus = profileStatus;
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

}
