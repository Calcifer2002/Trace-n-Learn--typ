package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
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

public class N_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_n);

        Dialog dialogNo; //popup if not proper letter
        Dialog dialogYes; //popup if proper letter


        NCustomView customNCanvas = findViewById(R.id.customNCanvas); //drawing canvas
        LinearLayout colorPanel = findViewById(R.id.colorPanel); //colour dash
        FirebaseAuth auth = FirebaseAuth.getInstance(); //to get user uid so that i can add data under it
        FirebaseUser currentUser = auth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = currentUser.getUid();
        dialogNo = new Dialog(N_Activity.this);
        dialogYes = new Dialog(N_Activity.this);
        dialogYes.setContentView(R.layout.correct);
        dialogNo.setContentView(R.layout.incorrect);
        dialogYes.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogYes.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNo.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg)); //just setting parameters


        for (int i = 0; i < colorPanel.getChildCount(); i++) {
            View childLayout = colorPanel.getChildAt(i);
            if (childLayout instanceof LinearLayout) {
                // iterate through the children of this LinearLayout
                for (int j = 0; j < ((LinearLayout) childLayout).getChildCount(); j++) {
                    View child = ((LinearLayout) childLayout).getChildAt(j);
                    if (child instanceof ImageView) {
                        final int colorIndex = i * 3 + j;  //to send to change colour
                        child.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customNCanvas.setStrokeColor(getColorForIndex(colorIndex)); //change colour
                            }
                        });
                    }
                }
            }
        }
        customNCanvas.setOnNoStrokesDetectedCallback(new NCustomView.NoStrokesCallback() {

            @Override
            public void onNoStrokesDetected(String accuracyInfo) {
                if (accuracyInfo.toLowerCase().contains("no")) { //my accuracy info if incorrect letter has the word no in it
                    dialogNo.show(); //so we show incorrect letter alert

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNo.dismiss();
                            //we dismiss after sometime
                            int colonIndex = accuracyInfo.indexOf(":");
                            int percentIndex = accuracyInfo.indexOf("%");


                            String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();


                            float rated = Float.parseFloat(rate);
                            databaseReference.child("users").child(uid).child("n-incorrect").setValue(rated);
                            databaseReference.child("users").child(uid).child("n-flower").setValue(0);
                            Intent intent = new Intent(N_Activity.this, N_Activity.class);
                            startActivity(intent);
                            finish(); //reload activity for kid to retry
                        }
                    }, 6000);
                } else {
                    dialogYes.show();


                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");


                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();


                    float rated = Float.parseFloat(rate);


                    Log.d("accu", accuracyInfo);
                    databaseReference.child("users").child(uid).child("n").setValue(rated);
                    databaseReference.child("users").child(uid).child("n-flower").setValue(1);//we save the accuracy rate for that letter in the db
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogYes.dismiss(); // Corrected this line

                            Intent intent = new Intent(N_Activity.this, N_Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 6000);
                }
            }
        });

    }
    private int getColorForIndex(int index) {
        String[] colorHexCodes = {
                "#3498db",
                "#f1c40f",
                "#2ecc71",
                "#e67e22", //just to send colour code
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
