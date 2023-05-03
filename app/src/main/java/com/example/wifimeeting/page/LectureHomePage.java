package com.example.wifimeeting.page;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.LectureSessionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class LectureHomePage extends Fragment {

    MaterialButton joinLectureButton;
    TextInputLayout portTextInput, lecturerNameTextInput;
    TextInputEditText portEditText, lecturerNameEditText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lecturer_home_page, container, false);

        joinLectureButton = view.findViewById(R.id.join_lecture_button);
        portTextInput = view.findViewById(R.id.port_text_input);
        portEditText = view.findViewById(R.id.port_edit_text);
        lecturerNameTextInput = view.findViewById(R.id.lecturer_name_text_input);
        lecturerNameEditText = view.findViewById(R.id.lecturer_name_edit_text);
        portEditText.setText(String.valueOf(Constants.DEFAULT_AUDIO_CALL_PORT));

        joinLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               joinLectureValidations();

                if(portTextInput.getError() == null && lecturerNameTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
				    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(LectureSessionMember.NAME.toString(), Objects.requireNonNull(lecturerNameEditText.getText()).toString().trim());
                        bundle.putBoolean(LectureSessionMember.IS_MUTE.toString(), false);
                        bundle.putInt(LectureSessionMember.PORT.toString(), Integer.parseInt(portEditText.getText().toString()));

                        LectureSessionPage lectureSessionPage = new LectureSessionPage();
                        lectureSessionPage.setArguments(bundle);
                        // Navigate to the next Fragment
                        ((NavigationHost) getActivity()).navigateTo(lectureSessionPage, true);
                    }
                }
            }
        });

        return view;
    }

    private void joinLectureValidations(){
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
    }

}
