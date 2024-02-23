package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                    final String uid = currentUser.getUid();
                    final String newUsername = userName.getText().toString();

                    databaseReference = FirebaseDatabase.getInstance().getReference();

                    // Check if the username already exists
                    databaseReference.child("users").child(uid).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Username exists, update only the username
                                        databaseReference.child("users").child(uid).child("username").setValue(newUsername);
                                    } else {
                                        // Username doesn't exist, insert new username and run the for loop
                                        databaseReference.child("users").child(uid).child("username").setValue(newUsername);

                                        // Run the for loop for additional data
                                        for (char c = 'a'; c <= 'z'; c++) {
                                            String letter = String.valueOf(c);
                                            String attempt = letter.toLowerCase() + "-attempted";
                                            databaseReference.child("users").child(uid).child(attempt).setValue(0);
                                            databaseReference.child("users").child(uid).child(letter).setValue(0);

                                            String flower = letter.toLowerCase() + "-flower";
                                            databaseReference.child("users").child(uid).child(flower).setValue(0);
                                            // Set values for "letter-freeplay"
                                            String keyFreeplay = letter + "-freeplay";
                                            databaseReference.child("users").child(uid).child(keyFreeplay).setValue("0");

                                            // Set values for "letter-freeplay-correct"
                                            String keyCorrect = letter + "-freeplay-correct";
                                            databaseReference.child("users").child(uid).child(keyCorrect).setValue(0);

                                            // Set values for "letter-freeplay-flower"
                                            String keyFlower = letter + "-freeplay-flower";
                                            databaseReference.child("users").child(uid).child(keyFlower).setValue("0");

                                            // Set values for "letter-freeplay-incorrect"
                                            String keyIncorrect = letter + "-freeplay-incorrect";
                                            databaseReference.child("users").child(uid).child(keyIncorrect).setValue("0");
                                        }
                                    }

                                    // Continue with the rest of your code
                                    Intent intent = new Intent(User_Activity.this, Base_Activity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle the error if needed
                                }
                            });
                }
            }
        });
    }
}
