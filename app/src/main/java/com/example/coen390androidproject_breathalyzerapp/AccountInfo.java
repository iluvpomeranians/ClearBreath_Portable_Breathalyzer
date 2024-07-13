package com.example.coen390androidproject_breathalyzerapp;

public class AccountInfo {
    private String accountName;
    private String accountLastName;
    // To find the user easier, set from creation date, such as 1st user is number 1
    // 2nd user is number 2 etc etc
    private int accountNumber;
    private float accountPhoneNumber;
    private String accountBirthday;
    private String accountAddress;
    private String accountEmail;
    private String accountPassword;
    private String accountConfirmPassword;
    private float accountBMI;
    private int accountAge;
    private boolean accountGender;

    public AccountInfo(String accountName, String accountLastName, int accountNumber,
                       float accountPhoneNumber, String accountBirthday,
                       String accountAddress, String accountEmail, String accountPassword,
                       String accountConfirmPassword, int accountWeight, int accountHeight,
                       int accountAge, boolean accountGender) {
        this.accountName = accountName;
        this.accountLastName = accountLastName;
        this.accountNumber = accountNumber;
        this.accountPhoneNumber = accountPhoneNumber;
        this.accountBirthday = accountBirthday;
        this.accountAddress = accountAddress;
        this.accountEmail = accountEmail;
        this.accountPassword = accountPassword;
        this.accountConfirmPassword = accountConfirmPassword;
        this.accountBMI = accountBMI;
        this.accountAge = accountAge;
        this.accountGender = accountGender;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountLastName() {
        return accountLastName;
    }

    public void setAccountLastName(String accountLastName) {
        this.accountLastName = accountLastName;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public float getAccountPhoneNumber() {
        return accountPhoneNumber;
    }

    public void setAccountPhoneNumber(float accountPhoneNumber) {
        this.accountPhoneNumber = accountPhoneNumber;
    }

    public String getAccountBirthday() {
        return accountBirthday;
    }

    public void setAccountBirthday(String accountBirthday) {
        this.accountBirthday = accountBirthday;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getAccountConfirmPassword() {
        return accountConfirmPassword;
    }

    public void setAccountConfirmPassword(String accountConfirmPassword) {
        this.accountConfirmPassword = accountConfirmPassword;
    }

    public float getAccountBMI()
    {
        return accountBMI;
    }
    public void setAccountBMI()
    {
        this.accountBMI = accountBMI;
    }

    public int getAccountAge() {
        return accountAge;
    }

    public void setAccountAge(int accountAge) {
        this.accountAge = accountAge;
    }

    public boolean isAccountGender() {
        return accountGender;
    }

    public void setAccountGender(boolean accountGender) {
        this.accountGender = accountGender;
    }
}

