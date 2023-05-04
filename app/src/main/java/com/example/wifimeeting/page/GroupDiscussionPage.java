package com.example.wifimeeting.page;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.membercard.MemberCardRecyclerViewAdapter;
import com.example.wifimeeting.components.membercard.MemberGridItemDecoration;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.usecase.smallgroupdiscussion.AudioCallMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.CreateMeetingBroadcast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.EndMeetingMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.JoinMeetingMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.LeaveMeetingMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.MuteUnmuteMeetingMulticast;
import com.example.wifimeeting.utils.AddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;

public class GroupDiscussionPage extends Fragment implements BackPressedListener{

    MaterialButton leaveButton, muteUnmuteButton;
    TextView memberName;
    MaterialAlertDialogBuilder leaveAlertDialog;
    MemberCardRecyclerViewAdapter viewAdapter;
    RecyclerView recyclerView;

    private long muteUnmuteButtonLastClickTime = 0;
    public static BackPressedListener backPressedListener;

    /**
     * My Details
     */
    private String name = null;
    private Boolean isMute = true;
    private String groupName= null;
    private Boolean isAdmin = false;
    private int port;
    private InetAddress multicastGroupAddress;

    private LinkedHashMap<String, Boolean> memberHashMap = new LinkedHashMap<>();
    Handler handler = new Handler();
    private InetAddress broadcastIp;

    AudioCallMulticast audioCall;
    JoinMeetingMulticast joinMeeting;
    LeaveMeetingMulticast leaveMeeting;
    MuteUnmuteMeetingMulticast muteUnmuteMeeting;
    CreateMeetingBroadcast createMeeting;
    EndMeetingMulticast endMeeting;

    public String getMemberHashMapSize(){
        return String.valueOf(memberHashMap.size());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.meeting_page, container, false);
        leaveButton = view.findViewById(R.id.leave_button);
        muteUnmuteButton = view.findViewById(R.id.mute_unmute_button);
        memberName = view.findViewById(R.id.member_name);

        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            name = bundle.getString(GroupDiscussionMember.NAME.toString());
            isMute = bundle.getBoolean(GroupDiscussionMember.IS_MUTE.toString());
            groupName = bundle.getString(GroupDiscussionMember.GROUP_NAME.toString());
            isAdmin = bundle.getBoolean(GroupDiscussionMember.IS_ADMIN.toString());
            port = bundle.getInt(GroupDiscussionMember.PORT.toString());
            try {
                multicastGroupAddress = InetAddress.getByName(bundle.getString(GroupDiscussionMember.MULTICAST_GROUP_ADDRESS.toString()));
            } catch (UnknownHostException e) {
                Log.e(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Error in retrieving Multicast Group Address!");
            }

            readyUiView();
        }

        //leave alert dialog initialize
        leaveAlertDialog =  new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle(R.string.confirm)
                .setMessage(R.string.leave_discussion_group_confirmation)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        leaveMeeting(false);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AddressGenerator addressGenerator = new AddressGenerator(view);
        broadcastIp = addressGenerator.getBroadcastIp();

        // Set up the RecyclerView
        initiateRecyclerView(view);
        audioCall = new AudioCallMulticast(addressGenerator.getIpAddress(), multicastGroupAddress, isMute, port);
        initializeMeeting();

        leaveButton.setOnClickListener(leaveButtonClickEvent());
        muteUnmuteButton.setOnClickListener(muteUnmuteButtonClickEvent());

        return view;
    }

    private void initializeMeeting(){
        audioCall.startCall();
        joinMeeting = new JoinMeetingMulticast(this,  name, isMute, multicastGroupAddress);
        leaveMeeting = new LeaveMeetingMulticast(this,  multicastGroupAddress);
        muteUnmuteMeeting = new MuteUnmuteMeetingMulticast(this, multicastGroupAddress);

        endMeeting = new EndMeetingMulticast( multicastGroupAddress);
        if(isAdmin){
            //broadcasting 'create meeting'
            createMeeting = new CreateMeetingBroadcast( broadcastIp);
            createMeeting.broadcastCreate(this, Constants.CREATE_ACTION, groupName, multicastGroupAddress.getHostAddress());
        } else {
            //listening 'end meeting'
            endMeeting.listenEndMeeting(this);
        }
    }

    public void leaveMeeting(boolean isAdminTriggered){

        requireFragmentManager().popBackStack();
        if(isAdminTriggered){
            Snackbar.make(this.requireView(), R.string.admin_end_meeting, Snackbar.LENGTH_SHORT).show();
        }

        //if admin leaves then multicast all members have to leave
        if(isAdmin){
            endMeeting.multicastEndMeeting(Constants.END_ACTION);
            createMeeting.stopBroadcasting();
        }

        audioCall.endCall();
        leaveMeeting.multicastLeaveAbsent(Constants.LEAVE_ACTION,name);
        joinMeeting.stopListeningJoinMeeting();
        leaveMeeting.stopListeningLeaveMeeting();
        muteUnmuteMeeting.stopListeningMuteUnmuteMeeting();
        endMeeting.stopListeningEndMeeting();
    }

    public synchronized void updateMemberHashMap(String action, String nameValue, Boolean isMuteValue){

        switch(action){

            case Constants.JOIN_ACTION:
            case Constants.PRESENT_ACTION:
            case Constants.MUTE_ACTION:{

                if (memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Updating member: " + nameValue);
                } else {
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Adding member: " + nameValue);
                }
                memberHashMap.put(nameValue, isMuteValue);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.updateData(memberHashMap);

                    }
                });
                Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());

                //stop the 'create meeting broadcast' if the discussion group has maximum number of members
                if(createMeeting !=null && memberHashMap.size() >= Constants.SMALL_GROUP_DISCUSSION_MEMBER_MAX_COUNT) {
                    createMeeting.stopBroadcasting();
                    return;
                }

                break;
            }

            case Constants.LEAVE_ACTION:
            case Constants.ABSENT_ACTION:{

                if(memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Removing member: " + nameValue);
                    memberHashMap.remove(nameValue);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewAdapter.updateData(memberHashMap);

                        }
                    });
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());
                    return;
                }
                Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Cannot remove member. " + name + " does not exist.");
                break;
            }

            default:
                Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Action not found");
        }

    }

    private void initiateRecyclerView(View view){
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
        viewAdapter = new MemberCardRecyclerViewAdapter(memberHashMap);
        recyclerView.setAdapter(viewAdapter);
        int largePadding = getResources().getDimensionPixelSize(R.dimen.member_grid_spacing_small);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.member_grid_spacing_small);
        recyclerView.addItemDecoration(new MemberGridItemDecoration(largePadding, smallPadding));
    }

    private void readyUiView (){
        if(name!= null)
            memberName.setText(name);
        if(isMute){
                muteUnmuteButton.setText(R.string.unmute);
                muteUnmuteButton.setIcon(getResources().getDrawable(R.drawable.baseline_mic_24));
        } else {
            muteUnmuteButton.setText(R.string.mute);
            muteUnmuteButton.setIcon(getResources().getDrawable(R.drawable.baseline_mic_off_24));
        }
    }

    private View.OnClickListener muteUnmuteButtonClickEvent() {
        return new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - muteUnmuteButtonLastClickTime < Constants.MUTE_UNMUTE_BUTTON_THRESHOLD_MILLISECONDS){
                    return;
                }
                muteUnmuteButtonLastClickTime = SystemClock.elapsedRealtime();

                isMute = !isMute;

                if(isMute){
                    muteUnmuteButton.setText(R.string.unmute);
                    muteUnmuteButton.setIcon(getResources().getDrawable(R.drawable.baseline_mic_24));
                    audioCall.muteMeFromMeeting();

                } else {
                    muteUnmuteButton.setText(R.string.mute);
                    muteUnmuteButton.setIcon(getResources().getDrawable(R.drawable.baseline_mic_off_24));
                    audioCall.unmuteMeFromMeeting();

                }
                muteUnmuteMeeting.multicastMuteUnmute(Constants.MUTE_ACTION,name, isMute);
            }
        };
    }

    private View.OnClickListener leaveButtonClickEvent (){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               leaveAlertDialog.show();
            }
        };
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
        leaveAlertDialog.show();
    }
}
