package com.example.dell.individualassignment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FIREBASE_DB_CHILD_NAME_ITEM = "Item";
    public static final String FIREBASE_DB_CHILD_NAME_CART = "Cart";
    public static final String FIREBASE_DB_CHILD_NAME_USER = "User";
    public static final String FIREBASE_DB_CHILD_NAME_TRANSACTION = "Transaction";
    public static final String EXTRA_ID = "com.example.dell.individualassignment.EXTRA_ID";
    public static final String EXTRA_BALANCE = "com.example.dell.individualassignment.EXTRA_BALANCE";
    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_2 = 2;

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout swipeRefresh;
    private Menu menu;
    private DatabaseReference reff, reffCartCount, reffWalletCount, reffTransactionCount;
    private RecyclerView item_recycler_view;
    private ArrayList<Item> item_list;
    private ItemRowAdapter adapter;

    private int user_id, item_id;
    private String item_name, item_description, item_image_name;
    private double item_price;
    private boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        user_id = i.getIntExtra(EXTRA_ID, 0);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(user_id == 0){
                    Snackbar.make(view, "Please login to view your cart", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    reffCartCount = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_CART);
                    reffCartCount.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Cart cart = snapshot.getValue(Cart.class);
                                if (cart.getUserID() == user_id && cart.getItemStatus() == 1) {
                                    check = true;
                                }
                            }
                            if(check){
                                Intent i = new Intent(MainActivity.this, ViewCart.class);
                                i.putExtra(EXTRA_ID, user_id);
                                startActivity(i);
                            }
                            else{
                                Snackbar.make(view, "No items found in the cart", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();
        if(user_id != 0){
            menu.findItem(R.id.nav_login).setVisible(false);

            reffWalletCount = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(user_id));
            reffWalletCount.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String balance = dataSnapshot.child("walletBalance").getValue().toString();

                    menu.findItem(R.id.walletBalance).setTitle("RM " + String.format("%.2f", Double.parseDouble(balance)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            menu.findItem(R.id.tvWalletBalance).setVisible(false);
            menu.findItem(R.id.walletBalance).setVisible(false);
            menu.findItem(R.id.nav_viewProfile).setVisible(false);
            menu.findItem(R.id.nav_reload).setVisible(false);
            menu.findItem(R.id.nav_transactionHistory).setVisible(false);
            menu.findItem(R.id.nav_cart).setVisible(false);
            menu.findItem(R.id.nav_purchaseHistory).setVisible(false);
            menu.findItem(R.id.nav_changePassword).setVisible(false);
            menu.findItem(R.id.nav_logout).setVisible(false);
        }

        item_list = new ArrayList<>();
        mProgressBar.setVisibility(View.VISIBLE);
        reff = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_ITEM);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    item_id = Integer.parseInt(dataSnapshot.child(String.valueOf(i)).child("itemID").getValue().toString());
                    item_name = dataSnapshot.child(String.valueOf(i)).child("itemName").getValue().toString();
                    item_description = dataSnapshot.child(String.valueOf(i)).child("itemDescription").getValue().toString();
                    item_price = Double.parseDouble(dataSnapshot.child(String.valueOf(i)).child("itemPrice").getValue().toString());
                    item_image_name = dataSnapshot.child(String.valueOf(i)).child("itemImageName").getValue().toString();

                    item_list.add(new Item(item_id, item_name, item_description, item_price, item_image_name));
                }
                Collections.shuffle(item_list);
                item_recycler_view = (RecyclerView) findViewById(R.id.item_recycler_view);
                adapter = new ItemRowAdapter(MainActivity.this, item_list, user_id);
                item_recycler_view.setAdapter(adapter);
                item_recycler_view.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Collections.shuffle(item_list);
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            Intent i = new Intent(this, Login.class);
            startActivity(i);

        } else if (id == R.id.nav_viewProfile) {
            Intent i = new Intent(this, ViewProfile.class);
            i.putExtra(EXTRA_ID, user_id);
            startActivityForResult(i, REQUEST_CODE);

        } else if (id == R.id.nav_reload) {
            Intent i = new Intent(this, ReloadWallet.class);
            i.putExtra(EXTRA_ID, user_id);
            startActivityForResult(i, REQUEST_CODE_2);

        }else if (id == R.id.nav_cart) {
            reffCartCount = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_CART);
            reffCartCount.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cart cart = snapshot.getValue(Cart.class);
                        if (cart.getUserID() == user_id && cart.getItemStatus() == 1) {
                            check = true;
                        }
                    }
                    if(check){
                        Intent i = new Intent(MainActivity.this, ViewCart.class);
                        i.putExtra(EXTRA_ID, user_id);
                        startActivityForResult(i, REQUEST_CODE);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "No item found in cart", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if (id == R.id.nav_purchaseHistory) {
            reffCartCount = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_CART);
            reffCartCount.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Cart cart = snapshot.getValue(Cart.class);
                        if (cart.getUserID() == user_id){
                            if(cart.getItemStatus() == 2 || cart.getItemStatus() == 3 || cart.getItemStatus() == -1) {
                                check = true;
                            }
                        }
                    }
                    if(check){
                        Intent i = new Intent(MainActivity.this, ViewPurchaseHistory.class);
                        i.putExtra(EXTRA_ID, user_id);
                        startActivityForResult(i, REQUEST_CODE);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "No record found in purchase history", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if(id == R.id.nav_transactionHistory){
            reffTransactionCount = FirebaseDatabase.getInstance().getReference().child(FIREBASE_DB_CHILD_NAME_TRANSACTION);
            reffTransactionCount.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Transaction transaction = snapshot.getValue(Transaction.class);
                        if (transaction.getUserID() == user_id){
                            check = true;
                        }
                    }
                    if(check){
                        Intent i = new Intent(MainActivity.this, ViewTransactionHistory.class);
                        i.putExtra(EXTRA_ID, user_id);
                        startActivityForResult(i, REQUEST_CODE);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "No record found in transaction history", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if (id == R.id.nav_changePassword) {
            Intent i = new Intent(this, ChangePassword.class);
            i.putExtra(EXTRA_ID, user_id);
            startActivityForResult(i, REQUEST_CODE);

        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder logoutConfirmation = new AlertDialog.Builder(this);
            logoutConfirmation.setIcon(R.drawable.ic_logout);
            logoutConfirmation.setTitle("Logout?");
            logoutConfirmation.setMessage("Are you sure you want to logout?");
            logoutConfirmation.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    user_id = 0;
                    Toast.makeText(MainActivity.this, "User logout successfully", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.putExtra(EXTRA_ID, user_id);
                    startActivity(i);
                }
            });
            logoutConfirmation.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            logoutConfirmation.setCancelable(true);
            logoutConfirmation.show();

        } else if(id == R.id.walletBalance){
            String walletBalance =  item.getTitle().toString();
            Toast.makeText(this, "Wallet Balance is " + walletBalance, Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                user_id = data.getIntExtra(EXTRA_ID, 0);
            }
        }
        if(requestCode == REQUEST_CODE_2){
            if(resultCode == RESULT_OK){
                user_id = data.getIntExtra(EXTRA_ID, 0);
                double balance = data.getDoubleExtra(EXTRA_BALANCE, 0.0);
                menu.findItem(R.id.walletBalance).setTitle("RM " + String.format("%.2f", balance));
            }
        }
    }

    private class ItemRowAdapter extends RecyclerView.Adapter<ItemRowAdapter.MyViewHolder>{

        private Context mContext;
        private ArrayList<Item> mItem_list;
        private int userId, images;

        public ItemRowAdapter(Context ct, ArrayList<Item> mItem_list, int userId){
            this.mContext = ct;
            this.mItem_list = mItem_list;
            this.userId = userId;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_row, viewGroup, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemRowAdapter.MyViewHolder myViewHolder, final int i) {
            myViewHolder.itemName.setText(mItem_list.get(i).getItemName());
            myViewHolder.itemPrice.setText("RM" + String.format("%.2f",mItem_list.get(i).getItemPrice()));
            images = mContext.getResources().getIdentifier(mItem_list.get(i).getItemImageName(), "drawable", mContext.getPackageName());
            myViewHolder.itemImage.setImageResource(images);

            myViewHolder.item_row_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ViewItem.class);
                    intent.putExtra("itemNo",mItem_list.get(i).getItemID());
                    intent.putExtra("userId", userId);
                    mContext.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItem_list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            TextView itemName, itemPrice;
            ImageView itemImage;
            ConstraintLayout item_row_layout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.transactionTitle);
                itemPrice = itemView.findViewById(R.id.transactionAmount);
                itemImage = itemView.findViewById(R.id.itemImageView);
                item_row_layout = itemView.findViewById(R.id.item_row_layout);
            }
        }
    }
}
