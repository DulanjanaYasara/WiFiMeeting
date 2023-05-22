package com.example.wifimeeting.page;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.groupitem.DiscussionGroupItem;
import com.example.wifimeeting.components.groupitem.ListGroupItemAdapter;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.transmission.CreateMeetingBroadcast;
import com.example.wifimeeting.utils.BroadcastAddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.example.wifimeeting.utils.MulticastAddressGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GroupDiscussionLobbyPage extends Fragment implements BackPressedListener {

    AutoCompleteTextView groupListTextView;
    MaterialButton joinButton, createButton;
    TextInputLayout groupNameTextInput, selectGroupTextInput, multicastGroupTextInput;
    TextInputEditText groupNameEditText, multicastGroupEditText;
    Handler handler = new Handler();
    private String name = null;
    private InetAddress broadcastIp;
    private int selectedDiscussionGroupIndex;

    public static BackPressedListener backPressedListener;

    private ArrayList<DiscussionGroupItem> groupDiscussionDetails = new ArrayList<DiscussionGroupItem>();
    ListGroupItemAdapter listGroupItemAdapter;
    CreateMeetingBroadcast createMeetingBroadcast;
    GroupDiscussionDetailsUpdate groupDiscussionDetailsUpdate;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.group_discussion_lobby_page, container, false);

        joinButton = view.findViewById(R.id.join_button);
        createButton = view.findViewById(R.id.create_button);
        groupNameTextInput = view.findViewById(R.id.group_name_text_input);
        groupNameEditText = view.findViewById(R.id.group_name_edit_text);
        selectGroupTextInput = view.findViewById(R.id.select_group_text_input);
        multicastGroupTextInput = view.findViewById(R.id.multicast_group_address_text_input);
        multicastGroupEditText = view.findViewById(R.id.multicast_group_address_edit_text);

        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            name = bundle.getString(GroupDiscussionMember.NAME.toString());
            groupNameEditText.setText(name + Constants.GROUP_SUFFIX);
        }
        multicastGroupEditText.setText(MulticastAddressGenerator.generateMulticastAddress());

        initiateGroupListTextView(view);
        groupListTextView.setOnItemClickListener(arraylistClickItemEvent());
        multicastGroupTextInput.setEndIconOnClickListener(multicastGroupRefreshIconClickEvent());
        joinButton.setOnClickListener(joinButtonClickEvent());
        createButton.setOnClickListener(createButtonClickEvent());

        BroadcastAddressGenerator addressGenerator = new BroadcastAddressGenerator(view);
        broadcastIp = addressGenerator.getBroadcastIp();

        initializeGroupDiscussion();

        return view;
    }

    private AdapterView.OnItemClickListener arraylistClickItemEvent(){
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedDiscussionGroupIndex = position;
            }
        };
    }

    private void initializeGroupDiscussion(){
        //GroupDiscussionDetails information update
        groupDiscussionDetailsUpdate = new GroupDiscussionDetailsUpdate();
        Thread discussionDetailsUpdateThread = new Thread(groupDiscussionDetailsUpdate);
        discussionDetailsUpdateThread.start();

        //start the listening of the broadcast
        createMeetingBroadcast = new CreateMeetingBroadcast( broadcastIp);
        createMeetingBroadcast.listenCreateMeeting(this);

    }

    // Overriding onPause() method
    @Override
    public void onPause() {
        // passing null value to backPressedListener
        backPressedListener=null;
        super.onPause();
    }


    // Overriding onResume() method
    @Override
    public void onResume() {
        super.onResume();
        // passing context of fragment to backPressedListener
        backPressedListener=this;
    }

    @Override
    public void onBackPressed() {
        if(createMeetingBroadcast !=null)
            createMeetingBroadcast.stopListeningCreateMeeting();
        if(groupDiscussionDetailsUpdate!=null)
            groupDiscussionDetailsUpdate.stop();
        requireFragmentManager().popBackStack();
    }

    private class GroupDiscussionDetailsUpdate implements Runnable{
        private volatile boolean exit = false;
        @Override
        public void run() {
            try {
                while(!exit){

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
                            Log.i(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Group Discussion Details Removing group: "+groupDiscussionDetails.get(peer).getGroupName());
                            groupDiscussionDetails.remove(peer.intValue());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listGroupItemAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                    Thread.sleep(Constants.GROUP_DISCUSSION_HEARTBEAT_INTERVAL);

                }
            } catch (InterruptedException e) {
                Log.e(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Update Group Discussion Details Failed!!! ");
            }
        }

        public void stop() {
            exit = true;
        }
    }

    private void initiateGroupListTextView(View view){
        groupListTextView = view.findViewById(R.id.select_group);

        listGroupItemAdapter = new ListGroupItemAdapter(this.requireContext(), groupDiscussionDetails);
        groupListTextView.setAdapter(listGroupItemAdapter);
    }

    public synchronized void updateGroupDiscussionDetails(String groupName, String multicastIpAddress, String noOfMembers){

        try {
            if(groupDiscussionDetails.contains(new DiscussionGroupItem(groupName))) {
                for (int i=0; i< groupDiscussionDetails.size(); i++){
                    if(groupDiscussionDetails.get(i).getGroupName().equals(groupName)){
                        groupDiscussionDetails.set(i, new DiscussionGroupItem(groupName, InetAddress.getByName(multicastIpAddress), noOfMembers, System.currentTimeMillis()));

                        Log.i(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Group Discussion Details Updated: Heartbeat");

                        if(Integer.parseInt(groupDiscussionDetails.get(i).getNoOfMembers())!= Integer.parseInt(noOfMembers)){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listGroupItemAdapter.notifyDataSetChanged();
                                }
                            });
                            Log.i(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Group Discussion Details Updated. #Groups: "+groupDiscussionDetails.size());

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
                Log.i(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Group Discussion Details Adding Group: "+ groupName);
            }

        } catch (Exception ex){
            Log.e(Constants.GROUP_DISCUSSION_LOBBY_PAGE_LOG_TAG, "Update Group Discussion Details Failed!!! ");
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

    private synchronized View.OnClickListener joinButtonClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupListTextView.getText() == null || groupListTextView.getText().toString().trim().equals("")) {
                    selectGroupTextInput.setError(getString(R.string.group_mandatory));
                } else {
                    selectGroupTextInput.setError(null);
                }

                if (selectGroupTextInput.getError() == null){
                    if (selectedDiscussionGroupIndex >= groupDiscussionDetails.size()){
                        Snackbar.make(view, R.string.group_does_not_exist, Snackbar.LENGTH_SHORT).show();
                    } else {

                        //stop listening
                        if (createMeetingBroadcast != null)
                            createMeetingBroadcast.stopListeningCreateMeeting();
                        if(groupDiscussionDetailsUpdate!=null)
                            groupDiscussionDetailsUpdate.stop();

                        Bundle bundle = new Bundle();
                        bundle.putString(GroupDiscussionMember.NAME.toString(), name);
                        bundle.putString(GroupDiscussionMember.MULTICAST_GROUP_ADDRESS.toString(), groupDiscussionDetails.get(selectedDiscussionGroupIndex).getMulticastGroupAddress().getHostAddress());
                        bundle.putString(GroupDiscussionMember.GROUP_NAME.toString(), groupDiscussionDetails.get(selectedDiscussionGroupIndex).getGroupName());
                        bundle.putString(GroupDiscussionMember.ROLE.toString(), Constants.NON_ADMIN_ROLE.toString());


                        GroupDiscussionPage groupDiscussionPage = new GroupDiscussionPage();
                        groupDiscussionPage.setArguments(bundle);

                        ((NavigationHost) getActivity()).navigateTo(groupDiscussionPage, true);
                    }
                }
            }
        };
    }

    private synchronized View.OnClickListener createButtonClickEvent() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupNameEditText.getText() == null || groupNameEditText.getText().toString().trim().equals("")) {
                    groupNameTextInput.setError(getString(R.string.group_name_mandatory));
                } else {
                    groupNameTextInput.setError(null);
                }

                if (multicastGroupEditText.getText() == null || multicastGroupEditText.getText().toString().trim().equals("")) {
                    multicastGroupTextInput.setError(getString(R.string.multicast_group_address_mandatory));
                } else {
                    multicastGroupTextInput.setError(null);
                }

                if (groupNameTextInput.getError() == null && multicastGroupTextInput.getError() == null) {

                    String multicastGroupAddress = multicastGroupEditText.getText().toString().trim();
                    if(!MulticastAddressGenerator.validateMulticastGroupAddress(multicastGroupAddress)){
                        Snackbar.make(view, R.string.multicast_group_address_validation_error, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    //stop listening
                    if(createMeetingBroadcast !=null)
                        createMeetingBroadcast.stopListeningCreateMeeting();
                    if(groupDiscussionDetailsUpdate!=null)
                        groupDiscussionDetailsUpdate.stop();

                    Bundle bundle = new Bundle();
                    bundle.putString(GroupDiscussionMember.NAME.toString(), name);
                    bundle.putString(GroupDiscussionMember.MULTICAST_GROUP_ADDRESS.toString(), multicastGroupAddress);
                    bundle.putString(GroupDiscussionMember.GROUP_NAME.toString(), groupNameEditText.getText().toString().trim());
                    bundle.putString(GroupDiscussionMember.ROLE.toString(), Constants.GROUP_ADMIN_ROLE.toString());


                    GroupDiscussionPage groupDiscussionPage = new GroupDiscussionPage();
                    groupDiscussionPage.setArguments(bundle);

                    // Navigate to the next Fragment
                    ((NavigationHost) getActivity()).navigateTo(groupDiscussionPage, true);
                }
            }
        };
    }

}
