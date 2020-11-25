package com.example.dell.individualassignment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ViewCart extends AppCompatActivity {

    private RecyclerView item_cart_recycler_view;
    private DatabaseReference reff, reffItem, reffUser, reffTransaction;
    private ProgressBar mProgressBar;
    private TextView tvTotal, tvWalletBalance;
    private ArrayList<Item> item_list = new ArrayList<>();
    private ArrayList<Integer> dbCartItems;
    private ArrayList<Integer> dbCartID;
    private ArrayList<Integer> qtyList;
    private ArrayList<Double> priceList;
    private ItemCartRowAdapter adapter;

    private int userID = 0, item_id;
    private int qty = 0;
    private int transaction_id = 1;
    private double total = 0.0;
    private double item_price;
    private String walletBalance, phoneNo, address;
    private boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);

        mProgressBar = findViewById(R.id.progressBar);
        tvTotal = findViewById(R.id.tvTotal);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        userID = getIntent().getIntExtra(MainActivity.EXTRA_ID,0);

        item_cart_recycler_view = findViewById(R.id.item_cart_recycler_view);
        adapter = new ItemCartRowAdapter(this, item_list, userID);
        item_cart_recycler_view.setAdapter(adapter);
        item_cart_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(item_cart_recycler_view);

        mProgressBar.setVisibility(View.VISIBLE);
        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dbCartItems = new ArrayList<>();
                dbCartID = new ArrayList<>();
                qtyList = new ArrayList<>();
                priceList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Cart cart = snapshot.getValue(Cart.class);
                    if (cart.getUserID() == userID && cart.getItemStatus() == 1) {
                        dbCartItems.add(cart.getItemID());
                        dbCartID.add(cart.getId());
                        qty = cart.getItemQty();
                        qtyList.add(qty);
                        check = true;
                    }
                }

                if (check) {
                    reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_ITEM);
                    reffItem.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(int i = 0; i < dbCartItems.size(); i++){
                                Item item = dataSnapshot.child(String.valueOf(dbCartItems.get(i))).getValue(Item.class);
                                item_list.add(new Item(item.getItemID(), item.getItemName(), item.getItemDescription(), item.getItemPrice(), item.getItemImageName()));
                                adapter.notifyDataSetChanged();

                                item_price = item.getItemPrice();
                                total = item_price * qtyList.get(i);
                                priceList.add(total);
                            }
                            double finalTotal = 0.0;
                            for (int j = 0; j < priceList.size(); j++) {
                                finalTotal += priceList.get(j);
                            }
                            tvTotal.setText("Total: RM " + String.format("%.2f", finalTotal));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                else{
                    Toast.makeText(ViewCart.this, "No items found in cart", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reffUser = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(userID));
        reffUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phoneNo = dataSnapshot.child("phoneNo").getValue().toString();
                address = dataSnapshot.child("address").getValue().toString();
                walletBalance = dataSnapshot.child("walletBalance").getValue().toString();
                tvWalletBalance.setText("Wallet: RM " + String.format("%.2f", Double.parseDouble(walletBalance)));
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            int position = viewHolder.getAdapterPosition();
            reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART).child(String.valueOf(dbCartID.get(position)));
            reffItem.child("itemStatus").setValue(0);
            dbCartItems.remove(position);
            dbCartID.remove(position);
            qtyList.remove(position);
            item_list.remove(position);
            adapter.notifyDataSetChanged();

            reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_ITEM);
            reffItem.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    priceList = new ArrayList<>();
                    for(int i = 0; i < dbCartItems.size(); i++) {
                        item_price = Double.parseDouble(dataSnapshot.child(String.valueOf(dbCartItems.get(i))).child("itemPrice").getValue().toString());
                        total = item_price * qtyList.get(i);
                        priceList.add(total);
                    }
                    double finalTotal = 0.0;
                    for (int j = 0; j < priceList.size(); j++) {
                        finalTotal += priceList.get(j);
                    }
                    tvTotal.setText("Total: RM " + String.format("%.2f", finalTotal));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Toast.makeText(ViewCart.this, "Item removed from the cart", Toast.LENGTH_SHORT).show();
            if(item_list.size() == 0){
                Toast.makeText(ViewCart.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewCart.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_ID, userID);
                startActivity(intent);
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(ViewCart.this, R.color.colorRed))
                    .setSwipeLeftLabelColor(ContextCompat.getColor(ViewCart.this, R.color.white))
                    .setSwipeRightLabelColor(ContextCompat.getColor(ViewCart.this, R.color.white))
                    .addActionIcon(R.drawable.ic_delete)
                    .addSwipeRightLabel("Remove this item")
                    .addSwipeLeftLabel("Remove this item")
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    public void btnPayment_clicked(View view) {
        String tvWallet = tvWalletBalance.getText().toString().trim();
        String tvTotalPrice = tvTotal.getText().toString().trim();
        String[] walletString = tvWallet.split("RM ");
        String[] totalPriceString = tvTotalPrice.split("RM ");

        double wallet = Double.parseDouble(walletString[1]);
        final double totalPrice = Double.parseDouble(totalPriceString[1]);

        if(totalPrice > wallet){
            Toast.makeText(this, "Your wallet balance is insufficient \n Please reload it before make payment", Toast.LENGTH_SHORT).show();
        }
        else{
            final double walletBalance = wallet - totalPrice;
            AlertDialog.Builder paymentConfirmation = new AlertDialog.Builder(this);
            paymentConfirmation.setIcon(R.drawable.ic_payment);
            paymentConfirmation.setTitle("Payment Confirmation");
            paymentConfirmation.setMessage("Are you sure you want to purchase those items? " +
                    "\n\nDelivery Address: " + address +
                    "\n\nPhone No: " + phoneNo +
                    "\n\nPlease make sure that delivery address & phone number is correct, your items will be delivered in 3 working days"+
                    "\n\nBalance of wallet after payment:\nRM " + String.format("%.2f", walletBalance));
            paymentConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(ViewCart.this, "Item not purchased", Toast.LENGTH_SHORT).show();
                }
            });
            paymentConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(userID));
                    reff.child("walletBalance").setValue(walletBalance);

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    String itemPurchasedDate = dateFormat.format(date);

                    Transaction transaction = new Transaction(transaction_id, userID, "Payment", "-RM" + String.format("%.2f", totalPrice), dateFormat1.format(date));
                    reffTransaction.child(String.valueOf(transaction_id)).setValue(transaction);

                    for(int i=0; i<dbCartID.size(); i++){
                        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART).child(String.valueOf(dbCartID.get(i)));
                        reff.child("itemStatus").setValue(2);
                        reff.child("itemPurchasedDate").setValue(itemPurchasedDate);
                    }
                    Intent i = new Intent(ViewCart.this, MainActivity.class);
                    i.putExtra(MainActivity.EXTRA_ID, userID);
                    startActivity(i);
                    Toast.makeText(ViewCart.this, "Item Purchased", Toast.LENGTH_SHORT).show();

                }
            });
            paymentConfirmation.setCancelable(true);
            paymentConfirmation.show();
        }
    }
    private class ItemCartRowAdapter extends RecyclerView.Adapter<ItemCartRowAdapter.MyViewHolder> {

        private Context mContext;
        private ArrayList<Item> mItem_list;
        private int userId, images;

        public ItemCartRowAdapter(Context ct, ArrayList<Item> mItem_list, int userId){
            this.mContext = ct;
            this.mItem_list = mItem_list;
            this.userId = userId;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.cart_row, viewGroup, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemCartRowAdapter.MyViewHolder myViewHolder, int i) {
            final int j = i;
            myViewHolder.itemName.setText(mItem_list.get(i).getItemName());
            myViewHolder.itemPrice.setText("RM" + String.format("%.2f",mItem_list.get(i).getItemPrice()));
            images = mContext.getResources().getIdentifier(mItem_list.get(i).getItemImageName(), "drawable", mContext.getPackageName());
            myViewHolder.itemImage.setImageResource(images);

            DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
            reff.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cart cart = snapshot.getValue(Cart.class);
                        if(cart.getItemID() == mItem_list.get(j).getItemID() && cart.getUserID() == userId){
                            myViewHolder.itemQuantity.setText("Quantity: "+String.valueOf(cart.getItemQty()));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            myViewHolder.cart_row_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewItem.class);
                    intent.putExtra("itemNo",mItem_list.get(j).getItemID());
                    intent.putExtra("userId", userId);
                    mContext.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItem_list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView itemName, itemPrice, itemQuantity;
            ImageView itemImage;
            ConstraintLayout cart_row_layout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.transactionTitle);
                itemPrice = itemView.findViewById(R.id.transactionAmount);
                itemImage = itemView.findViewById(R.id.itemImageView);
                itemQuantity = itemView.findViewById(R.id.itemQuantity);
                cart_row_layout = itemView.findViewById(R.id.cart_row_layout);
            }
        }
    }
}
