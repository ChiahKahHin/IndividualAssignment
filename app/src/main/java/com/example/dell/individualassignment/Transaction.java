package com.example.dell.individualassignment;

public class Transaction {
    private int id;
    private int userID;
    private String transactionTitle;
    private String transactionAmount;
    private String transactionDateTime;

    public Transaction(){
        id = 0;
        userID = 0;
        transactionTitle = "";
        transactionAmount = "";
        transactionDateTime = "";
    }

    public Transaction(int id, int userID, String transactionTitle, String transactionAmount, String transactionDateTime){
        this.id = id;
        this.userID = userID;
        this.transactionTitle = transactionTitle;
        this.transactionAmount = transactionAmount;
        this.transactionDateTime = transactionDateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionTitle() {
        return transactionTitle;
    }

    public void setTransactionTitle(String transactionTitle) {
        this.transactionTitle = transactionTitle;
    }

    public String getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }
}
