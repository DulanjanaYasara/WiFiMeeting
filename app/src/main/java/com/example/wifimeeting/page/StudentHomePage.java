package com.example.wifimeeting.page;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.utils.AddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.MyDetails;
import com.example.wifimeeting.utils.Role;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class StudentHomePage extends Fragment {

    MaterialButton joinLectureButton;
    MaterialButton smallGroupDiscussionButton;
    TextInputLayout portTextInput;
    TextInputEditText portEditText;
    TextInputLayout studentNameTextInput;
    TextInputEditText studentNameEditText;

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
        portEditText.setText(Constants.DEFAULT_PORT);

        smallGroupDiscussionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidations();
                if(portTextInput.getError() == null && studentNameTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(MyDetails.NAME.toString(), Objects.requireNonNull(studentNameEditText.getText()).toString().trim());
                        bundle.putString(MyDetails.PORT.toString(), Objects.requireNonNull(portEditText.getText()).toString());

                        SmallGroupDiscussionPage discussionPage = new SmallGroupDiscussionPage();
                        discussionPage.setArguments(bundle);
                        // Navigate to the next Fragment
                        ((NavigationHost) getActivity()).navigateTo(discussionPage, true);
                    }
                }

            }
        });

        joinLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidations();
                if(portTextInput.getError() == null && studentNameTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(MyDetails.NAME.toString(), Objects.requireNonNull(studentNameEditText.getText()).toString().trim());
                        bundle.putBoolean(MyDetails.IS_MUTE.toString(), true);
                        bundle.putString(MyDetails.ROLE.toString(), Role.STUDENT.toString());

                        MeetingPage meetingPage = new MeetingPage();
                        meetingPage.setArguments(bundle);
                        // Navigate to the next Fragment
                        ((NavigationHost) getActivity()).navigateTo(meetingPage, true);
                    }
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

}
