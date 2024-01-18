package com.yp.tracenlearn;// CustomCanvasView.java
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ICustomView extends View {

    private Bitmap mBitmap;

    private Bitmap additionalBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;

    private List<Point> nonTransparentPixels = new ArrayList<>();
    private List<Point> strokeCoordinates = new ArrayList<>();


    public ICustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(70f);
        // Do not load the background image here
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Load the background image
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.i_letter_png_picture);
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

        getNonTransparentPixels();

        // Optionally, trigger a redraw after loading the background image
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, null);
        if (additionalBitmap != null) {
            canvas.drawBitmap(additionalBitmap, 0, 0, null);
        }

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
    private  int strokeCount;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Start a new path for the current stroke
                mCurrentPath = new Path();
                mCurrentPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentPath.lineTo(x, y);
                strokeCoordinates.add(new Point((int) x, (int) y));
                break;
            case MotionEvent.ACTION_UP:
                // Add the current path to the list of paths
                mPaths.add(mCurrentPath);
                strokeCount++;
                // Check if the complete path covers the entire bitmap

                calculateAndLogAccuracy();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }


    // Method to check if the complete path covers the entire bitmap
    // Method to check if the complete path covers the entire bitmap
    // Method to check if the complete path covers the entire bitmap
    // Method to check if the complete path covers the entire bitmap
    private static final float ERROR_THRESHOLD_PERCENTAGE = 90f; // Adjust this value as needed

    // Method to check if the complete path covers the majority of the bitmap
    // Adjust this value as needed

    // Method to check if the complete path covers the majority of the bitmap

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


    private void calculateAndLogAccuracy() {
        // Ensure non-transparent pixel coordinates are updated

        int matchingCount = 0;
        int totalStrokeCoordinates = strokeCoordinates.size();
        int totalBitMapCoordinates = nonTransparentPixels.size();
        int diff = totalStrokeCoordinates - totalBitMapCoordinates;
        Log.d("CustomView", "Coordinate Difference: " + totalStrokeCoordinates);
        Log.d("CustomView", "Coordinate Difference: " + diff);


        for (Point strokePoint : strokeCoordinates) {
            if (nonTransparentPixels.contains(strokePoint)) {
                matchingCount++;
            }
        }

        // Calculate accuracy percentage
        double accuracy = (double) matchingCount / totalStrokeCoordinates* 100 ;

        // Log the accuracy score
        if ( strokeCount == 3 && totalStrokeCoordinates > 100) {
            Log.d("CustomView", "Accuracy Score: " + accuracy + "%");}
        else{
            Log.d("CustomView", "NO " + accuracy + "%");
        }
    }


}







