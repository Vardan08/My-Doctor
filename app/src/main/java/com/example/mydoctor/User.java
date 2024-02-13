package com.example.mydoctor;

public class User {
    private String fullName;
    private String email;
    private String mobileNumber;
    private String location;
    private String password;
    private String roll;


    public User() {}

    public User(String fullName, String email, String mobileNumber, String location, String password, String roll) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.location = location;
        this.password = password;
        this.roll = roll;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }
}
