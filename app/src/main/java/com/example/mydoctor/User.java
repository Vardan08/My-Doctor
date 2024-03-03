package com.example.mydoctor;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
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

    protected User(Parcel in) {
        fullName = in.readString();
        email = in.readString();
        mobileNumber = in.readString();
        location = in.readString();
        password = in.readString();
        roll = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(mobileNumber);
        dest.writeString(location);
        dest.writeString(password);
        dest.writeString(roll);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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
