package com.example.wifimeeting.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.R;
import com.google.android.material.button.MaterialButton;

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
                ((NavigationHost) getActivity()).navigateTo(new StudentHomePage(), true);
            }
        });

        lecturerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationHost) getActivity()).navigateTo(new LecturerLoginPage(), true);
            }
        });

        return view;
    }
}
