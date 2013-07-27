package edu.gatech.gtri.visualservo.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.graphics.Color.argb;

public class RightJoystick extends View {

    private Context mContext;
    private Bitmap progressMark;
    private Bitmap progressMarkPressed;
    private Paint p = new Paint();
    private Paint clip = new Paint();
    private Paint rim = new Paint();
    private Paint center = new Paint();
    private Paint centerbevel = new Paint();
    private Paint indicator = new Paint();
    private static int width;
    private static int height;
    private static float cx;
    private static float cy;
    private boolean pressed;
    private boolean hasSet = false;
    private final int FRAMERATE = 16; // For some reason, choreographer isn't working for this view (might be global surface?)
    private Handler mHandler; // I'll just use a handler instead, and sleep for the frame
    // drawing this should be async
    private float margins = 100;
    private float thickness = 7;
    private boolean isOut = false;
    private RectF oval = new RectF();

    public RightJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        clip.setColor(argb(20, 0, 0, 0));
        clip.setAntiAlias(true);
        rim.setColor(argb(100, 135, 135, 135));
        rim.setAntiAlias(true);
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(thickness);
        center.setColor(argb(51, 135, 135, 135));
        center.setAntiAlias(true);
        centerbevel.setColor(argb(51, 200, 200, 200));
        centerbevel.setAntiAlias(true);
        indicator.setColor(context.getResources().getColor(android.R.color.holo_blue_light));
        indicator.setAntiAlias(true);
        indicator.setStrokeWidth(thickness);
        indicator.setStyle(Paint.Style.STROKE);
        pressed = true;
        initDrawable();
    }

    public void setCenter() {
        cx = (width - progressMark.getWidth()) / 2;
        cy = (width - progressMark.getHeight()) / 2;
        invalidate();
    }

    public void initDrawable() {
        progressMark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scrubber_control_normal_holo);
        progressMarkPressed = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.scrubber_control_pressed_holo);
    }

    private Runnable redraw = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private class FollowLine implements Runnable {

        private float ix;
        private float iy;
        private float fx;
        private float fy;
        private int fps = 16;
        private float speed = 10;

        public FollowLine(float ix, float iy, float fx, float fy) {
            this.ix = ix;
            this.iy = iy;
            this.fx = fx;
            this.fy = fy;
            RightJoystick.cx = (int) this.ix;
            RightJoystick.cy = (int) this.iy;
        }

        @Override
        public void run() {
            float plusx = (fx - ix) / speed;
            float plusy = (fy - iy) / speed;
            for (int i = 0; i < speed; i++) {
                if ((int) fx != (int) ix && (int) fy != (int) iy) {
                    if (pressed) {
                        cx = cx + plusx;
                        cy = cy + plusy;
                        try {
                            Thread.sleep(fps);
                            mHandler.postDelayed(redraw, FRAMERATE);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    /* public float[] getInsidePoint(float x, float y) {
        float[] fl = new float[2];
        float middle = width / 2;
        float r = (width - margins) / 2;
        x = x - middle;
        y = y - middle;
        float d = Math.abs(pythagoras(x, y, middle, middle));
        if (d < r) {
            fl[0] = x;
            fl[1] = y;
            return fl;
        } else {
            float angle = (float) Math.toDegrees(Math.atan2((middle - y), (middle - x)));

        }
    } */

    public static float pythagoras(float x, float y) {
        double x2 = (double) x;
        double y2 = (double) y;
        double mx = (double) (width / 2);
        double my = (double) (height / 2);
        return (float) Math.abs(Math.sqrt(Math.pow(x2 - mx, 2) + Math.pow(y2 - my, 2)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle((width) / 2, (width) / 2, (int) ((width - margins) / 2), clip);
        canvas.drawCircle((width) / 2, (width) / 2, (int) ((width - margins) / 2), rim);
        if(!hasSet) {
            setCenter();
            hasSet = true;
        }
        canvas.drawCircle((width) / 2, (width) / 2, progressMarkPressed.getWidth() / 6, center);
        if (!pressed) {
            canvas.drawCircle((width) / 2, ((width) / 2) - 1, progressMarkPressed.getWidth() / 6, centerbevel);
            canvas.drawCircle((width) / 2, (width) / 2, progressMarkPressed.getWidth() / 6, clip);
            canvas.drawCircle((width) / 2, (width) / 2, progressMarkPressed.getWidth() / 6, center);
            canvas.drawBitmap(progressMarkPressed, cx, cy, p);
        } else {
            canvas.drawBitmap(progressMark, cx, cy, p);
        }
        float fx = cx + (progressMarkPressed.getWidth() / 2);
        float fy = cy + (progressMarkPressed.getWidth() / 2);
        float angle = getAngle(fx, fy) - 90;
        float distance = pythagoras(fx, fy) / 3;
        if (!(distance * 3 <= 3)) {
            canvas.drawArc(oval, angle - (distance / 2), distance, false, indicator);
        }
        mHandler.postDelayed(redraw, FRAMERATE);
    }

    // could be useful, but isn't needed at the moment
    /* public float[] getMarkCenter(float x, float y) {
        float[] values = new float[2];
        values[0] = x + (progressMarkPressed.getHeight() / 2);
        values[1] = y + (progressMarkPressed.getWidth() / 2);
        return values;
    } */

    public static float getAngle(float x, float y) {
        double theta = Math.atan2(y - (width / 2), x - (width / 2));
        theta += Math.PI/2.0;
        float angle = (float) Math.toDegrees(theta);
        if (angle < 0) {
            angle += 360;
        }
        // Log.d("PYTHAGORAS", "angle " + angle);
        return angle;
    }

    public float getValidDistance() {
        return (width - margins - thickness - progressMarkPressed.getWidth()) / 2;
    }

    public float[] toRect(float a, float r) {
        a = a - 90;
        float[] values = new float[2];
        values[0] = (float) (r * Math.cos(Math.toRadians(a)));
        values[1] = (float) (r * Math.sin(Math.toRadians(a)));
        return values;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
        oval.set(margins / 2, margins / 2, width - margins / 2, height - margins / 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float ax = x;
        float ay = y;
        if (pythagoras(ax, ay) >= getValidDistance()) {
            float[] xy = toRect(getAngle(ax, ay), getValidDistance());
            ax = xy[0] + width / 2;
            ay = xy[1] + width / 2;
            // Log.d("PYTHAGORAS", Float.toString(distance) + " max " + getValidDistance());
        }
        boolean up = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moved(ax, ay, up);
                break;
            case MotionEvent.ACTION_MOVE:
                if (ax < width && ay < height) {
                    moved(ax, ay, up);
                } else {
                    up = true;
                    if (!isOut) {
                        isOut = true;
                        moved(ax, ay, up);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                up = true;
                isOut = false;
                moved(ax, ay, up);
                break;
        }
        return true;
    }

    private void moved(float x, float y, boolean up) {
        pressed = up;
        x = x - (progressMarkPressed.getWidth() / 2);
        y = y - (progressMarkPressed.getWidth() / 2);
        if (up) {
            int mx = (width - progressMark.getWidth()) / 2;
            int my = (height - progressMark.getHeight()) / 2;
            new Thread(new FollowLine(x, y, mx, my)).start();
        } else {
            cx = x;
            cy = y;
            // Log.d("ANGLE", Float.toString(getAngle(x, y)));
        }
        invalidate();
    }

    // Get the current XY value
    public static float[] getXY() {
        float[] f = new float[2];
        f[0] = cx;
        f[1] = cy;
        return f;
    }

    // Get the current A (Theta) R value
    public static float[] getAR() {
        float[] f = new float[2];
        f[0] = getAngle(cx, cy);
        f[1] = pythagoras(cx, cy);
        return f;
    }
}