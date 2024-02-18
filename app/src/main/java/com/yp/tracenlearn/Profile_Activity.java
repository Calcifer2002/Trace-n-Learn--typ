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

        for (char letter = 'a'; letter <= 'z'; letter++) {
            final String letterKey = letter + "-flower";
            final String correctKey = String.valueOf(letter);
            final String idLetter = "result" + letter;
            int resId = getResources().getIdentifier(idLetter, "id", getPackageName());
            TextView resultTextView = findViewById(resId);
            userUidRef.child(letterKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer counter = dataSnapshot.getValue(Integer.class);
                    Log.d("%d", String.valueOf(counter));

                    if (counter != null) {
                        if (counter == 0) {
                            // Letter is 0, set the text to "Incorrect"
                            resultTextView.setText("Incorrect");
                        } else if (counter == 1) {
                            // Letter is 1, get the value of "correct" and set the text accordingly
                            userUidRef.child(correctKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot incorrectSnapshot) {
                                    Double correctValue = incorrectSnapshot.getValue(Double.class);

                                    if (correctValue != null) {
                                        int intValue = correctValue.intValue();

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

                                        resultTextView.setText("Correct - " + normalizedValue + "/10");
                                        Log.d("meow", "Correct - " + normalizedValue + "/10");
                                    }

                                }

                                // Handle onCancelled method if needed
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("Error", "Firebase database error: " + databaseError.getMessage());
                                }
                            });
                        }
                    }
                }

                // Handle onCancelled method if needed
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Error", "Firebase database error: " + databaseError.getMessage());
                }
            });
        }

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
            }
        }
