package com.yp.tracenlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class Base_Activity extends AppCompatActivity {

    private static final int LETTER_CHANGE_DELAY = 15000; // 8 seconds
    private Handler handler = new Handler();
    private Runnable letterChangeRunnable;
    private int currentIndex = 0;
    private Set<String> shownLetters = new HashSet<>();

    // Your array of letters
    String[] array = {"L", "T", "I", "V", "H", "F", "E", "N", "C", "U", "M", "W", "X",
            "J", "O", "P", "D", "A", "B", "S", "Z", "Y", "K", "Q", "R", "G"};
    List<String> letters = new ArrayList<>(Arrays.asList(array));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        TextView usrName = findViewById(R.id.userName);
        TextView flowerNumber = findViewById(R.id.flower);
        TextView open = findViewById(R.id.chosen);
        TextView free = findViewById(R.id.freeplay);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                // Handle errors
            }
        });

        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int flowerSum = 0;

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String childKey = childSnapshot.getKey();
                    Object childValue = childSnapshot.getValue();

                    if (childKey != null && childKey.contains("flower") && childValue instanceof Long) {
                        flowerSum += (Long) childValue;
                    }
                }

                String formattedText = String.format("Flowers: %d/26", flowerSum);
                flowerNumber.setText(formattedText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Base_Activity.this, Difficult_Activity.class);
                startActivity(intent);
            }
        });

        free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                Random random = new Random();

                Dialog dialog = new Dialog(Base_Activity.this);
                dialog.setContentView(R.layout.freeplay_intro);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
                dialog.show();

                // timer   10 seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            // Start the periodic letter change after the dialog is dismissed
                            startPeriodicLetterChange(usersRef, random);
                        }
                    }
                }, 10000); //   10 seconds in milliseconds
            }
        });
    }

    private boolean baseActivityStarted = false;

    private void startPeriodicLetterChange(final DatabaseReference usersRef,
                                           final Random random) {
        // Initialize currentIndex to the index of "L"
        currentIndex = 0;

        // Start the letter activity with the first letter
        getNextLetter(usersRef);

        letterChangeRunnable = new Runnable() {
            @Override
            public void run() {
                getNextLetter(usersRef);

                handler.postDelayed(letterChangeRunnable, 12000);
            }
        };

        handler.postDelayed(letterChangeRunnable, 12000);
    }

    private int counter = 0; // Initialize a counter outside of the method
    private final int MAX_LETTERS = 12; // Set the maximum number of letters to generate

    private void getNextLetter(final DatabaseReference usersRef) {
        List<String> letters = Arrays.asList("L", "T", "I", "V", "H", "F", "E", "N", "C", "U", "M", "W", "X",
                "J", "O", "P", "D", "A", "B", "S", "Z", "Y", "K", "Q", "R", "G");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();

        DatabaseReference userUidRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userUidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && counter < MAX_LETTERS) {
                    String letter = letters.get(currentIndex);
                    String freePlayKey = letter.toLowerCase() + "-freeplay";

                    String accuracy = letter.toLowerCase();
                    Object accuracyObject = dataSnapshot.child(accuracy).getValue();

                    Object freePlayValueObject = dataSnapshot.child(freePlayKey).getValue();

                    // Check if the value is not null
                    if (freePlayValueObject != null ) {
                        // Convert the value to String
                        String freePlayValue = String.valueOf(freePlayValueObject);
                        Log.d("meow",freePlayValue);

                        // Start the letter activity with the current letter
                        startLetterActivity(letter);

                        // Increment the currentIndex based on the value of freePlayValue
                        if ("1".equals(freePlayValue) ) {
                            currentIndex += 2;
                        } else  {
                            currentIndex++;
                        }

                        // Increment the counter
                        counter++;
                    }
                } else if (counter >= MAX_LETTERS && !baseActivityStarted) {
                    startBaseActivity();
                    baseActivityStarted = true; // Set the flag to true
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event if needed
            }
        });
    }

    private void startBaseActivity() {
        Intent intent = new Intent(this, Base_Activity.class);
        startActivity(intent);
        finish();
    }

    private void startLetterActivity(String letter) {
        String activityName = letter.toUpperCase() + "_Activity";
        try {
            Class<?> activityClass = Class.forName("com.yp.tracenlearn." + activityName);
            Intent intent = new Intent(Base_Activity.this, activityClass);
            boolean freePlay = true;
            intent.putExtra("freePlay", freePlay);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}