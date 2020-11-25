package com.example.dell.individualassignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewPurchaseDetails extends AppCompatActivity {

    private ImageView ivItemImg;
    private TextView tvItemName, tvItemPrice, tvItemQuantity, tvAmountPaid, tvDatePurchased, tvDateOrderReceived, tvDateOrder;
    private Button btnOrderReceived, btnCancelOrder;
    private DatabaseReference reffItem, reffCart, reffUser, reffTransaction;
    private ProgressBar mProgressBar;

    private int itemId, userId, cartId, itemImg;
    private int transaction_id = 1;
    private double itemPrice, total, walletBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_purchase_details);

        mProgressBar = findViewById(R.id.progressBar);
        ivItemImg = findViewById(R.id.itemImage);
        tvItemName = findViewById(R.id.transactionTitle);
        tvItemPrice = findViewById(R.id.transactionAmount);
        tvItemQuantity = findViewById(R.id.itemQuantity);
        tvAmountPaid = findViewById(R.id.amountPaid);
        tvDatePurchased = findViewById(R.id.datePurchased);
        tvDateOrderReceived = findViewById(R.id.dateOrderReceived);
        tvDateOrder = findViewById(R.id.tvDateOrder);
        btnOrderReceived = findViewById(R.id.btnOrderReceived);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        mProgressBar.setVisibility(View.VISIBLE);

        itemId = getIntent().getIntExtra("itemNo",0);
        userId = getIntent().getIntExtra("userId",0);
        cartId = getIntent().getIntExtra("cartId", 0);

        reffUser = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(userId));
        reffUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                walletBalance = user.getWalletBalance();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_ITEM).child(String.valueOf(itemId));
        reffItem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                itemPrice = item.getItemPrice();
                tvItemName.setText(item.getItemName());
                tvItemPrice.setText("RM" + String.format("%.2f", item.getItemPrice()));
                itemImg = getResources().getIdentifier(item.getItemImageName(), "drawable", getPackageName());
                ivItemImg.setImageResource(itemImg);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reffCart = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART).child(String.valueOf(cartId));
        reffCart.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Cart cart = dataSnapshot.getValue(Cart.class);
                tvItemQuantity.setText(String.valueOf(cart.getItemQty()));
                total = itemPrice*cart.getItemQty();
                tvAmountPaid.setText("RM" + String.format("%.2f", total));
                tvDatePurchased.setText(cart.getItemPurchasedDate());
                if(cart.getItemStatus()==2){
                    tvDateOrderReceived.setText("N/A");
                }
                else if(cart.getItemStatus() == 3){
                    tvDateOrderReceived.setText(cart.getItemDeliveredDate());
                    btnOrderReceived.setVisibility(View.GONE);
                    btnCancelOrder.setVisibility(View.GONE);
                }
                else if(cart.getItemStatus() == -1){
                    tvDateOrder.setText("Date Cancel Order: ");
                    tvDateOrderReceived.setText(cart.getItemDeliveredDate());
                    btnOrderReceived.setVisibility(View.GONE);
                    btnCancelOrder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reffTransaction = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_TRANSACTION);
        reffTransaction.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    transaction_id = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount())) + 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mProgressBar.setVisibility(View.GONE);
    }

    public void btnOrderReceived_clicked(View view) {
        AlertDialog.Builder orderReceivedConfirmation = new AlertDialog.Builder(this);
        orderReceivedConfirmation.setTitle("Order Received Confirmation");
        orderReceivedConfirmation.setMessage("Are you sure that you received those items?");
        orderReceivedConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ViewPurchaseDetails.this, "Item not received", Toast.LENGTH_SHORT).show();
            }
        });
        orderReceivedConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date date = new Date();
                String itemPurchasedDate = dateFormat.format(date);
                reffCart.child("itemDeliveredDate").setValue(itemPurchasedDate);
                reffCart.child("itemStatus").setValue(3);

                Toast.makeText(ViewPurchaseDetails.this, "Order received", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ViewPurchaseDetails.this, ViewPurchaseHistory.class);
                i.putExtra(MainActivity.EXTRA_ID, userId);
                startActivity(i);

            }
        });
        orderReceivedConfirmation.setCancelable(true);
        orderReceivedConfirmation.show();
    }

    public void btnCancelOrder_clicked(View view) {
        AlertDialog.Builder cancelOrderConfirmation = new AlertDialog.Builder(this);
        cancelOrderConfirmation.setTitle("Cancel Order Confirmation");
        cancelOrderConfirmation.setMessage("Are you sure that you want to cancel the order?\n\n" +
                "Refund will be made once the order is cancelled");
        cancelOrderConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ViewPurchaseDetails.this, "Order not cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        cancelOrderConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                Transaction transaction = new Transaction(transaction_id, userId, "Refund", "+RM"
                        + String.format("%.2f", total), dateFormat1.format(date));
                reffTransaction.child(String.valueOf(transaction_id)).setValue(transaction);

                double refund = walletBalance + total;
                reffUser.child("walletBalance").setValue(refund);
                reffCart.child("itemDeliveredDate").setValue(dateFormat.format(date));
                reffCart.child("itemStatus").setValue(-1);
                Toast.makeText(ViewPurchaseDetails.this, "Order cancelled\nAmount paid will be refund",
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ViewPurchaseDetails.this, ViewPurchaseHistory.class);
                i.putExtra(MainActivity.EXTRA_ID, userId);
                startActivity(i);
            }
        });
        cancelOrderConfirmation.setCancelable(true);
        cancelOrderConfirmation.show();
    }
}
