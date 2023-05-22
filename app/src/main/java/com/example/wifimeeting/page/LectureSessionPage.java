package com.example.wifimeeting.page;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.membercard.MemberCardRecyclerViewAdapter;
import com.example.wifimeeting.components.membercard.MemberCharacteristics;
import com.example.wifimeeting.components.membercard.MemberGridItemDecoration;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.transmission.AudioCallMulticast;
import com.example.wifimeeting.transmission.CreateMeetingBroadcast;
import com.example.wifimeeting.transmission.EndMeetingMulticast;
import com.example.wifimeeting.transmission.JoinMeetingMulticast;
import com.example.wifimeeting.transmission.LeaveMeetingMulticast;
import com.example.wifimeeting.transmission.MuteUnmuteMeetingMulticast;
import com.example.wifimeeting.transmission.UpdateMembersMulticast;
import com.example.wifimeeting.utils.BroadcastAddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.example.wifimeeting.utils.LectureSessionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class LectureSessionPage extends Fragment implements BackPressedListener{

    MaterialButton leaveButton, muteUnmuteButton;
    TextView memberName, moduleNameTextView;
    ImageView moduleImageView;
    MaterialAlertDialogBuilder leaveAlertDialog;
    MemberCardRecyclerViewAdapter viewAdapter;
    RecyclerView recyclerView;

    private long muteUnmuteButtonLastClickTime = 0;
    public static BackPressedListener backPressedListener;

    /**
     * My Details
     */
    private String name = null;
    private String role = null;
    private String moduleName= null;
    private InetAddress multicastGroupAddress;
    private Boolean isMute;

    private LinkedHashMap<String, MemberCharacteristics> memberHashMap = new LinkedHashMap<>();
    Handler handler = new Handler();
    private InetAddress broadcastIp;

    AudioCallMulticast audioCall;
    JoinMeetingMulticast joinMeeting;
    LeaveMeetingMulticast leaveMeeting;
    CreateMeetingBroadcast createMeeting;
    MuteUnmuteMeetingMulticast muteUnmuteMeeting;
    EndMeetingMulticast endMeeting;
    UpdateMembersMulticast updateMembersMulticast;
    JoinMemberRefresh joinMemberRefresh = null;
    BroadcastAddressGenerator addressGenerator;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.meeting_page, container, false);
        leaveButton = view.findViewById(R.id.leave_button);
        muteUnmuteButton = view.findViewById(R.id.mute_unmute_button);
        memberName = view.findViewById(R.id.member_name);
        moduleNameTextView =  view.findViewById(R.id.group_name);
        moduleImageView = view.findViewById(R.id.groupImage);

        if(this.getArguments() != null){
            Bundle bundle = this.getArguments();
            name = bundle.getString(LectureSessionMember.NAME.toString());
            role = bundle.getString(LectureSessionMember.ROLE.toString());
            moduleName = bundle.getString(LectureSessionMember.MODULE_CODE.toString());
            try {
                multicastGroupAddress = InetAddress.getByName(bundle.getString(GroupDiscussionMember.MULTICAST_GROUP_ADDRESS.toString()));
            } catch (UnknownHostException e) {
                Log.e(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Error in retrieving Multicast Group Address!");
            }

            isMute = Constants.STUDENT_ROLE.toString().equals(role);
            readyUiView();
        }

        //leave alert dialog initialize
        leaveAlertDialog =  new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle(R.string.confirm)
                .setMessage(R.string.leave_lecture_session_confirmation)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        leaveMeeting(false);

                        if(joinMemberRefresh!=null)
                            joinMemberRefresh.stop();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        addressGenerator = new BroadcastAddressGenerator(view);
        broadcastIp = addressGenerator.getBroadcastIp();

        // Set up the RecyclerView
        initiateRecyclerView(view);
        audioCall = new AudioCallMulticast(addressGenerator.getIpAddress(), multicastGroupAddress, isMute);
        initializeMeeting();

        leaveButton.setOnClickListener(leaveButtonClickEvent());
        muteUnmuteButton.setOnClickListener(muteUnmuteButtonClickEvent());

        return view;
    }

    private void initializeMeeting(){
        boolean isAdmin = role.equals(Constants.LECTURER_ROLE);

        audioCall.startCall();
        joinMeeting = new JoinMeetingMulticast(this,  name, isMute, multicastGroupAddress, isAdmin);
        explicitlyAddingFirstMember();
        leaveMeeting = new LeaveMeetingMulticast(this, multicastGroupAddress);
        muteUnmuteMeeting = new MuteUnmuteMeetingMulticast(this, multicastGroupAddress);

        endMeeting = new EndMeetingMulticast( multicastGroupAddress);
        if(isAdmin){
            //broadcasting 'create meeting'
            createMeeting = new CreateMeetingBroadcast(broadcastIp);
            createMeeting.broadcastCreate(this, Constants.CREATE_ACTION, moduleName, multicastGroupAddress.getHostAddress());
        } else {
            //listening 'end meeting'
            endMeeting.listenEndMeeting(this);
        }
    }

    private void explicitlyAddingFirstMember(){
        memberHashMap.put(name, new MemberCharacteristics(isMute));
        updateMembersMulticast = new UpdateMembersMulticast(this, multicastGroupAddress, addressGenerator.getIpAddress());
    }

    public synchronized void addMissedMembers(LinkedHashMap<String, Boolean> memberList) {
        for(Map.Entry<String, Boolean> entry: memberList.entrySet()){
            if(!memberHashMap.containsKey(entry.getKey())) {
                memberHashMap.put(entry.getKey(), new MemberCharacteristics(entry.getValue()));

                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Adding the missed member :"+ entry.getKey());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.updateData(memberHashMap);

                    }
                });
            }
        }
    }

    private class JoinMemberRefresh implements Runnable{
        private volatile boolean pause = false;
        private volatile boolean exit = false;
        @Override
        public void run() {
            try {
                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Starting JOIN meeting refresh thread...");
                Long mostRecentLastMemberJoinTime = 0L;

                while(!exit){

                    while(!pause){
                        Long now = System.currentTimeMillis();

                        // Get the collection of values and iterate over them
                        for (MemberCharacteristics characteristics: memberHashMap.values()) {
                            if(characteristics.getLastMemberJoinTime() > mostRecentLastMemberJoinTime)
                                mostRecentLastMemberJoinTime = characteristics.getLastMemberJoinTime();
                        }

                        if(now - mostRecentLastMemberJoinTime > Constants.JOIN_MEETING_REFRESH_TIMEOUT_MILLISECONDS) {

                            if(role.equals(Constants.LECTURER_ROLE)){
                                LinkedHashMap<String, Boolean> members = new LinkedHashMap<>();
                                for (Map.Entry<String, MemberCharacteristics> entry : memberHashMap.entrySet()) {
                                    members.put(entry.getKey(), entry.getValue().isMute());
                                }

                                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Lecturer multicasting member information");
                                updateMembersMulticast.multicastUpdateMember(Constants.UPDATE_ACTION, members);

                            } else {

                                //Random integer generation
                                int randomNum =  (new Random()).nextInt(memberHashMap.size());
                                int limit = Math.round((float) memberHashMap.size() * Constants.JOIN_MEMBER_REFRESH_MULTICAST_PERCENTAGE/100);


                                if (randomNum < (limit - 1)) {
                                    LinkedHashMap<String, Boolean> members = new LinkedHashMap<>();
                                    for (Map.Entry<String, MemberCharacteristics> entry : memberHashMap.entrySet()) {
                                        members.put(entry.getKey(), entry.getValue().isMute());
                                    }
                                    Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Selected for multicasting member information");
                                    updateMembersMulticast.multicastUpdateMember(Constants.UPDATE_ACTION, members);
                                }

                            }
                            pause = true;
                        }

                        Thread.sleep(Constants.CLASSROOM_LECTURE_JOIN_UPDATE_INTERVAL);
                    }

                }

            } catch (Exception ex) {
                Log.e(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Join Members Refresh Failed!!! ");
            }
        }

        public void stop() {
            exit = true;
        }

        public boolean isPause() {
            return pause;
        }

        public void setPause(boolean pause) {
            this.pause = pause;
        }
    }

    public synchronized void leaveMeeting(boolean isAdminTriggered){

        requireFragmentManager().popBackStack();
        if(isAdminTriggered){
            Snackbar.make(this.requireView(), R.string.lecture_session_ended, Snackbar.LENGTH_SHORT).show();
        }

        //if admin leaves then multicast all members have to leave
        if(role.equals(Constants.LECTURER_ROLE)){
            endMeeting.multicastEndMeeting(Constants.END_ACTION);
            createMeeting.stopBroadcasting();
        }

        audioCall.endCall();
        leaveMeeting.multicastLeaveAbsent(Constants.LEAVE_ACTION,name);
        joinMeeting.stopListeningJoinMeeting();
        leaveMeeting.stopListeningLeaveMeeting();
        muteUnmuteMeeting.stopListeningMuteUnmuteMeeting();
        endMeeting.stopListeningEndMeeting();
        updateMembersMulticast.stopListeningUpdateMembers();
    }

    public synchronized boolean getCurrentIsMute(){
        return isMute;
    }

    public synchronized void updateMemberHashMap(String action, String nameValue, Boolean isMuteValue){

        switch(action){

            case Constants.JOIN_ACTION:
            case Constants.PRESENT_ACTION:
            case Constants.MUTE_ACTION:{

                if (memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Updating member: " + nameValue);
                    MemberCharacteristics value = memberHashMap.get(nameValue);
                    if(value!= null) value.setIsMute(isMuteValue);

                } else {
                    Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Adding member: " + nameValue);
                    memberHashMap.put(nameValue, new MemberCharacteristics(isMuteValue));

                    if(joinMemberRefresh == null){
                        //Members Details Information JOIN message packet loss rectifying
                        joinMemberRefresh = new JoinMemberRefresh();
                        Thread joinMemberRefreshThread = new Thread(joinMemberRefresh);
                        joinMemberRefreshThread.start();
                    } else {
                        if(joinMemberRefresh.isPause())
                            joinMemberRefresh.setPause(false);
                    }

                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.updateData(memberHashMap);

                    }
                });
                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());
                break;
            }

            case Constants.LEAVE_ACTION:
            case Constants.ABSENT_ACTION:{

                if(memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Removing member: " + nameValue);
                    memberHashMap.remove(nameValue);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewAdapter.updateData(memberHashMap);

                        }
                    });
                    Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());
                    return;
                }
                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Cannot remove member. " + name + " does not exist.");
                break;
            }

            default:
                Log.i(Constants.LECTURE_SESSION_PAGE_LOG_TAG, "Action not found");
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
        if(moduleName!= null)
            moduleNameTextView.setText(moduleName);
        moduleImageView.setImageResource(R.drawable.baseline_local_library_24);

        if(isMute){
            muteUnmuteButton.setText( R.string.unmute);
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
