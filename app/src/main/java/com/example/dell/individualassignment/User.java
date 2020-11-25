package com.example.dell.individualassignment;

public class User {
    private int id;
    private String email;
    private String password;
    private String address;
    private String phoneNo;
    private double walletBalance;

    public User(){
        this.id = 0;
        this.email = "";
        this.password = "";
        this.address = "";
        this.phoneNo = "";
        this.walletBalance = 0.0;
    }

    public User(int id, String email, String password, String address, String phoneNo, double walletBalance){
        this.id = id;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNo = phoneNo;
        this.walletBalance = walletBalance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }
}
