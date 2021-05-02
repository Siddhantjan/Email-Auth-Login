package com.example.taskappforintern;

public class UserData {
    private String userID;
    private String Name;
    private String Email;
    private String imageURI;

    public UserData() {
    }

    public UserData(String userID, String name, String email, String imageURI) {
        this.userID = userID;
        Name = name;
        Email = email;
        this.imageURI = imageURI;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

}

