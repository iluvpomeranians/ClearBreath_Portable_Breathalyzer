package com.example.coen390androidproject_breathalyzerapp;
import java.io.Serializable;

public class AccountInfo implements Serializable {

    //GETTERS AND SETTERS FOR ACCOUNT INFO

    private int id;
    private String fullName;
    private String username;
    private String password;
    private String gender;
    private int age;
    private String email;
    private double bmi;

    public AccountInfo(int id, String fullName, String username, String password, String gender, int age, String email, double bmi) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.bmi = bmi;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public String getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public double getBmi() {
        return bmi;
    }
}
