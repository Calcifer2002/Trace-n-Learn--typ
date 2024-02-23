package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
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

        ImageView viewProfile = findViewById(R.id.profile);


        ImageView homeButton = findViewById(R.id.home);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Freeplay_Result.this, Base_Activity.class);
                startActivity(intent);
                finish(); // If you want to finish the current activity when navigating to Base_Activity
            }
        });
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Freeplay_Result.this, Profile_Activity.class);
                startActivity(intent);
            }
        });
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
                int counter = 0;
                int flowerSum = 0;
                for (String letter : shownLetters) {
                    counter ++;
                    String key = letter.toLowerCase() + "-freeplay-flower";
                    String key2 = letter.toLowerCase() + "-freeplay-correct";
                    final String count = String.valueOf(counter);
                    final String idLetter = "letter" + count;
                    final String idResult = "result" + count;
                    int resId = getResources().getIdentifier(idResult, "id", getPackageName());
                    int letId = getResources().getIdentifier(idLetter, "id", getPackageName());
                    TextView letterView = findViewById(letId);
                    TextView resView = findViewById(resId);
                    letterView.setText(letter);



                    // Check if the key exists in the dataSnapshot
                    if (dataSnapshot.child(key).exists()) {
                        int letterFlower = dataSnapshot.child(key).getValue(Integer.class);
                        double correctness = dataSnapshot.child(key2).getValue(Double.class);

                        Log.d("meow","flower "+ flowerSum+ letter+ letterFlower);
                        flowerSum += letterFlower;
                        if (letterFlower == 1){


                            int intValue = (int) Math.round(correctness);

                            int normalizedValue;

                                //normalising
                                if (intValue >= 90 && intValue <= 91) {
                                    normalizedValue = 6;
                                } else if (intValue >= 92 && intValue <= 93) {
                                    normalizedValue = 7;
                                } else if (intValue >= 94 && intValue <= 95) {
                                    normalizedValue = 8;
                                } else if (intValue >= 96 && intValue <= 98) {
                                    normalizedValue = 9;
                                } else if (intValue >= 99) {
                                    normalizedValue = 10;
                                } else {

                                    normalizedValue = 0;
                                }

                                resView.setText("Correct - " + normalizedValue + "/10");
                        }
                        else if (letterFlower ==0){
                            resView.setText("Incorrect");
                        }
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
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.goodjob);
        mediaPlayer.start();
    }
}
