package edu.gatech.gtri.visualservo.android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SidebarFragment extends Fragment {
    private static String motors =
            "" +
            "" +
            "";
    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_sidebar, container, false);
        } catch (InflateException e) {

        }
        return view;
    }


}
