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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class B_Activity extends AppCompatActivity {
    Dialog dialogNo;
    Dialog dialogYes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        BCustomView customBCanvas = findViewById(R.id.customBCanvas);
        LinearLayout colorPanel = findViewById(R.id.colorPanel);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String uid = currentUser.getUid();
        dialogNo = new Dialog(B_Activity.this);
        dialogYes = new Dialog(B_Activity.this);
        dialogYes.setContentView(R.layout.correct);
        dialogNo.setContentView(R.layout.incorrect);
        dialogYes.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogYes.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        dialogNo.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        Pattern pattern = Pattern.compile("\\b(\\d+\\.?\\d*)%\\b");

        // Match the pattern against the text


        for (int i = 0; i < colorPanel.getChildCount(); i++) {
            View childLayout = colorPanel.getChildAt(i);
            if (childLayout instanceof LinearLayout) {
                // Iterate through the children of this LinearLayout
                for (int j = 0; j < ((LinearLayout) childLayout).getChildCount(); j++) {
                    View child = ((LinearLayout) childLayout).getChildAt(j);
                    if (child instanceof ImageView) {
                        final int colorIndex = i * 3 + j;  // Assuming each line has 3 colors
                        child.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customBCanvas.setStrokeColor(getColorForIndex(colorIndex));
                            }
                        });
                    }
                }
            }
        }
        customBCanvas.setOnNoStrokesDetectedCallback(new BCustomView.NoStrokesCallback() {

            @Override
            public void onNoStrokesDetected(String accuracyInfo) {
                if (accuracyInfo.toLowerCase().contains("no")) {
                    dialogNo.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogNo.dismiss();

                            Intent intent = new Intent(B_Activity.this, B_Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 6000);
                } else {
                    dialogYes.show();


                    int colonIndex = accuracyInfo.indexOf(":");
                    int percentIndex = accuracyInfo.indexOf("%");


                    String rate = accuracyInfo.substring(colonIndex + 1, percentIndex).trim();


                    float rated = Float.parseFloat(rate);


                    Log.d("accu", accuracyInfo);
                    databaseReference.child("users").child(uid).child("b").setValue(rated);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogYes.dismiss(); // Corrected this line

                            Intent intent = new Intent(B_Activity.this, B_Activity.class);
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
                "#e67e22",
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


