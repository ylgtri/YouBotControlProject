package edu.gatech.gtri.visualservo.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.graphics.Color.argb;

public class CenteredSeekBar extends View {

    private Context mContext;
    private Handler mHandler;
    private static int width;
    private static int height;
    private static int margins = 50;
    private static Paint line = new Paint();
    private static Paint progress = new Paint();
    private static Paint none = new Paint();
    private float thickness = 7;
    private Bitmap progressMark;
    private Bitmap progressMarkPressed;
    private float percentage;
    private int length;
    private boolean pressed;

    public CenteredSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        line.setColor(argb(100, 135, 135, 135));
        line.setStrokeWidth(thickness);
        progress.setColor(context.getResources().getColor(android.R.color.holo_blue_light));
        progress.setStrokeWidth(thickness);
        progressMark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scrubber_control_normal_holo);
        progressMarkPressed = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.scrubber_control_pressed_holo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
        length = width - 2 * margins;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0 + margins, height / 2, width - margins, height / 2, line);
        int center = width / 2;
        canvas.drawLine(center + percentage * length, height / 2, center, height / 2, progress);
        if (pressed) {
            float c = center + percentage * length;
            canvas.drawBitmap(progressMarkPressed, c - progressMarkPressed.getWidth() / 2, (height / 2) -
                    progressMarkPressed.getHeight() / 2, none);
        } else {
            float c = center + percentage * length;
            canvas.drawBitmap(progressMark, c - progressMark.getWidth() / 2, (height / 2) -
                    progressMark.getHeight() / 2, none);
        }
        mHandler.postDelayed(redraw, 16);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moved(x, y, false);
                break;
            case MotionEvent.ACTION_MOVE:
                moved(x, y, false);
                break;
            case MotionEvent.ACTION_UP:
                moved(x, y, true);
                break;
        }
        return true;
    }

    private class FollowLine implements Runnable {

        private float speed = 0.05F;
        private int fps = 16;
        private float x;

        public FollowLine(float x) {
            this.x = x;
        }

        @Override
        public void run() {
            int frames = Math.abs((int) (x / speed));
            // Log.d("FRAMES", Integer.toString(frames) + ", " + x);
            for (int i = 0; i <= frames; i++) {
                if (x > 0) {
                    x = x - speed;
                    if (x >= 0) {
                    percentage = x;
                    }
                    try {
                        Thread.sleep(fps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.postDelayed(redraw, 0);
                } else if (x < 0 ) {
                    x = x + speed;
                    percentage = x;
                    try {
                        Thread.sleep(fps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.postDelayed(redraw, 0);

                }
                    percentage = 0;
                    mHandler.postDelayed(redraw, fps);
            }
        }
    }

    private Runnable redraw = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };


    public void moved(float x, float y, boolean up) {
        if (x < margins) {
            x = margins;
        }
        if (x > (width - margins)) {
            x = width - margins;
        }
        if (up) {
            new Thread(new FollowLine(percentage)).start();
        } else {
            float c = x - (width / 2);
            percentage = (c / length);
            if (percentage > 1) {
                percentage = 1;
            }
            if (percentage < -1) {
                percentage = -1;
            }
        }
        pressed = !up;
        invalidate();
    }
}