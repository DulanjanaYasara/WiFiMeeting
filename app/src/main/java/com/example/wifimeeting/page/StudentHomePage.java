package com.example.wifimeeting.page;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class StudentHomePage extends Fragment {

    MaterialButton joinLectureButton;
    MaterialButton smallGroupDiscussionButton;
    TextInputLayout portTextInput;
    TextInputEditText portEditText;
    TextInputLayout studentNameTextInput;
    TextInputEditText studentNameEditText;
    TextView serverTextView;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.student_home_page, container, false);

        joinLectureButton = view.findViewById(R.id.join_lecture_button);
        smallGroupDiscussionButton = view.findViewById(R.id.small_group_discussion_button);
        portTextInput = view.findViewById(R.id.port_text_input);
        portEditText = view.findViewById(R.id.port_edit_text);
        studentNameTextInput = view.findViewById(R.id.student_name_text_input);
        studentNameEditText = view.findViewById(R.id.student_name_edit_text);
        serverTextView = view.findViewById(R.id.server_ip);

        serverTextView.setText(getWiFiIpAddress());
        portEditText.setText(Constants.DEFAULT_PORT);

        smallGroupDiscussionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidations();
                if(portTextInput.getError() == null && studentNameTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new SmallGroupDiscussionPage(), true);
                }

            }
        });

        joinLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidations();
                if(portTextInput.getError() == null && studentNameTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }

            }
        });

        return view;
    }

    private void checkValidations (){
        if (portEditText.getText() == null || portEditText.getText().toString().trim().equals("")) {
            portTextInput.setError(getString(R.string.port_mandatory));
        } else {
            portTextInput.setError(null);
        }

        if (studentNameEditText.getText() == null || studentNameEditText.getText().toString().trim().equals("")) {
            studentNameTextInput.setError(getString(R.string.student_name_mandatory));
        } else {
            studentNameTextInput.setError(null);
        }
    }

    private String getWiFiIpAddress (){
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }
}
