package com.example.wifimeeting.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.usecase.smallgroupdiscussion.DiscussionGroupItem;
import com.example.wifimeeting.usecase.smallgroupdiscussion.ListGroupItemAdapter;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.MyDetails;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SmallGroupDiscussionPage extends Fragment {

    AutoCompleteTextView groupListTextView;
    MaterialButton joinButton, createButton;
    TextInputLayout groupNameTextInput, selectGroupTextInput, multicastGroupTextInput;
    TextInputEditText groupNameEditText, multicastGroupEditText;

    private String name = null;
    private String port = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.small_group_discussion_page, container, false);

        groupListTextView = view.findViewById(R.id.select_group);
        joinButton = view.findViewById(R.id.join_button);
        createButton = view.findViewById(R.id.create_button);
        groupNameTextInput = view.findViewById(R.id.group_name_text_input);
        groupNameEditText = view.findViewById(R.id.group_name_edit_text);
        selectGroupTextInput = view.findViewById(R.id.select_group_text_input);
        multicastGroupTextInput = view.findViewById(R.id.multicast_group_address_text_input);
        multicastGroupEditText = view.findViewById(R.id.multicast_group_address_edit_text);

        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            name = bundle.getString(MyDetails.NAME.toString());
            port = bundle.getString(MyDetails.PORT.toString());

            groupNameEditText.setText(name + Constants.GROUP_SUFFIX);
        }
        multicastGroupEditText.setText(generateMulticastAddress());

        multicastGroupTextInput.setEndIconOnClickListener(multicastGroupRefreshIconClickEvent());
        joinButton.setOnClickListener(joinButtonClickEvent());
        createButton.setOnClickListener(createButtonClickEvent());

        // create a arraylist of the type DiscussionGroupItem
        final ArrayList<DiscussionGroupItem> arrayList = new ArrayList<DiscussionGroupItem>();
        arrayList.add(new DiscussionGroupItem("Gryffindor", null, "12"));
        arrayList.add(new DiscussionGroupItem("Hufflepuff", null, "10"));
        arrayList.add(new DiscussionGroupItem("Ravenclaw", null, "6"));
        arrayList.add(new DiscussionGroupItem("Slytherin", null, "9"));
        Collections.sort(arrayList);

        ListGroupItemAdapter listGroupItemAdapter = new ListGroupItemAdapter(this.requireContext(), arrayList);
        groupListTextView.setAdapter(listGroupItemAdapter);
        return view;
    }

    private String generateMulticastAddress(){
        Random rand = new Random();
        return "239." + rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256);
    }

    private View.OnClickListener multicastGroupRefreshIconClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multicastGroupEditText.setText(generateMulticastAddress());
            }
        };
    }

    private View.OnClickListener joinButtonClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupListTextView.getText() == null || groupListTextView.getText().toString().trim().equals("")) {
                    selectGroupTextInput.setError(getString(R.string.group_mandatory));
                } else {
                    selectGroupTextInput.setError(null);
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }
            }
        };
    }

    private View.OnClickListener createButtonClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupNameEditText.getText() == null || groupNameEditText.getText().toString().trim().equals("")) {
                    groupNameTextInput.setError(getString(R.string.group_name_mandatory));
                } else {
                    groupNameTextInput.setError(null);
                }

                if (groupNameTextInput.getError() == null) {
                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(new MeetingPage(), true);
                }
            }
        };
    }

}
