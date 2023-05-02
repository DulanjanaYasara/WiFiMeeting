package com.example.wifimeeting.page;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.groupitem.DiscussionGroupItem;
import com.example.wifimeeting.components.groupitem.ListGroupItemAdapter;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.MyDetails;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SmallGroupDiscussionPage extends Fragment {

    AutoCompleteTextView groupListTextView;
    MaterialButton joinButton, createButton;
    TextInputLayout groupNameTextInput, selectGroupTextInput, multicastGroupTextInput;
    TextInputEditText groupNameEditText, multicastGroupEditText;
    Handler handler = new Handler();
    private String name = null;
    private String port = null;
    private ArrayList<DiscussionGroupItem> groupDiscussionDetails = new ArrayList<DiscussionGroupItem>();
    ListGroupItemAdapter listGroupItemAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.small_group_discussion_page, container, false);

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

        initiateGroupListTextView(view);
        multicastGroupTextInput.setEndIconOnClickListener(multicastGroupRefreshIconClickEvent());
        joinButton.setOnClickListener(joinButtonClickEvent());
        createButton.setOnClickListener(createButtonClickEvent());

        //GroupDiscussionDetails information update
        Thread discussionDetailsUpdateThread = new Thread(new GroupDiscussionDetailsUpdate());
        discussionDetailsUpdateThread.start();

        return view;
    }

    private class GroupDiscussionDetailsUpdate implements Runnable{
        @Override
        public void run() {
            try {
                while(true){

                    // check if any group discussions have timed out, if so remove them
                    long now = System.currentTimeMillis();

                    List<Integer> timedOutPeerIndices = new ArrayList<>();
                    for (int i =0; i < groupDiscussionDetails.size(); i++) {
                        if (now - groupDiscussionDetails.get(i).getHeartBeatReceivedTime() > Constants.GROUP_DISCUSSION_DISCOVERY_TIMEOUT_MILLISECONDS) {
                            timedOutPeerIndices.add(i);
                        }
                    }

                    if (timedOutPeerIndices.size()>0){
                        for (Integer peer: timedOutPeerIndices) {
                            groupDiscussionDetails.remove(peer);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listGroupItemAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    Thread.sleep(Constants.GROUP_DISCUSSION_HEARTBEAT_INTERVAL);

                }
            } catch (InterruptedException e) {
                Log.e(Constants.SMALL_GROUP_DISCUSSION_PAGE_LOG_TAG, "Update Group Discussion Details Failed!!! ");
            }
        }
    }

    private void initiateGroupListTextView(View view){
        groupListTextView = view.findViewById(R.id.select_group);

        listGroupItemAdapter = new ListGroupItemAdapter(this.requireContext(), groupDiscussionDetails);
        groupListTextView.setAdapter(listGroupItemAdapter);
    }

    public synchronized void updateGroupDiscussionDetails(String groupName, String multicastIpAddress, String noOfMembers){

        try {
            if(groupDiscussionDetails.contains(groupName)) {
                for (int i=0; i< groupDiscussionDetails.size(); i++){
                    if(groupDiscussionDetails.get(i).getGroupName().equals(groupName)){
                        groupDiscussionDetails.set(i, new DiscussionGroupItem(groupName, InetAddress.getByName(multicastIpAddress), noOfMembers, System.currentTimeMillis()));

                        if(Integer.parseInt(groupDiscussionDetails.get(i).getNoOfMembers())!= Integer.parseInt(noOfMembers)){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listGroupItemAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        break;
                    }
                }
            } else {
                groupDiscussionDetails.add(new DiscussionGroupItem(groupName, InetAddress.getByName(multicastIpAddress), noOfMembers, System.currentTimeMillis()));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listGroupItemAdapter.notifyDataSetChanged();
                    }
                });
            }

        } catch (Exception ex){
            Log.e(Constants.SMALL_GROUP_DISCUSSION_PAGE_LOG_TAG, "Update Group Discussion Details Failed!!! ");
        }
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
