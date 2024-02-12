package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Difficulty_Page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_page);
    }

    public void onLetterClick(View view) {
        int textViewId = view.getId();
        Intent intent;

        if (textViewId == R.id.boxA) {
            intent = new Intent(this, A_Activity.class);
        } else if (textViewId == R.id.boxB) {
            intent = new Intent(this, B_Activity.class);
        } else if (textViewId == R.id.boxC) {
            intent = new Intent(this, C_Activity.class);
        } else if (textViewId == R.id.boxD) {
            intent = new Intent(this, D_Activity.class);
        } else if (textViewId == R.id.boxE) {
            intent = new Intent(this, E_Activity.class);
        } else if (textViewId == R.id.boxF) {
            intent = new Intent(this, F_Activity.class);
        } else if (textViewId == R.id.boxG) {
            intent = new Intent(this, G_Activity.class);
        } else if (textViewId == R.id.boxH) {
            intent = new Intent(this, H_Activity.class);
        } else if (textViewId == R.id.boxI) {
            intent = new Intent(this, I_Activity.class);
        } else if (textViewId == R.id.boxJ) {
            intent = new Intent(this, J_Activity.class);
        } else if (textViewId == R.id.boxK) {
            intent = new Intent(this, K_Activity.class);
        } else if (textViewId == R.id.boxL) {
            intent = new Intent(this, L_Activity.class);
        } else if (textViewId == R.id.boxM) {
            intent = new Intent(this, M_Activity.class);
        } else if (textViewId == R.id.boxN) {
            intent = new Intent(this, N_Activity.class);
        } else if (textViewId == R.id.boxO) {
            intent = new Intent(this, O_Activity.class);
        } else if (textViewId == R.id.boxP) {
            intent = new Intent(this, P_Activity.class);
        } else if (textViewId == R.id.boxQ) {
            intent = new Intent(this, Q_Activity.class);
        } else if (textViewId == R.id.boxR) {
            intent = new Intent(this, R_Activity.class);
        } else if (textViewId == R.id.boxS) {
            intent = new Intent(this, S_Activity.class);
        } else if (textViewId == R.id.boxT) {
            intent = new Intent(this, T_Activity.class);
        } else if (textViewId == R.id.boxU) {
            intent = new Intent(this, U_Activity.class);
        } else if (textViewId == R.id.boxV) {
            intent = new Intent(this, V_Activity.class);
        } else if (textViewId == R.id.boxW) {
            intent = new Intent(this, W_Activity.class);
        } else if (textViewId == R.id.boxX) {
            intent = new Intent(this, X_Activity.class);
        } else if (textViewId == R.id.boxY) {
            intent = new Intent(this, Y_Activity.class);
        } else if (textViewId == R.id.boxZ) {
            intent = new Intent(this, Z_Activity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
    }


}