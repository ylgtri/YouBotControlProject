package edu.gatech.gtri.visualservo.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

    private static Fragment mainFragment;
    private static FragmentManager fm;
    private static boolean hidden;
    private static LinearLayout side;
    private static RelativeLayout main;
    private static Animation fadeout;
    private Animation fadein;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_main);
        fm = this.getFragmentManager();
        mHandler = new Handler();
        mainFragment = fm.findFragmentById(R.id.mainFragment);
        side = (LinearLayout) findViewById(R.id.sidebarFragment);
        main = (RelativeLayout) findViewById(R.id.mainFragment);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        hidden = true;
        side.setVisibility(View.GONE);
    }

    public void toggleMode(View v) {
        if (hidden) {
            RightJoystick joystick = (RightJoystick) mainFragment.getView().findViewById(R.id.joystick2);
            CenteredSeekBar center2 = (CenteredSeekBar) mainFragment.getView().findViewById(R.id.center2);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    side.setVisibility(View.VISIBLE);
                }
            };
            mHandler.postDelayed(r, 200);
            joystick.startAnimation(fadein);
            center2.startAnimation(fadein);
            side.setVisibility(View.VISIBLE);
            hidden = false;
        } else {
            RightJoystick joystick = (RightJoystick) mainFragment.getView().findViewById(R.id.joystick2);
            CenteredSeekBar center2 = (CenteredSeekBar) mainFragment.getView().findViewById(R.id.center2);
            center2.startAnimation(fadeout);
            joystick.startAnimation(fadeout);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    side.setVisibility(View.GONE);
                }
            };
            mHandler.postDelayed(r, 200);
            hidden = true;
        }
    }
}
