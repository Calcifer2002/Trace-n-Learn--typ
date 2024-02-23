package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

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
        TextView flowerNumber = findViewById(R.id.flower);

        ImageView edtUsername = findViewById(R.id.editUsername);

        edtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open User_activity
                Intent intent = new Intent(Profile_Activity.this, User_Activity.class);
                startActivity(intent);
            }
        });

        ImageView viewProfile = findViewById(R.id.profile);


        ImageView homeButton = findViewById(R.id.home);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile_Activity.this, Base_Activity.class);
                startActivity(intent);
                finish(); // If you want to finish the current activity when navigating to Base_Activity
            }
        });
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile_Activity.this, Profile_Activity.class);
                startActivity(intent);
            }
        });
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);
                if (username != null) {
                    // Set the retrieved username to the TextView
                    userNameTextView.setText(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(Profile_Activity.this, "Failed to retrieve username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //get db
        DatabaseReference userUidRef = database.getReference("users").child(user.getUid());

        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;
                int flowerSum = 0;
                for (char letter = 'a'; letter <= 'z'; letter++) {
                    counter ++;
                    String key = letter + "-flower";
                    String key3 = letter + "-attempted";
                    String key2 = String.valueOf(letter);


                    final String idResult = "result" + letter;
                    int resId = getResources().getIdentifier(idResult, "id", getPackageName());


                    TextView resView = findViewById(resId);




                    // Check if the key exists in the dataSnapshot
                    if (dataSnapshot.child(key3).exists()) {

                        int letterFlower = dataSnapshot.child(key).getValue(Integer.class);
                        double correctness = dataSnapshot.child(key2).getValue(Double.class);
                        int letterAttempt = dataSnapshot.child(key3).getValue(Integer.class);

                        Log.d("meow", String.valueOf(letterAttempt));
                        flowerSum += letterFlower;
                        if (letterAttempt == 0 ){
                            resView.setText("Unattempted");}

                        else if (letterFlower ==0){
                            resView.setText("Incorrect");
                        }
                        else if (letterFlower == 1){

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

                    } else {
                        // Handle the case where the key doesn't exist
                        Log.e("meow", "Key not found in database: " + key);
                    }
                }
                String formattedText = String.format("Flowers: %d/26", flowerSum);
                // At this point, flowerSum contains the total sum of flowers for the shown letters
                flowerNumber.setText(formattedText);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors during data retrieval
                Log.e("meow", "Database error: " + error.getMessage());
            }
        });
    }
}

