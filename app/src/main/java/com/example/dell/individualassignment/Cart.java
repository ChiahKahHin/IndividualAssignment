package com.example.dell.individualassignment;

public class Cart {
    private int id;
    private int userID;
    private int itemID;
    private int itemQty;
    private int itemStatus;
    private String itemPurchasedDate;
    private String itemDeliveredDate;

    public Cart(){
        this.id = 0;
        this.userID = 0;
        this.itemID = 0;
        this.itemQty = 0;
        this.itemStatus = 0;
        this.itemPurchasedDate = "";
        this.itemDeliveredDate = "";
    }

    public Cart(int id, int userID, int itemID, int itemQty, int itemStatus, String itemPurchasedDate, String itemDeliveredDate){
        this.id = id;
        this.userID = userID;
        this.itemID = itemID;
        this.itemQty = itemQty;
        this.itemStatus = itemStatus;
        this.itemPurchasedDate = itemPurchasedDate;
        this.itemDeliveredDate = itemDeliveredDate;
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

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getItemQty() {
        return itemQty;
    }

    public void setItemQty(int itemQty) {
        this.itemQty = itemQty;
    }

    public int getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(int itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getItemPurchasedDate() {
        return itemPurchasedDate;
    }

    public void setItemPurchasedDate(String itemPurchasedDate) {
        this.itemPurchasedDate = itemPurchasedDate;
    }

    public String getItemDeliveredDate() {
        return itemDeliveredDate;
    }

    public void setItemDeliveredDate(String itemDeliveredDate) {
        this.itemDeliveredDate = itemDeliveredDate;
    }
}
