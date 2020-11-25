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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register extends AppCompatActivity {

    private EditText etEmail, etPhoneNo, etAddress, etPassword, etConfirmPassword;
    private ProgressBar mProgressBar;
    private DatabaseReference reff;
    private User user;

    private int id = 1;
    private String email, phoneNo, address, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.email);
        etPhoneNo = findViewById(R.id.phoneNo);
        etAddress = findViewById(R.id.address);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm_password);
        mProgressBar = findViewById(R.id.progressBar);

        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    id = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount())) + 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void goToLoginPage(View view) {
        Intent i = new Intent(Register.this, Login.class);
        startActivity(i);

    }

    public void btnRegister_clicked(View view) {
        email = etEmail.getText().toString().trim();
        phoneNo = etPhoneNo.getText().toString().trim();
        address = etAddress.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(email.isEmpty()){
            etEmail.setError("Email address is required!");
            etEmail.requestFocus();
            return;
        }

        if(phoneNo.isEmpty()){
            etPhoneNo.setError("Phone number is required!");
            etPhoneNo.requestFocus();
            return;
        }

        if(address.isEmpty()){
            etAddress.setError("Address is required!");
            etAddress.requestFocus();
            return;
        }

        if(password.isEmpty()){
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }

        if(confirmPassword.isEmpty()){
            etConfirmPassword.setError("Confirm Password is required!");
            etConfirmPassword.requestFocus();
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

        if(!password.equals(confirmPassword)){
            etPassword.setError("Password and confirm password must be the same!");
            etPassword.requestFocus();
            return;
        }

        if(password.length() < 8){
            etPassword.setError("Password length must be more than or equal to 8");
            etPassword.requestFocus();
            return;
        }
        else{
            password = MD5(password);
        }

        mProgressBar.setVisibility(View.VISIBLE);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(user.getEmail().equals(email)){
                        etEmail.setError("Email exists in database, please enter another email");
                        etEmail.requestFocus();
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }
                    if(user.getPhoneNo().equals(phoneNo)){
                        etPhoneNo.setError("Phone number exists in database, please enter another phone number");
                        etPhoneNo.requestFocus();
                        mProgressBar.setVisibility(View.GONE);
                        return;
                    }
                }

                user = new User(id, email, password, address, phoneNo,0);
                reff.child(String.valueOf(id)).setValue(user);
                Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static String MD5(String password) {
        byte[] bytes = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {}
        byte[] hashed_password = md.digest(bytes);
        StringBuilder sb = new StringBuilder();

        for (byte b: hashed_password) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
