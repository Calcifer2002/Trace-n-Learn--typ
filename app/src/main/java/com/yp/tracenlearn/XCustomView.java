package com.yp.tracenlearn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class XCustomView extends View {
    private Bitmap mBitmap; // Letter bitmap

    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;

    private List<Point> nonTransparentPixels = new ArrayList<>(); // To check where the letter bitmap is
    private List<Point> strokeCoordinates = new ArrayList<>(); // To check where the user drawings are

    private int strokeColor = Color.BLACK; // Default stroke color
    public interface NoStrokesCallback {      // If no strokes for 3 seconds, we have this callback with the accuracy data passed in
        void onNoStrokesDetected(String accuracyInfo);
    }
    private XCustomView.NoStrokesCallback noStrokesCallback;
    private Handler handler = new Handler(); // To handle delays

    public XCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // When the custom view begins, we initialize it with default stroke properties and code to help keep track of movement
    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(strokeColor); // Designing the look for the stroke - which is customizable
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(70f);
    }

    // To set stroke color that we get from X_Activity color panel, so we pass the index into this
    public void setStrokeColor(int color) {
        this.strokeColor = color;
        mPaint.setColor(color); // We get the int from the color panel and set it
        invalidate(); // Redraw the canvas with the new stroke color
    }

    /*
    When the size of view changes we need to redo bitmap accordingly, so we put it in canvas and center it after scaling
    the image and then we obtain which info about the non-transparent pixels which we will be using to calculate
    accuracy- after seeing how many of non-transparent pixels the user drew over
    */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Load the background image
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.letter_x);
        mBitmap = drawable.getBitmap();

        // Scale the background image to fit the new size
        mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);

        // Center the image in the canvas
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int imageWidth = mBitmap.getWidth();
        int imageHeight = mBitmap.getHeight();

        float left = (canvasWidth - imageWidth) / 2f;
        float top = (canvasHeight - imageHeight) / 2f;

        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, left, top, null);

        getNonTransparentPixels(); // To find the list of over what the image is lying so we can compare it with stroke

        invalidate();
    }

    // This is responsible for all the drawing in the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, null); // Draw the image

        // Draw all paths on the canvas
        for (Path path : mPaths) {
            canvas.drawPath(path, mPaint);
        }

        // Draw the current path
        if (mCurrentPath != null) {
            canvas.drawPath(mCurrentPath, mPaint);
        }
    }

    // Declare a global variable to store the entire path drawn so far
    private List<Path> mPaths = new ArrayList<>();
    private Path mCurrentPath;
    private int strokeCount = 0;

    // Function to check if no strokes in the last 3 seconds
    private void startNoStrokesCountdown() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNoStrokesDialog();
            } // To help with showing the dialog pop up
        }, 3000);
    }

    // Notify the callback that no strokes were detected
    private void showNoStrokesDialog() {
        if (noStrokesCallback != null) {
            String accuracyInfo = getAccuracyInfo();
            noStrokesCallback.onNoStrokesDetected(accuracyInfo);
        }
    }

    public void setOnNoStrokesDetectedCallback(XCustomView.NoStrokesCallback callback) {
        this.noStrokesCallback = callback;
    }

    /*
    This function tracks the path user is drawing, records stroke coordinates, and provides haptic feedback
    also ensures the 3-second timeout by calling startNoStrokesCountdown();
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Vibrator vb = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        float x = event.getX();
        float y = event.getY(); // Haptic feedback

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                vb.vibrate(10000);
                mCurrentPath = new Path();  // Start a new path for the current stroke
                mCurrentPath.moveTo(x, y);
                handler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentPath.lineTo(x, y);
                strokeCoordinates.add(new Point((int) x, (int) y));
                break;
            case MotionEvent.ACTION_UP:
                // Add the current path to the list of paths
                vb.cancel();
                mPaths.add(mCurrentPath);

                // Keeping track of the count of strokes
                strokeCount++;
                Log.d("CustomView", "Stroke Coordinates Size: " + strokeCoordinates.size());

                startNoStrokesCountdown();
                // Reset countdown

                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    // Method to find where the bitmap is
    private void getNonTransparentPixels() {
        nonTransparentPixels.clear();
        for (int x = 0; x < mBitmap.getWidth(); x++) {
            for (int y = 0; y < mBitmap.getHeight(); y++) {
                if (Color.alpha(mBitmap.getPixel(x, y)) != 0) {
                    // Non-transparent pixel found, add its coordinates to the list
                    nonTransparentPixels.add(new Point(x, y));
                }
            }
        }
    }

    // To troubleshoot, we log the coordinates
    public void logCoordinates() {
        // Log both stroke and non-transparent pixel coordinates
        Log.d("CustomView", "Stroke Coordinates: " + strokeCoordinates.toString());
        // Ensure non-transparent pixel coordinates are updated
        Log.d("CustomView", "Non-Transparent Pixel Coordinates: " + nonTransparentPixels.toString());
    }

    /*
    This is the function where we calculate the accuracy information, we see how many stroke points match the
    same as the ones the image is over and we calculate accuracy
    */
    private String getAccuracyInfo() {
        // Ensure non-transparent pixel coordinates are updated

        int matchingCount = 0;
        int totalStrokeCoordinates = strokeCoordinates.size();
        int totalBitMapCoordinates = nonTransparentPixels.size();
        int diff = totalStrokeCoordinates - totalBitMapCoordinates;
        Log.d("CustomView", "Coordinate Difference: " + diff);

        for (Point strokePoint : strokeCoordinates) {
            if (nonTransparentPixels.contains(strokePoint)) {  // Checking commonality between user stroke and image
                matchingCount++;
            }
        }

        // Accuracy percentage

        double accuracy = (double) matchingCount / totalStrokeCoordinates * 100;

        //Different accuracy messages for different situations
        if (strokeCount <= 3 && totalStrokeCoordinates > 100 && accuracy > 90) {
            return "Accuracy Score: " + accuracy + "%"; //Accurate letter, max 3 strokes to achieve X

        } else if (strokeCount > 3) {
            return "NO: " + accuracy + "%" + "many"; //If letter is drawn with way too many strokes
        } else if (totalStrokeCoordinates < 100 && accuracy > 90) {
            return "NO: " + accuracy + "%" + "slow!!"; //If the letter was drawn too quick
        }
        else {
            return "NO: " + accuracy + "%"; //If its completely unproper
        }}



}
