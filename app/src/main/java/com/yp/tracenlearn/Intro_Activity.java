package com.yp.tracenlearn;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class Intro_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Button button = findViewById(R.id.button);


        //Clicking play takes them to the main screen where they get an OTP
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Main_Activity
                Intent intent = new Intent(Intro_Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
