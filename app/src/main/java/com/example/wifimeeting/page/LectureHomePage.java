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

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.R;
import com.example.wifimeeting.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LectureHomePage extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lecturer_home_page, container, false);
        MaterialButton joinLectureButton = view.findViewById(R.id.join_lecture_button);

        final TextInputLayout portTextInput = view.findViewById(R.id.port_text_input);
        final TextInputEditText portEditText = view.findViewById(R.id.port_edit_text);
        final TextInputLayout lecturerNameTextInput = view.findViewById(R.id.lecturer_name_text_input);
        final TextInputEditText lecturerNameEditText = view.findViewById(R.id.lecturer_name_edit_text);
        final TextView serverTextView = view.findViewById(R.id.server_ip_text_view);

        serverTextView.setText(getWiFiIpAddress());
        portEditText.setText(Constants.DEFAULT_PORT);

        joinLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (portEditText.getText() == null || portEditText.getText().toString().trim().equals("")) {
                    portTextInput.setError(getString(R.string.port_mandatory));
                } else {
                    portTextInput.setError(null);
                }

                if (lecturerNameEditText.getText() == null || lecturerNameEditText.getText().toString().trim().equals("")) {
                    lecturerNameTextInput.setError(getString(R.string.lecturer_name_mandatory));
                } else {
                    lecturerNameTextInput.setError(null);
                }

                if(portTextInput.getError() == null && lecturerNameTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }
            }
        });

        return view;
    }

    private String getWiFiIpAddress (){
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }
}
