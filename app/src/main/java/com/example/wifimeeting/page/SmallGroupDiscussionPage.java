package com.example.wifimeeting.page;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.R;
import com.example.wifimeeting.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;

public class SmallGroupDiscussionPage extends Fragment {

    AutoCompleteTextView memberListTextView;
    AutoCompleteTextView groupListTextView;

    boolean[] selectedMember;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.small_group_discussion_page, container, false);

        groupListTextView = view.findViewById(R.id.select_group);
        String groupNameList[] = {"Sneakers", "Fighters", "Bolters", "Lions"};
        ArrayAdapter arrayAdapterGroupList = new ArrayAdapter(this.getContext(), R.layout.option_group_item, groupNameList);

        //to make default value
//        groupListTextView.setText(arrayAdapterGroupList.getItem(0).toString(), false);
        groupListTextView.setAdapter(arrayAdapterGroupList);


        /**
         * Material Design Dialog
         */
        memberListTextView = view.findViewById(R.id.select_members);
        String memberArray[] = {"Chamindu", "Dulanjana", "Yasara", "Udesh"};
        ArrayList<Integer> memberList = new ArrayList<>();

        selectedMember = new boolean[memberArray.length];
        memberListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(view.getContext(), R.style.ThemeOverlay_App_MaterialAlertDialog);
                builder.setTitle("Select Member");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(memberArray, selectedMember, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            memberList.add(i);
                            Collections.sort(memberList);
                        } else {
                            memberList.remove(Integer.valueOf(i));
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for (int j = 0; j < memberList.size(); j++) {
                            stringBuilder.append(memberArray[memberList.get(j)]);

                            if (j != memberList.size() - 1) {
                                stringBuilder.append(Constants.MEMBERS_SEPARATOR);
                            }
                        }

                        memberListTextView.setText(stringBuilder.toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        MaterialButton joinButton = view.findViewById(R.id.join_button);
        MaterialButton createButton = view.findViewById(R.id.create_button);

        final TextInputLayout groupNameTextInput = view.findViewById(R.id.group_name_text_input);
        final TextInputEditText groupNameEditText = view.findViewById(R.id.group_name_edit_text);

        final TextInputLayout selectMembersTextInput = view.findViewById(R.id.select_members_text_input);
        final TextInputLayout selectGroupTextInput = view.findViewById(R.id.select_group_text_input);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupListTextView.getText() == null || groupListTextView.getText().toString().trim().equals("")) {
                    selectGroupTextInput.setError(getString(R.string.group_mandatory));
                } else {
                    selectGroupTextInput.setError(null);
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupNameEditText.getText() == null || groupNameEditText.getText().toString().trim().equals("")) {
                    groupNameTextInput.setError(getString(R.string.group_name_mandatory));
                } else {
                    groupNameTextInput.setError(null);
                }

                if (memberListTextView.getText() == null || memberListTextView.getText().toString().trim().equals("")) {
                    selectMembersTextInput.setError(getString(R.string.members_mandatory));
                } else if (memberListTextView.getText().toString().trim().split(Constants.MEMBERS_SEPARATOR).length > Constants.SMALL_GROUP_DISCUSSION_MEMBER_COUNT){
                    selectMembersTextInput.setError(getString(R.string.max_count_exceeded));
                } else {
                    selectMembersTextInput.setError(null);
                }

                if (groupNameTextInput.getError() == null && selectMembersTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }
            }
        });

        return view;
    }
}
