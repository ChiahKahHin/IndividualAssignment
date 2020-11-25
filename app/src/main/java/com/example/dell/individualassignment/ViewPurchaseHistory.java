package com.example.dell.individualassignment;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ViewPurchaseHistory extends AppCompatActivity {

    private RecyclerView purchase_history_recycler_view;
    private PurchaseHistoryRowAdapter adapter;
    private ArrayList<Item> item_list = new ArrayList<>();
    private ProgressBar mProgressBar;
    private DatabaseReference reff, reffItem;
    private ArrayList<Integer> dbCartItem;
    private ArrayList<Integer> dbCartID;

    private int userID, item_id;
    private boolean check;
    private String item_name, item_description, item_image_name;
    private double item_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_purchase_history);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        userID = getIntent().getIntExtra(MainActivity.EXTRA_ID,0);

        purchase_history_recycler_view = findViewById(R.id.purchase_history_recycler_view);

        mProgressBar.setVisibility(View.VISIBLE);
        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dbCartItem = new ArrayList<>();
                dbCartID = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Cart cart = snapshot.getValue(Cart.class);
                    if(cart.getUserID() == userID){
                        if(cart.getItemStatus() == 2 || cart.getItemStatus() == 3 || cart.getItemStatus() == -1) {
                            dbCartItem.add(cart.getItemID());
                            dbCartID.add(cart.getId());
                            Collections.reverse(dbCartItem);
                            Collections.reverse(dbCartID);
                            check = true;
                        }
                    }
                }

                if (check) {
                    for (int i = 0; i < dbCartItem.size(); i++) {
                        reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_ITEM).child(String.valueOf(dbCartItem.get(i)));
                        reffItem.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Item item = dataSnapshot.getValue(Item.class);
                                item_id = item.getItemID();
                                item_name = item.getItemName();
                                item_description = item.getItemDescription();
                                item_price = item.getItemPrice();
                                item_image_name = item.getItemImageName();

                                item_list.add(new Item(item_id, item_name, item_description, item_price, item_image_name));

                                adapter = new PurchaseHistoryRowAdapter(ViewPurchaseHistory.this, item_list, dbCartID, userID);
                                purchase_history_recycler_view.setAdapter(adapter);
                                purchase_history_recycler_view.setLayoutManager(new LinearLayoutManager(ViewPurchaseHistory.this));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else{
                    Toast.makeText(ViewPurchaseHistory.this, "No items found in purchase history", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mProgressBar.setVisibility(View.GONE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_dashboard) {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra(MainActivity.EXTRA_ID, userID);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent replyIntent = new Intent();
        replyIntent.putExtra(MainActivity.EXTRA_ID, userID);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    private class PurchaseHistoryRowAdapter extends RecyclerView.Adapter<PurchaseHistoryRowAdapter.MyViewHolder> {

        private Context mContext;
        private ArrayList<Item> mItem_list;
        private ArrayList<Integer> dbCartID;
        private int userId, images;

        public PurchaseHistoryRowAdapter(Context ct, ArrayList<Item> mItem_list, ArrayList<Integer> dbCartID ,int userId) {
            this.mContext = ct;
            this.mItem_list = mItem_list;
            this.dbCartID = dbCartID;
            this.userId = userId;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.purchase_history_row, viewGroup, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final PurchaseHistoryRowAdapter.MyViewHolder myViewHolder, int i) {
            final int j = i;
            myViewHolder.itemName.setText(mItem_list.get(i).getItemName());
            myViewHolder.itemPrice.setText("RM" + String.format("%.2f", mItem_list.get(i).getItemPrice()));
            images = mContext.getResources().getIdentifier(mItem_list.get(i).getItemImageName(), "drawable", mContext.getPackageName());
            myViewHolder.itemImage.setImageResource(images);

            DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
            reff.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double itemTotal = 0.0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cart cart = snapshot.getValue(Cart.class);
                        if (cart.getItemID() == mItem_list.get(j).getItemID() && cart.getUserID() == userId && cart.getId() == dbCartID.get(j)) {
                            myViewHolder.itemQuantity.setText("Quantity: " + String.valueOf(cart.getItemQty()));
                            myViewHolder.itemPurchaseDate.setText("Date Purchased: " + cart.getItemPurchasedDate());
                            itemTotal =  mItem_list.get(j).getItemPrice()*cart.getItemQty();
                            myViewHolder.itemTotal.setText("Amount Paid: RM" + String.format("%.2f", itemTotal));

                            if(cart.getItemStatus() == 2){
                                myViewHolder.itemStatus.setText("Delivering");
                            }
                            else if(cart.getItemStatus() == 3){
                                myViewHolder.itemStatus.setText("Delivered");
                                myViewHolder.itemPurchaseDate.setText("Date Delivered: " + cart.getItemDeliveredDate());
                            }
                            else{
                                myViewHolder.itemPurchaseDate.setText("Date Cancelled: " + cart.getItemDeliveredDate());
                                myViewHolder.itemTotal.setText("Amount Refund: RM" + String.format("%.2f", itemTotal));
                                myViewHolder.itemStatus.setText("Cancelled");
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            myViewHolder.purchase_history_row_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewPurchaseDetails.class);
                    intent.putExtra("itemNo", mItem_list.get(j).getItemID());
                    intent.putExtra("userId", userId);
                    intent.putExtra("cartId", dbCartID.get(j));
                    mContext.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItem_list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView itemName, itemPrice, itemQuantity, itemPurchaseDate, itemTotal, itemStatus;
            ImageView itemImage;
            ConstraintLayout purchase_history_row_layout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.transactionTitle);
                itemPrice = itemView.findViewById(R.id.transactionAmount);
                itemImage = itemView.findViewById(R.id.itemImageView);
                itemQuantity = itemView.findViewById(R.id.itemQuantity);
                itemPurchaseDate = itemView.findViewById(R.id.tvDate);
                itemTotal = itemView.findViewById(R.id.tvTotal);
                itemStatus = itemView.findViewById(R.id.tvStatus);

                purchase_history_row_layout = itemView.findViewById(R.id.purchase_history_row_layout);
            }
        }
    }
}
