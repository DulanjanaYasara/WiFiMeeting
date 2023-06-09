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
import com.example.wifimeeting.utils.MulticastAddressGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class LecturerHomePage extends Fragment {

    MaterialButton startLectureButton;
    TextInputLayout moduleCodeTextInput, lecturerNameTextInput, multicastGroupTextInput;
    TextInputEditText moduleCodeEditText, lecturerNameEditText, multicastGroupEditText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lecturer_home_page, container, false);

        startLectureButton = view.findViewById(R.id.start_lecture_button);
        moduleCodeTextInput = view.findViewById(R.id.module_code_text_input);
        moduleCodeEditText = view.findViewById(R.id.module_code_edit_text);
        lecturerNameTextInput = view.findViewById(R.id.lecturer_name_text_input);
        lecturerNameEditText = view.findViewById(R.id.lecturer_name_edit_text);
        multicastGroupTextInput = view.findViewById(R.id.multicast_group_address_text_input);
        multicastGroupEditText = view.findViewById(R.id.multicast_group_address_edit_text);

        multicastGroupEditText.setText(MulticastAddressGenerator.generateMulticastAddress());

        startLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               joinLectureValidations();

                if(moduleCodeTextInput.getError() == null && lecturerNameTextInput.getError() == null && multicastGroupTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
				    else {

                        //multicast group address validations
                        String multicastGroupAddress = multicastGroupEditText.getText().toString().trim();
                        if(!MulticastAddressGenerator.validateMulticastGroupAddress(multicastGroupAddress)){
                            Snackbar.make(view, R.string.multicast_group_address_validation_error, Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString(LectureSessionMember.NAME.toString(), Objects.requireNonNull(lecturerNameEditText.getText()).toString().trim());
                        bundle.putString(LectureSessionMember.ROLE.toString(), Constants.LECTURER_ROLE);
                        bundle.putString(LectureSessionMember.MODULE_CODE.toString(), Objects.requireNonNull(moduleCodeEditText.getText()).toString().trim());
                        bundle.putString(LectureSessionMember.MULTICAST_GROUP_ADDRESS.toString(), Objects.requireNonNull(multicastGroupEditText.getText()).toString().trim());

                        LectureSessionPage lectureSessionPage = new LectureSessionPage();
                        lectureSessionPage.setArguments(bundle);
                        // Navigate to the next Fragment
                        ((NavigationHost) getActivity()).navigateTo(lectureSessionPage, true);
                    }
                }
            }
        });
        multicastGroupTextInput.setEndIconOnClickListener(multicastGroupRefreshIconClickEvent());

        return view;
    }

    private void joinLectureValidations(){
        if (moduleCodeEditText.getText() == null || moduleCodeEditText.getText().toString().trim().equals("")) {
            moduleCodeTextInput.setError(getString(R.string.module_code_mandatory));
        } else {
            moduleCodeTextInput.setError(null);
        }

        if (lecturerNameEditText.getText() == null || lecturerNameEditText.getText().toString().trim().equals("")) {
            lecturerNameTextInput.setError(getString(R.string.lecturer_name_mandatory));
        } else {
            lecturerNameTextInput.setError(null);
        }

        if (multicastGroupEditText.getText() == null || multicastGroupEditText.getText().toString().trim().equals("")) {
            multicastGroupTextInput.setError(getString(R.string.multicast_group_address_mandatory));
        } else {
            multicastGroupTextInput.setError(null);
        }
    }


    private View.OnClickListener multicastGroupRefreshIconClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multicastGroupEditText.setText(MulticastAddressGenerator.generateMulticastAddress());
            }
        };
    }

}
