package com.example.wifimeeting.page;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.moduleitem.ListModuleItemAdapter;
import com.example.wifimeeting.components.moduleitem.ModuleDetailItem;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.example.wifimeeting.utils.LectureSessionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class StudentHomePage extends Fragment {

    MaterialButton joinLectureButton;
    MaterialButton smallGroupDiscussionButton;
    TextInputLayout selectModuleTextInput;
    TextInputLayout studentNameTextInput;
    TextInputEditText studentNameEditText;
    AutoCompleteTextView moduleListTextView;
    ArrayAdapter<ModuleDetailItem> listModuleItemAdapter;
    private String selectedModule;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.student_home_page, container, false);

        joinLectureButton = view.findViewById(R.id.join_lecture_button);
        smallGroupDiscussionButton = view.findViewById(R.id.small_group_discussion_button);
        selectModuleTextInput = view.findViewById(R.id.select_module_text_input);
        moduleListTextView = view.findViewById(R.id.select_module);
        studentNameTextInput = view.findViewById(R.id.student_name_text_input);
        studentNameEditText = view.findViewById(R.id.student_name_edit_text);

        smallGroupDiscussionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkValidations(false);
                if(selectModuleTextInput.getError() == null && studentNameTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(GroupDiscussionMember.NAME.toString(), Objects.requireNonNull(studentNameEditText.getText()).toString().trim());

                        GroupDiscussionLobbyPage discussionPage = new GroupDiscussionLobbyPage();
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

                checkValidations(true);
                if(selectModuleTextInput.getError() == null && studentNameTextInput.getError() == null) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        Snackbar.make(view, R.string.permission_mandatory, Snackbar.LENGTH_SHORT).show();
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putString(LectureSessionMember.NAME.toString(), Objects.requireNonNull(studentNameEditText.getText()).toString().trim());
                        bundle.putBoolean(LectureSessionMember.IS_MUTE.toString(), true);

                        LectureSessionPage lectureSessionPage = new LectureSessionPage();
                        lectureSessionPage.setArguments(bundle);
                        // Navigate to the next Fragment
                        ((NavigationHost) getActivity()).navigateTo(lectureSessionPage, true);
                    }
                }

            }
        });

        initiateGroupListTextView(view);
        return view;
    }

    private void initiateGroupListTextView(View view){
        ArrayList<ModuleDetailItem> moduleDetailList = new ArrayList<ModuleDetailItem>();
        moduleDetailList.add(new ModuleDetailItem("CS5092 - Programming","",12L));
        moduleDetailList.add(new ModuleDetailItem("CS5090 - Networking","",12L));

        listModuleItemAdapter = new ListModuleItemAdapter(this.requireContext(), moduleDetailList);
        moduleListTextView.setAdapter(listModuleItemAdapter);

        moduleListTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < parent.getCount()) {
                    ModuleDetailItem item = (ModuleDetailItem)parent.getItemAtPosition(position);
                    selectedModule = item.getModuleCode();
                } else {
                    selectedModule = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where no item is selected
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        // Clear the selected value
        selectedModule = null;
        if(moduleListTextView!=null)
            moduleListTextView.setText("");
        if(listModuleItemAdapter!=null)
            listModuleItemAdapter.clear();
    }

    private void checkValidations (boolean isLecture){
        if(isLecture){
            if (moduleListTextView.getText() == null || moduleListTextView.getText().toString().trim().equals("")) {
                selectModuleTextInput.setError(getString(R.string.module_required));
            } else {
                selectModuleTextInput.setError(null);
            }
        }

        if (studentNameEditText.getText() == null || studentNameEditText.getText().toString().trim().equals("")) {
            studentNameTextInput.setError(getString(R.string.student_name_mandatory));
        } else {
            studentNameTextInput.setError(null);
        }
    }

}
