package com.yp.tracenlearn;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import android.graphics.Color;

@RunWith(AndroidJUnit4.class)
public class A_ActivityTest {

    @Rule
    public ActivityTestRule<A_Activity> activityRule = new ActivityTestRule<>(A_Activity.class);

    private A_Activity activity;

    @Before
    public void setUp() {
        activity = activityRule.getActivity();
    }

    @Test
    public void testGetColorForIndex_validIndex_returnsCorrectColor() {
        // Given
        int validIndex = 1; // assuming a valid index for testing

        // When
        int color = activity.getColorForIndex(validIndex);

        // Then
        assertEquals(Color.parseColor("#f1c40f"), color); // check if the expected color is returned
    }

    @Test
    public void testGetColorForIndex_invalidIndex_returnsDefaultColor() {
        // Given
        int invalidIndex = 10; // assuming an invalid index for testing

        // When
        int color = activity.getColorForIndex(invalidIndex);

        // Then
        assertEquals(Color.BLACK, color); // check if the default color is returned for an invalid index
    }
}

