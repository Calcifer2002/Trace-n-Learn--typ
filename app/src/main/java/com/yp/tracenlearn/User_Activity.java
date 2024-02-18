package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User_Activity extends AppCompatActivity {
    private EditText userName;
    private Button addUser;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        userName = findViewById(R.id.editName);
        addUser = findViewById(R.id.sendUserButton);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference();


                    databaseReference.child("users").child(uid).child("username").setValue(userName.getText().toString());
                    Intent intent = new Intent(User_Activity.this, Base_Activity.class);
                    startActivity(intent);
                }
            }
        });


    }
}