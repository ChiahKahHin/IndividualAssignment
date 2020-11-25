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

public class ChangePassword extends AppCompatActivity {

    private DatabaseReference reff;
    private ProgressBar mProgressBar;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;

    private int user_id;
    private String databasePassword, currentPassword, newPassword, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurrentPassword = (EditText)findViewById(R.id.currentPassword);
        etNewPassword = (EditText)findViewById(R.id.newPassword);
        etConfirmPassword = (EditText)findViewById(R.id.confirmPassword);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent i = getIntent();
        user_id = i.getIntExtra(MainActivity.EXTRA_ID, 0);

        reff = FirebaseDatabase.getInstance().getReference().child(MainActivity.FIREBASE_DB_CHILD_NAME_USER).child(String.valueOf(user_id));
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databasePassword = dataSnapshot.child("password").getValue().toString();
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

    public void btnChangePassword_clicked(View view) {
        currentPassword = etCurrentPassword.getText().toString().trim();
        newPassword = etNewPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if(currentPassword.isEmpty()){
            etCurrentPassword.setError("Current Password is required!");
            etCurrentPassword.requestFocus();
            return;
        }

        if(newPassword.isEmpty()){
            etNewPassword.setError("New password is required!");
            etNewPassword.requestFocus();
            return;
        }

        if(confirmPassword.isEmpty()){
            etConfirmPassword.setError("Confirm Password is required!");
            etConfirmPassword.requestFocus();
            return;
        }

        if(!databasePassword.equals(Register.MD5(currentPassword))){
            etCurrentPassword.setError("Current Password is incorrect!");
            etCurrentPassword.requestFocus();
            return;
        }

        if(newPassword.length()<8){
            etNewPassword.setError("Password length must be more than or equal to 8");
            etNewPassword.requestFocus();
            return;
        }

        if(!newPassword.equals(confirmPassword)){
            etNewPassword.setError("New password and confirm password must be the same");
            etNewPassword.requestFocus();
            return;
        }

        if(currentPassword.equals(newPassword)){
            etNewPassword.setError("Current and new password should not be the same");
            etNewPassword.requestFocus();
            return;
        }

        if(newPassword.equals(confirmPassword)){
            mProgressBar.setVisibility(View.VISIBLE);
            newPassword = Register.MD5(newPassword);
            reff.child("password").setValue(newPassword);
            databasePassword = newPassword;
            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            etCurrentPassword.setText("");
            etNewPassword.setText("");
            etConfirmPassword.setText("");
        }
    }
}
