package com.yp.tracenlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class B_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        BCustomView customBCanvas = findViewById(R.id.customBCanvas);
        LinearLayout colorPanel = findViewById(R.id.colorPanel);

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


