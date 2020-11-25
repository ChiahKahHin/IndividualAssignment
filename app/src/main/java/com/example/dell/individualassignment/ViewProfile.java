package com.example.dell.individualassignment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewProfile extends AppCompatActivity {

    DatabaseReference reff;
    private EditText etEmail, etPhoneNo, etAddress;
    private ProgressBar mProgressBar;
    private String email, phoneNo, address;
    private int user_id;
    private boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        etEmail = (EditText)findViewById(R.id.update_email);
        etPhoneNo = (EditText)findViewById(R.id.update_phoneNo);
        etAddress = (EditText)findViewById(R.id.update_address);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent i = getIntent();
        user_id = i.getIntExtra(MainActivity.EXTRA_ID, 0);

        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(user_id));
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                email = dataSnapshot.child("email").getValue().toString();
                phoneNo = dataSnapshot.child("phoneNo").getValue().toString();
                address = dataSnapshot.child("address").getValue().toString();

                etEmail.setText(email);
                etPhoneNo.setText(phoneNo);
                etAddress.setText(address);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent replyIntent = new Intent();
        replyIntent.putExtra(MainActivity.EXTRA_ID, user_id);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void btnUpdateDetails_clicked(View view) {
        email = etEmail.getText().toString().trim();
        phoneNo = etPhoneNo.getText().toString().trim();
        address = etAddress.getText().toString().trim();

        if(email.isEmpty()){
            etEmail.setError("Email address is required!");
            etEmail.requestFocus();
            return;
        }

        if(phoneNo.isEmpty()){
            etEmail.setError("Phone no is required!");
            etEmail.requestFocus();
            return;
        }
        if(address.isEmpty()){
            etAddress.setError("Address is required!");
            etAddress.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please provide a valid email address!");
            etEmail.requestFocus();
            return;
        }

        if(!Patterns.PHONE.matcher(phoneNo).matches()){
            etPhoneNo.setError("Please provide a valid phone number!");
            etPhoneNo.requestFocus();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getEmail().equals(email) && user.getId()!=user_id){
                        Toast.makeText(ViewProfile.this, "Email found in the database, please try another", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }
                    if(user.getPhoneNo().equals(phoneNo) && user.getId()!=user_id){
                        Toast.makeText(ViewProfile.this, "Phone number found in the database, please try another", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }
                }

                reff.child(String.valueOf(user_id)).child("email").setValue(email);
                reff.child(String.valueOf(user_id)).child("phoneNo").setValue(phoneNo);
                reff.child(String.valueOf(user_id)).child("address").setValue(address);
                Toast.makeText(ViewProfile.this, "User details update successfully", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
