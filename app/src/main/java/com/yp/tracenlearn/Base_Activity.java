package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Base_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        TextView usrName = findViewById(R.id.userName);

        TextView flowerNumber = findViewById(R.id.flower);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        TextView open = findViewById(R.id.chosen);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference userUidRef = database.getReference("users").child(user.getUid());
        DatabaseReference usersRef = database.getReference("users").child(user.getUid()).child("username");

        ImageView viewProfile = findViewById(R.id.profile);

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Base_Activity.this, Profile_Activity.class);
                startActivity(intent);
            }
        });

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {

                    usrName.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

    });
        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flowerSum = 0;

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {


                    // Get the value of the child
                    // Get the value of the child
                    String childKey = childSnapshot.getKey();
                    Object childValue = childSnapshot.getValue();

                    if (childKey != null && childKey.contains("flower") && childValue instanceof Long) {
                        Log.d("meoq", childKey);
                        // Get the value of the child

                        flowerSum += (Long) childValue;

                    }
                }

                String formattedText = String.format("Flowers: %d/26", flowerSum);
                flowerNumber.setText(formattedText);
                Log.d("FlowerSumActivity", "Sum of children with 'flower': " + flowerSum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("FlowerSumActivity", "Error reading data: " + databaseError.getMessage());
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open User_activity
                Intent intent = new Intent(Base_Activity.this, Difficult_Activity.class);
                startActivity(intent);
            }
        });}}