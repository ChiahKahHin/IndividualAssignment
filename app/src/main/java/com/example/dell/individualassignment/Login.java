package com.example.dell.individualassignment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ProgressBar mProgressBar;
    private DatabaseReference reff;

    private String email, password;
    private int id;
    private boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = (EditText)findViewById(R.id.email);
        etPassword = (EditText)findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER);
    }

    public void goToRegisterPage(View view) {
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }

    public void btnLogin_clicked(View view) {
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();

        if(email.isEmpty()){
            etEmail.setError("Email address is required!");
            etEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }
        else{
            password = Register.MD5(password);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                        id = user.getId();
                        check = true;
                    }
                }
                if(check){
                    Toast.makeText(Login.this, "User login successfully", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra(MainActivity.EXTRA_ID, id);
                    startActivity(i);
                }
                else{
                    Toast.makeText(Login.this, "Email not found or password incorrect", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
