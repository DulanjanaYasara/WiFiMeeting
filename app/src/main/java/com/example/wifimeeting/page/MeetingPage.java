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
import com.example.wifimeeting.card.MemberCardRecyclerViewAdapter;
import com.example.wifimeeting.card.MemberGridItemDecoration;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.usecase.bigclassroomlecturesession.JoinMeeting;
import com.example.wifimeeting.usecase.bigclassroomlecturesession.LeaveMeeting;
import com.example.wifimeeting.usecase.bigclassroomlecturesession.MuteUnmuteMeeting;
import com.example.wifimeeting.utils.AddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.MyDetails;
import com.example.wifimeeting.utils.Role;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.InetAddress;
import java.util.LinkedHashMap;

public class MeetingPage extends Fragment implements BackPressedListener{

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
    private String role;

    private LinkedHashMap<String, Boolean> memberHashMap = new LinkedHashMap<>();
    Handler handler = new Handler();
    private InetAddress broadcastIp;

    JoinMeeting joinMeeting;
    LeaveMeeting leaveMeeting;
    MuteUnmuteMeeting muteUnmuteMeeting;

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
            name = bundle.getString(MyDetails.NAME.toString());
            isMute = bundle.getBoolean(MyDetails.IS_MUTE.toString());
            role = bundle.getString(MyDetails.ROLE.toString());
            readyUiView();
        }

        //leave alert dialog initialize
        leaveAlertDialog =  new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle(R.string.confirm)
                .setMessage(R.string.leave_meeting_confirmation)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requireFragmentManager().popBackStack();
                        leaveMeeting();
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
        initializeMeeting();

        leaveButton.setOnClickListener(leaveButtonClickEvent());
        muteUnmuteButton.setOnClickListener(muteUnmuteButtonClickEvent());

        return view;
    }

    private void initializeMeeting(){
        joinMeeting = new JoinMeeting(this,  name, isMute, broadcastIp);
        leaveMeeting = new LeaveMeeting(this,  broadcastIp);
        muteUnmuteMeeting = new MuteUnmuteMeeting(this, broadcastIp);

    }

    private void leaveMeeting(){
        leaveMeeting.broadcastLeaveAbsent(Constants.LEAVE_ACTION,name);
        joinMeeting.stopListeningJoinMeeting();
        leaveMeeting.stopListeningLeaveMeeting();
        muteUnmuteMeeting.stopListeningMuteUnmuteMeeting();

    }

    public synchronized void updateMemberHashMap(String action, String nameValue, Boolean isMuteValue){

        switch(action){

            case Constants.JOIN_ACTION:
            case Constants.PRESENT_ACTION:
            case Constants.MUTE_ACTION:{

                if (memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.MEETING_PAGE_LOG_TAG, "Updating member: " + nameValue);
                } else {
                    Log.i(Constants.MEETING_PAGE_LOG_TAG, "Adding member: " + nameValue);
                }
                memberHashMap.put(nameValue, isMuteValue);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.updateData(memberHashMap);

                    }
                });
                Log.i(Constants.MEETING_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());
                break;
            }

            case Constants.LEAVE_ACTION:
            case Constants.ABSENT_ACTION:{

                if(memberHashMap.containsKey(nameValue)) {
                    Log.i(Constants.MEETING_PAGE_LOG_TAG, "Removing member: " + nameValue);
                    memberHashMap.remove(nameValue);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewAdapter.updateData(memberHashMap);

                        }
                    });
                    Log.i(Constants.MEETING_PAGE_LOG_TAG, "#Members: " + memberHashMap.size());
                    return;
                }
                Log.i(Constants.MEETING_PAGE_LOG_TAG, "Cannot remove member. " + name + " does not exist.");
                break;
            }

            default:
                Log.i(Constants.MEETING_PAGE_LOG_TAG, "Action not found");
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
            if(role != null){
                muteUnmuteButton.setText(role.equals(Role.LECTURER.toString())? R.string.mute: R.string.unmute);
                muteUnmuteButton.setIcon(
                        role.equals(Role.LECTURER.toString())?
                                getResources().getDrawable(R.drawable.baseline_mic_off_24):
                                getResources().getDrawable(R.drawable.baseline_mic_24));
            }
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

                boolean isMuteValue = muteUnmuteButton.getText().toString().equals(getString(R.string.mute));

                muteUnmuteButton.setText(isMuteValue? R.string.unmute: R.string.mute);
                muteUnmuteButton.setIcon(
                        isMuteValue?
                                getResources().getDrawable(R.drawable.baseline_mic_off_24):
                                getResources().getDrawable(R.drawable.baseline_mic_24));
                muteUnmuteMeeting.broadcastMuteUnmute(Constants.MUTE_ACTION,name, isMuteValue);

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
