package com.example.dell.individualassignment;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ViewTransactionHistory extends AppCompatActivity {

    private DatabaseReference reff;
    private ProgressBar mProgressBar;
    private RecyclerView transaction_history_recycler_view;
    private ArrayList<Transaction> transactionHistoryList;
    private TransactionHistoryRowAdapter mAdapter;
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction_history);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        transaction_history_recycler_view = (RecyclerView) findViewById(R.id.transaction_history_recycler_view);

        userID = getIntent().getIntExtra(MainActivity.EXTRA_ID,0);
        mProgressBar.setVisibility(View.VISIBLE);
        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_TRANSACTION);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactionHistoryList = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if(transaction.getUserID() == userID){
                        transactionHistoryList.add(transaction);
                    }
                }
                Collections.reverse(transactionHistoryList);
                mAdapter = new ViewTransactionHistory.TransactionHistoryRowAdapter(ViewTransactionHistory.this, transactionHistoryList);
                transaction_history_recycler_view.setAdapter(mAdapter);
                transaction_history_recycler_view.setLayoutManager(new LinearLayoutManager(ViewTransactionHistory.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Intent replyIntent = new Intent();
        replyIntent.putExtra(MainActivity.EXTRA_ID, userID);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    private class TransactionHistoryRowAdapter extends RecyclerView.Adapter<ViewTransactionHistory.TransactionHistoryRowAdapter.MyViewHolder> {

        private Context mContext;
        private ArrayList<Transaction> mTransactionHistoryList;

        public TransactionHistoryRowAdapter(Context ct, ArrayList<Transaction> mTransactionHistoryList) {
            this.mContext = ct;
            this.mTransactionHistoryList = mTransactionHistoryList;
        }

        @NonNull
        @Override
        public ViewTransactionHistory.TransactionHistoryRowAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.transaction_history_row, viewGroup, false);

            return new ViewTransactionHistory.TransactionHistoryRowAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewTransactionHistory.TransactionHistoryRowAdapter.MyViewHolder myViewHolder, int i) {
            myViewHolder.transactionTitle.setText(mTransactionHistoryList.get(i).getTransactionTitle());
            myViewHolder.transactionAmount.setText(mTransactionHistoryList.get(i).getTransactionAmount());
            myViewHolder.transactionDateTime.setText(mTransactionHistoryList.get(i).getTransactionDateTime());
        }

        @Override
        public int getItemCount() {
            return mTransactionHistoryList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView transactionTitle, transactionAmount, transactionDateTime;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                transactionTitle = itemView.findViewById(R.id.transactionTitle);
                transactionAmount = itemView.findViewById(R.id.transactionAmount);
                transactionDateTime = itemView.findViewById(R.id.transactionDateTime);
            }
        }
    }
}
