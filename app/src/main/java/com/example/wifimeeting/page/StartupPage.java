package com.example.wifimeeting.page;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.navigation.NavigationHost;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment representing the login screen for WifiMeeting.
 */
public class StartupPage extends Fragment {

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.startup_page, container, false);

        MaterialButton studentButton = view.findViewById(R.id.student_button);
        MaterialButton lecturerButton = view.findViewById(R.id.lecturer_button);

        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isWifiConnected(view))
                    ((NavigationHost) getActivity()).navigateTo(new StudentHomePage(), true);
            }
        });

        lecturerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isWifiConnected(view))
                    ((NavigationHost) getActivity()).navigateTo(new LecturerLoginPage(), true);
            }
        });

        return view;
    }

    public boolean isWifiConnected(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ( wifi == null || !wifi.isConnected()) {
            Snackbar.make(view, R.string.not_wifi_connected, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
