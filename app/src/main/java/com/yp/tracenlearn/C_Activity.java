package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
//Audio used is from https://elevenlabs.io/
public class C_Activity extends AppCompatActivity {

    // Initialising dialogs
    private Dialog dialogNo;
    private Dialog dialogYes;
    private Dialog dialogNoMany;
    private Dialog dialogNoSlow;
    //Audio used is from https://elevenlabs.io/
    private Boolean freePlay = false; // Flag to see if we should store values differently for the different mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c);

        CCustomView customCCanvas = findViewById(R.id.customCCanvas); // Drawing canvas
        LinearLayout colorPanel = findViewById(R.id.colorPanel); // Color dash panel to change colours

        /*This is to get the current user to have different account functionality*/
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = currentUser.getUid();
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.c);
        mediaPlayer.start();//Letter is read out loud
        /*When we press freePlay we run  intent.putExtra("freePlay", freePlay); which passes
         the flag in which turns true to enable freeplay mode so that we know to store values with "freeplay" attatched key
         */

        boolean freePlay = getIntent().getBooleanExtra("freePlay", false);

        // The block below is just designing the dialogs
        dialogNo = new Dialog(C_Activity.this);
        dialogYes = new Dialog(C_Activity.this);
        dialogNoMany = new Dialog(C_Activity.this);
        dialogNoSlow = new Dialog(C_Activity.this);
        dialogYes.setContentView(R.layout.correct);
        dialogNo.setContentView(R.layout.incorrect);
        dialogNoMany.setContentView(R.layout.incorrect_many);
        dialogNoSlow.setContentView(R.layout.incorrect_slow);
        dialogYes.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNoSlow.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogYes.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNo.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg)); // just setting parameters
        dialogNoMany.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNoSlow.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));

        /* For loop below is that we have linear layouts with imageviews nested in them.
        So we wait ucheck if someone clicked on the nested imageviews through the for loop, we get the index which is
        sent to the customcanvas which has an array of colours. It will use the index to call on the respective colour, changing
        the stroke colour.
        */
        for (int i = 0; i < colorPanel.getChildCount(); i++) {
            View childLayout = colorPanel.getChildAt(i);
            if (childLayout instanceof LinearLayout) {
                // Iterate through the children of this LinearLayout
                for (int j = 0; j < ((LinearLayout) childLayout).getChildCount(); j++) {
                    View child = ((LinearLayout) childLayout).getChildAt(j);
                    if (child instanceof ImageView) {
                        final int colorIndex = i * 3 + j;  // We get the index
                        child.setOnClickListener(new View.OnClickListener() { // We wait for click
                            @Override
                            public void onClick(View v) {
                                customCCanvas.setStrokeColor(getColorForIndex(colorIndex)); // Get the respective colour for that index
                            }
                        });
                    }
                }
            }
        }

        /*This is a callback for when the user has not made a stroke for 3 seconds or more meaning they are done with the drawing
        so we can update db with values and show dialogs respectively with delays. The 0 or 1 helps with knowing if traced wrong or right respectively.
        */
        customCCanvas.setOnNoStrokesDetectedCallback(new CCustomView.NoStrokesCallback() {
            @Override
            public void onNoStrokesDetected(String accuracyInfo) {
                if (freePlay && accuracyInfo.toLowerCase().contains("no")) {     //When in freeplaymode and the letter is not traced properly
                    int colonIndex = accuracyInfo.indexOf(":");                  //No indictes that the trace is not proper
                    int percentIndex = accuracyInfo.indexOf("%");                //Parsing the info to send to db

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();
                    float rated = Float.parseFloat(rate);
                    databaseReference.child("users").child(uid).child("c-freeplay-incorrect").setValue(rated); //Keeping track of wrong letter rate
                    databaseReference.child("users").child(uid).child("c-freeplay-flower").setValue(0); //To track flower counter- wrong trace so we set it to 0
                    databaseReference.child("users").child(uid).child("c-freeplay").setValue("0");
                } else if (freePlay && !accuracyInfo.toLowerCase().contains("no")) {  //If it doesnt have "no" it means the trace is proper
                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();
                    float rated = Float.parseFloat(rate);
                    databaseReference.child("users").child(uid).child("c-freeplay-correct").setValue(rated);//Keeping track of correct letter rate
                    databaseReference.child("users").child(uid).child("c-freeplay-flower").setValue(1); //To track flower counter- correct trace so we set it to 1
                    databaseReference.child("users").child(uid).child("c-freeplay").setValue("1");
                } else if (accuracyInfo.toLowerCase().contains("many")) { // If way too many strokes
                    dialogNoMany.show(); // So we show too many strokes alert
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.toomany);
                    mediaPlayer.start();                    //Respective audio plays

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNoMany.dismiss(); //This handler makes sure dialog stays for 6 seconds
                            // We dismiss after 6 seconds
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("c-incorrect").setValue(rated);
                            databaseReference.child("users").child(uid).child("c-flower").setValue(0);
                            databaseReference.child("users").child(uid).child("c-freeplay").setValue("0");
                            Intent intent = new Intent(C_Activity.this, C_Activity.class);
                            startActivity(intent);
                            finish(); // Reload activity for the kid to retry
                        }
                    }, 6000);

                } else if (accuracyInfo.toLowerCase().contains("slow")) { // My accuracy info if the letter is drawn too quickly
                    dialogNoSlow.show(); // So we show an alert to slow down
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.slowdown);
                    mediaPlayer.start(); //We play the audio

                    new Handler().postDelayed(new Runnable() {  //Same as previous
                        @Override
                        public void run() {
                            dialogNoSlow.dismiss();//Makes sure its staying for 6 seconds
                            // We dismiss after some time
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("c-incorrect").setValue(rated);//Updating incorrect counters
                            databaseReference.child("users").child(uid).child("c-flower").setValue(0);
                            databaseReference.child("users").child(uid).child("c-freeplay").setValue("0");
                            Intent intent = new Intent(C_Activity.this, C_Activity.class);
                            startActivity(intent);
                            finish(); // Reload activity for the kid to retry
                        }
                    }, 6000);
                } else if (accuracyInfo.toLowerCase().contains("no")) {
                    dialogNo.show(); // So we show an incorrect letter alert
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tryagain);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNo.dismiss();
                            // We dismiss after some time
                            int colonIndex = accuracyInfo.indexOf(":"); //parsing data
                            int percentIndex = accuracyInfo.indexOf("%");

                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("c-incorrect").setValue(rated);//Updating incorrect counters
                            databaseReference.child("users").child(uid).child("c-flower").setValue(0);
                            databaseReference.child("users").child(uid).child("c-freeplay").setValue("0");
                            Intent intent = new Intent(C_Activity.this, C_Activity.class);
                            startActivity(intent);
                            finish(); // Reload activity for the kid to retry
                        }
                    }, 6000);

                } else {
                    dialogYes.show(); // If everything is okay

                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.goodjob);
                    mediaPlayer.start(); //Audio plays

                    int colonIndex = accuracyInfo.indexOf(":"); //Parsing data
                    int percentIndex = accuracyInfo.indexOf("%");

                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();

                    float rated = Float.parseFloat(rate);

                    Log.d("accu", accuracyInfo);
                    databaseReference.child("users").child(uid).child("c").setValue(rated);
                    databaseReference.child("users").child(uid).child("c-flower").setValue(1);
                    databaseReference.child("users").child(uid).child("c-freeplay").setValue("1");//We save the accuracy rate for that letter in the db
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogYes.dismiss(); //Same logic as above we show dialog for 6 seconds

                            Intent intent = new Intent(C_Activity.this, Difficult_Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 6000);
                }
            }
        });

    }

    /*The purpose of the function below is to just get respective colour
     */
    public int getColorForIndex(int index) {
        String[] colorHexCodes = {
                "#3498db",
                "#f1c40f",
                "#2ecc71",
                "#e67e22", //Just to send colour code
                "#9b59b6",
                "#1abc9c"
        };

        if (index >= 0 && index < colorHexCodes.length) {
            return Color.parseColor(colorHexCodes[index]); //If not in list then we give black colour
        } else {
            return Color.BLACK;
        }
    }
}

