package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class P_Activity extends AppCompatActivity {
    private Dialog dialogNo;
    private Dialog dialogYes;
    private Dialog dialogNoMany;
    private Dialog dialogNoSlow;
    private Boolean freePlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p);

        PCustomView customPCanvas = findViewById(R.id.customPCanvas); // drawing canvas
        LinearLayout colorPanel = findViewById(R.id.colorPanel); // color dash
        FirebaseAuth auth = FirebaseAuth.getInstance(); // get user uid to add data under it
        FirebaseUser currentUser = auth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = currentUser.getUid();

        freePlay = getIntent().getBooleanExtra("freePlay", false);

        dialogNo = new Dialog(P_Activity.this);
        dialogYes = new Dialog(P_Activity.this);
        dialogNoMany = new Dialog(P_Activity.this);
        dialogNoSlow = new Dialog(P_Activity.this);
        dialogYes.setContentView(R.layout.correct);
        dialogNo.setContentView(R.layout.incorrect);
        dialogNoMany.setContentView(R.layout.incorrect_many);        // setting the dialog parameters
        dialogNoSlow.setContentView(R.layout.incorrect_slow);
        dialogYes.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNoSlow.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogYes.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNo.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg)); // just setting parameters
        dialogNoMany.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNoSlow.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));

        for (int i = 0; i < colorPanel.getChildCount(); i++) {
            View childLayout = colorPanel.getChildAt(i);
            if (childLayout instanceof LinearLayout) {
                // iterate through the children of this LinearLayout
                for (int j = 0; j < ((LinearLayout) childLayout).getChildCount(); j++) {
                    View child = ((LinearLayout) childLayout).getChildAt(j);
                    if (child instanceof ImageView) {
                        final int colorIndex = i * 3 + j;  // to send to change color
                        child.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customPCanvas.setStrokeColor(getColorForIndex(colorIndex)); // change color
                            }
                        });
                    }
                }
            }
        }

        customPCanvas.setOnNoStrokesDetectedCallback(new PCustomView.NoStrokesCallback() {
            @Override
            public void onNoStrokesDetected(String accuracyInfo) {
                if (freePlay && accuracyInfo.toLowerCase().contains("no")) {
                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();
                    float rated = Float.parseFloat(rate);
                    databaseReference.child("users").child(uid).child("p-freeplay-incorrect").setValue(rated);
                    databaseReference.child("users").child(uid).child("p-freeplay-flower").setValue(0);
                    databaseReference.child("users").child(uid).child("p-freeplay").setValue("0");

                } else if (freePlay && !accuracyInfo.toLowerCase().contains("no")) {
                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();
                    float rated = Float.parseFloat(rate);
                    databaseReference.child("users").child(uid).child("p-freeplay-correct").setValue(rated);
                    databaseReference.child("users").child(uid).child("p-freeplay-flower").setValue(1);
                    databaseReference.child("users").child(uid).child("p-freeplay").setValue("1");

                } else if (accuracyInfo.toLowerCase().contains("many")) { // if way too many strokes
                    dialogNoMany.show(); // so we show too many strokes alert
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.toomany);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNoMany.dismiss();
                            // we dismiss after some time
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("p-incorrect").setValue(rated);
                            databaseReference.child("users").child(uid).child("p-flower").setValue(0);
                            Intent intent = new Intent(P_Activity.this, P_Activity.class);
                            startActivity(intent);
                            finish(); // reload activity for the kid to retry
                        }
                    }, 6000);

                } else if (accuracyInfo.toLowerCase().contains("slow")) { // my accuracy info if the letter is drawn too quickly
                    dialogNoSlow.show(); // so we show an alert to slow down
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.slowdown);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNoSlow.dismiss();
                            // we dismiss after some time
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("p-incorrect").setValue(rated);
                            databaseReference.child("users").child(uid).child("p-flower").setValue(0);
                            Intent intent = new Intent(P_Activity.this, P_Activity.class);
                            startActivity(intent);
                            finish(); // reload activity for the kid to retry
                        }
                    }, 6000);
                } else if (accuracyInfo.toLowerCase().contains("no")) {
                    dialogNo.show(); // so we show an incorrect letter alert
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tryagain);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNo.dismiss();
                            // we dismiss after some time
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("p-incorrect").setValue(rated);
                            databaseReference.child("users").child(uid).child("p-flower").setValue(0);
                            Intent intent = new Intent(P_Activity.this, P_Activity.class);
                            startActivity(intent);
                            finish(); // reload activity for the kid to retry
                        }
                    }, 6000);

                } else {
                    dialogYes.show(); // if everything is okay

                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.goodjob);
                    mediaPlayer.start();

                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                    float rated = Float.parseFloat(rate);

                    Log.d("accu", accuracyInfo

                    );
                    databaseReference.child("users").child(uid).child("p").setValue(rated);
                    databaseReference.child("users").child(uid).child("p-flower").setValue(1);// we save the accuracy rate for that letter in the db
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogYes.dismiss(); // Corrected this line

                            Intent intent = new Intent(P_Activity.this, Difficult_Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 6000);
                }
            }
        });
    }

    public void setFreeplayMode(boolean value) {
        freePlay = value;
    }

    private int getColorForIndex(int index) {
        String[] colorHexCodes = {
                "#3498db",
                "#f1c40f",
                "#2ecc71",
                "#e67e22", // just to send color code
                "#9b59b6",
                "#1abc9c"
        };

        if (index >= 0 && index < colorHexCodes.length) {
            return Color.parseColor(colorHexCodes[index]);
        } else {
            return Color.BLACK;
        }
    }
}
