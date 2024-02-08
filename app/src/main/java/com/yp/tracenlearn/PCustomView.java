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

public class PCustomView extends View {
    private Bitmap mBitmap;


    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;

    private List<Point> nonTransparentPixels = new ArrayList<>(); //to check where the letter bitmap is
    private List<Point> strokeCoordinates = new ArrayList<>(); //to check where the user drawings are

    private int strokeColor = Color.BLACK;
    public interface NoStrokesCallback {
        void onNoStrokesDetected(String accuracyInfo);
    }
    private PCustomView.NoStrokesCallback noStrokesCallback;
    private Handler handler = new Handler();

    public PCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(strokeColor); //designing the look for the stroke - which is customisable ofc
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(70f);

    }
    public void setStrokeColor(int color) {
        this.strokeColor = color;
        mPaint.setColor(color); //we get the int from the colour panel and set it
        invalidate(); // redraw the canvas with the new stroke color
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // load the background image
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.letter_p);
        mBitmap = drawable.getBitmap();

        // scale the background image to fit the new size
        mBitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);

        // center the image in the canvas
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int imageWidth = mBitmap.getWidth();
        int imageHeight = mBitmap.getHeight();

        float left = (canvasWidth - imageWidth) / 2f;
        float top = (canvasHeight - imageHeight) / 2f;

        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, left, top, null);

        getNonTransparentPixels();


        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, null);

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


    private void startNoStrokesCountdown() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNoStrokesDialog();
            } //to help with showing the dialog pop up
        }, 3000);
    }
    private void showNoStrokesDialog() {
        // Notify the callback that no strokes were detected
        if (noStrokesCallback != null) {
            String accuracyInfo = getAccuracyInfo();
            noStrokesCallback.onNoStrokesDetected(accuracyInfo);
        }
    }
    public void setOnNoStrokesDetectedCallback(PCustomView.NoStrokesCallback callback) {
        this.noStrokesCallback = callback;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Vibrator vb = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                vb.vibrate(10000);
                mCurrentPath = new Path();  // start a new path for the current stroke
                mCurrentPath.moveTo(x, y);
                handler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentPath.lineTo(x, y);
                strokeCoordinates.add(new Point((int) x, (int) y));
                break;
            case MotionEvent.ACTION_UP:
                // add the current path to the list of paths
                vb.cancel();
                mPaths.add(mCurrentPath);

                //keeping track of count of strokes
                strokeCount++;
                Log.d("CustomView", "Stroke Coordinates Size: " + strokeCoordinates.size());




                startNoStrokesCountdown();
                // reset countdown


                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }







    //,ethod to find where the bitmap is
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

    public void logCoordinates() {
        // Log both stroke and non-transparent pixel coordinates
        Log.d("CustomView", "Stroke Coordinates: " + strokeCoordinates.toString());
        // Ensure non-transparent pixel coordinates are updated
        Log.d("CustomView", "Non-Transparent Pixel Coordinates: " + nonTransparentPixels.toString());
    }

    private String getAccuracyInfo() {
        // Ensure non-transparent pixel coordinates are updated

        int matchingCount = 0;
        int totalStrokeCoordinates = strokeCoordinates.size();
        int totalBitMapCoordinates = nonTransparentPixels.size();
        int diff = totalStrokeCoordinates - totalBitMapCoordinates;
        Log.d("CustomView", "Coordinate Difference: " + diff);


        for (Point strokePoint : strokeCoordinates) {
            if (nonTransparentPixels.contains(strokePoint)) {
                matchingCount++;
            }
        }

        //accuracy percentage
        double accuracy = (double) matchingCount / totalStrokeCoordinates* 100 ;


        if (strokeCount <= 2 && totalStrokeCoordinates > 150 && accuracy > 90) {
            return "Accuracy Score: " + accuracy + "%"; //letter is proper but also accuracy rate
        }
        else{
            return "NO: " + accuracy + "%"; //if letter is not proper
        }
    }

}
