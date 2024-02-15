package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        DatabaseReference usersRef = database.getReference("users").child(user.getUid()).child("username");

        TextView userNameTextView = findViewById(R.id.userName);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    // Set the retrieved username to the TextView
                    userNameTextView.setText("Hi " + username + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(Profile_Activity.this, "Failed to retrieve username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Remove the unnecessary DatabaseReference usersReflower line

        // Reference to the specific UID node under "users"
        DatabaseReference userUidRef = database.getReference("users").child(user.getUid());

        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flowerSum = 0;

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Assuming the child nodes are integers
                    Integer childValue = childSnapshot.getValue(Integer.class);

                    if (childValue != null) {
                        // Add the numerical value to the sum
                        flowerSum += childValue;
                    }
                }

                // Now, 'flowerSum' contains the sum of all children with the word "flower"
                Log.d("FlowerSumActivity", "Sum of children with 'flower': " + flowerSum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("FlowerSumActivity", "Error reading data: " + databaseError.getMessage());
            }
        });
    }
}
