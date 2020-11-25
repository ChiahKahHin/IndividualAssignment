package com.example.dell.individualassignment;

public class Item {
    private int itemID;
    private double itemPrice;
    private String itemName;
    private String itemDescription;
    private String itemImageName;

    public Item() {
        this.itemID = 0;
        this.itemName = "";
        this.itemDescription = "";
        this.itemPrice = 0.0;
        this.itemImageName = "";
    }

    public Item(int itemID, String itemName, String itemDescription, double itemPrice, String itemImageName){
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemImageName = itemImageName;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImageName() {
        return itemImageName;
    }

    public void setItemImageName(String itemImageName) {
        this.itemImageName = itemImageName;
    }
}
