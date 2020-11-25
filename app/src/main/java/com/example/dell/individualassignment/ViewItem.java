package com.example.dell.individualassignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewItem extends AppCompatActivity {

    private ImageView ivItemImg;
    private TextView tvItemName, tvItemDescription, tvItemPrice;
    private Button btnAddToCart;
    private DatabaseReference reffItem, reffCart, reffCartCount;
    private ProgressBar mProgressBar;

    private int itemId, userId, cartId = 1;
    private int itemImg;
    private String title = "Add to cart";
    private boolean check = false;
    private int itemIdStored, itemQtyStored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);

        mProgressBar = findViewById(R.id.progressBar);
        ivItemImg = findViewById(R.id.itemImage);
        tvItemName = findViewById(R.id.transactionTitle);
        tvItemDescription = findViewById(R.id.itemDescription);
        tvItemPrice = findViewById(R.id.transactionAmount);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        mProgressBar.setVisibility(View.VISIBLE);

        itemId = getIntent().getIntExtra("itemNo",0);
        userId = getIntent().getIntExtra("userId",0);

        reffItem = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_ITEM).child(String.valueOf(itemId));
        reffItem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Item item = dataSnapshot.getValue(Item.class);
                    tvItemName.setText(item.getItemName());
                    tvItemPrice.setText("RM" + String.format("%.2f", item.getItemPrice()));
                    tvItemDescription.setText(item.getItemDescription());
                    itemImg = getResources().getIdentifier(item.getItemImageName(), "drawable", getPackageName());
                    ivItemImg.setImageResource(itemImg);
                    mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(userId != 0){
            reffCart = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
            reffCart.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Cart cart = snapshot.getValue(Cart.class);
                            if (userId == cart.getUserID() && itemId == cart.getItemID() && cart.getItemStatus() == 1) {
                                cartId = cart.getId();
                                itemIdStored = cart.getItemID();
                                itemQtyStored = cart.getItemQty();
                                btnAddToCart.setText("Update Quantity");
                                check = true;
                                break;
                            }
                        }
                        if (!check) {
                            cartId = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount())) + 1;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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
            i.putExtra(MainActivity.EXTRA_ID, userId);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent replyIntent = new Intent();
        replyIntent.putExtra(MainActivity.EXTRA_ID, userId);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void btnAddToCart_clicked(View view) {
        if(userId == 0){
            Toast.makeText(this, "Please login or register an account", Toast.LENGTH_SHORT).show();
        }
        else{
            final NumberPicker itemQty = new NumberPicker(this);
            itemQty.setMinValue(1);
            itemQty.setMaxValue(10);
            if(check || itemQtyStored != 0){
                itemQty.setValue(itemQtyStored);
                title = "Update Quantity";
            }
            itemQty.setWrapSelectorWheel(true);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage("Quantity")
                    .setView(itemQty)
                    .setPositiveButton(title, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            int qty = itemQty.getValue();
                            if(itemIdStored == itemId){
                                if(itemQtyStored == qty){
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(ViewItem.this, "The quantity of item not changed", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else{
                                    itemQtyStored = qty;
                                    reffCart.child(String.valueOf(cartId)).child("itemQty").setValue(qty);
                                    mProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(ViewItem.this, "Item quantity updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                itemQtyStored = qty;
                                title = "Update Quantity";
                                btnAddToCart.setText("Update Quantity");
                                Cart cart = new Cart(cartId, userId, itemId, qty, 1, "","");
                                reffCart.child(String.valueOf(cartId)).setValue(cart);
                                mProgressBar.setVisibility(View.GONE);
                                Toast.makeText(ViewItem.this, "Item added into cart", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        }
    }

    public void btnViewCart_clicked(View view) {
        if(userId == 0){
            Toast.makeText(this, "Please login or register an account", Toast.LENGTH_SHORT).show();
        }
        else{
            reffCartCount = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_CART);
            reffCartCount.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cart cart = snapshot.getValue(Cart.class);
                        if (cart.getUserID() == userId && cart.getItemStatus() == 1) {
                            check = true;
                        }
                    }
                    if(check){
                        Intent i = new Intent(ViewItem.this, ViewCart.class);
                        i.putExtra(MainActivity.EXTRA_ID, userId);
                        startActivity(i);
                    }
                    else{
                        Toast.makeText(ViewItem.this, "No item found in the cart", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
