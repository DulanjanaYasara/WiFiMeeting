package com.example.wifimeeting.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.R;
import com.example.wifimeeting.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class StudentHomePage extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.student_home_page, container, false);

        MaterialButton joinLectureButton = view.findViewById(R.id.join_lecture_button);
        MaterialButton smallGroupDiscussionButton = view.findViewById(R.id.small_group_discussion_button);

        final TextInputLayout portTextInput = view.findViewById(R.id.port_text_input);
        final TextInputEditText portEditText = view.findViewById(R.id.port_edit_text);

        final TextInputLayout studentNameTextInput = view.findViewById(R.id.student_name_text_input);
        final TextInputEditText studentNameEditText = view.findViewById(R.id.student_name_edit_text);

        portEditText.setText(Constants.DEFAULT_PORT);

        smallGroupDiscussionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationHost) getActivity()).navigateTo(new SmallGroupDiscussionPage(), true);
            }
        });

        joinLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                if(portTextInput.getError() == null && studentNameTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }

            }
        });

        return view;
    }
}
