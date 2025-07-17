package com.example.projectict602;

public class Usermodel {
    public String email;
    public String fullname;
    public String nickname;
    public String dateOfBirth;

    public Usermodel() {
        // Required for Firebase deserialization
    }

    public Usermodel(String email, String fullname, String nickname, String dateOfBirth) {
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
    }
}
