package com.example.dell.individualassignment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReloadWallet extends AppCompatActivity {

    private DatabaseReference reff, reffTransaction;
    private ProgressBar mProgressBar;
    private EditText etReloadAmount;
    private TextView tvWalletBalance;

    private String walletBalance, password, reloadAmount;
    private int user_id;
    private int transaction_id = 1;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload_wallet);

        etReloadAmount = findViewById(R.id.etReloadAmount);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        mProgressBar = findViewById(R.id.progressBar);

        Intent i = getIntent();
        user_id = i.getIntExtra(MainActivity.EXTRA_ID, 0);

        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(user_id));
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                walletBalance = dataSnapshot.child("walletBalance").getValue().toString();

                tvWalletBalance.setText("Wallet Balance: RM " + String.format("%.2f", Double.parseDouble(walletBalance)));
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
    }

    @Override
    public void onBackPressed() {
        Intent replyIntent = new Intent();
        String amount[] = tvWalletBalance.getText().toString().trim().split("RM ");
        replyIntent.putExtra(MainActivity.EXTRA_ID, user_id);
        replyIntent.putExtra(MainActivity.EXTRA_BALANCE, Double.parseDouble(amount[1]));
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void btnReload_clicked(View view) {
        reloadAmount = etReloadAmount.getText().toString();

        if(reloadAmount.isEmpty()){
            etReloadAmount.setError("Reload amount is required!");
            etReloadAmount.requestFocus();
            return;
        }

        if(Double.parseDouble(reloadAmount) < 1){
            etReloadAmount.setError("Reload amount must be more than or equal to 1");
            etReloadAmount.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                walletBalance = dataSnapshot.child("walletBalance").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Insert into transaction
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        Transaction transaction = new Transaction(transaction_id, user_id, "Reload", "+RM" +
                String.format("%.2f", Double.parseDouble(reloadAmount)), dateFormat.format(date));
        reffTransaction.child(String.valueOf(transaction_id)).setValue(transaction);

        //update the wallet balance of user
        total = Double.parseDouble(walletBalance) + Double.parseDouble(reloadAmount);
        reff.child("walletBalance").setValue(total);
        Toast.makeText(this, "Wallet Reloaded", Toast.LENGTH_SHORT).show();
        tvWalletBalance.setText("Wallet Balance: RM " + String.format("%.2f", total));

        etReloadAmount.setText("");
        etReloadAmount.requestFocus();
        mProgressBar.setVisibility(View.GONE);
    }
}
