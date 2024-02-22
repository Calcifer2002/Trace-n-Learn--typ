package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Freeplay_Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freeplay_result);
        ArrayList<String> shownLetters = getIntent().getStringArrayListExtra("shownLetters");
        int receivedSkips = getIntent().getIntExtra("skips", 0);
        Log.d("meow", String.valueOf(shownLetters));
        Log.d("meow", String.valueOf(receivedSkips));
        TextView flowers = findViewById(R.id.flower);
        TextView title1 = findViewById(R.id.title1);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        DatabaseReference userUidRef = database.getReference("users").child(user.getUid());
        int score;
        if (receivedSkips >= 26) {
            score = 5;
        } else if (receivedSkips >= 24) {
            score = 4;
        } else if (receivedSkips >= 22) {
            score = 3;
        } else if (receivedSkips >= 18) {
            score = 2;
        } else if (receivedSkips >= 13) {
            score = 1;
        } else {
            score = 0; // Default score if none of the conditions are met
        }
        title1.setText(String.format("FreePlay Result Skill Level:\n %d/5", score));
        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flowerSum = 0;

                for (String letter : shownLetters) {

                    String key = letter + "-freeplay-flower";

                    // Check if the key exists in the dataSnapshot
                    if (dataSnapshot.child(key).exists()) {
                        int letterFlower = dataSnapshot.child(key).getValue(Integer.class);
                        flowerSum += letterFlower;
                    } else {
                        // Handle the case where the key doesn't exist
                        Log.e("meow", "Key not found in database: " + key);
                    }
                }
                String formattedText = String.format("Flowers: %d/13", flowerSum);
                // At this point, flowerSum contains the total sum of flowers for the shown letters
                flowers.setText(formattedText);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors during data retrieval
                Log.e("meow", "Database error: " + error.getMessage());
            }
        });
    }
}
