package com.example.wifimeeting.page;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.example.wifimeeting.components.membercard.MemberGridItemDecoration;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.usecase.smallgroupdiscussion.AudioCallMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.CreateMeetingBroadcast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.EndMeetingMulticast;
import com.example.wifimeeting.usecase.smallgroupdiscussion.GroupMemberService;
import com.example.wifimeeting.usecase.smallgroupdiscussion.MemberRegistryListener;
import com.example.wifimeeting.utils.AddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class GroupDiscussionPage extends Fragment implements BackPressedListener, PropertyChangeListener {

    MaterialButton leaveButton, muteUnmuteButton;
    TextView memberName, groupNameTextView;
    ImageView memberImageView;
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

    private LinkedHashMap<String, Boolean> memberHashMap;
    Handler handler = new Handler();
    private InetAddress broadcastIp;
    private UDN udn = new UDN(UUID.randomUUID());
    private AndroidUpnpService upnpService;
    private LocalService<GroupMemberService> groupMemberLocalService =null;
    private MemberRegistryListener registryListener = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            groupMemberLocalService = getGroupMemberService();

            // Register the member when this activity binds to the service for the first time
            if (groupMemberLocalService == null) {
                try {
                    LocalDevice groupMemberDevice = createDevice();
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Registering GroupMember...");
                    upnpService.getRegistry().addDevice(groupMemberDevice);
                    groupMemberLocalService = getGroupMemberService();
                } catch (Exception ex) {
                    Log.e(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Registering GroupMember failed"+ex);
                    return;
                }
            }

            // Start monitoring the group member changes in state
            groupMemberLocalService.getManager().getImplementation().getPropertyChangeSupport().addPropertyChangeListener(GroupDiscussionPage.this);

            registryListener = new MemberRegistryListener(new MemberRegistryListener.UiUpdateListener() {
                @Override
                public void onDeviceListUpdated(LinkedHashMap<String, Boolean> updatedHasMap) {
                    memberHashMap = updatedHasMap;
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "MemberHashMap :"+ memberHashMap.toString());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewAdapter.updateData(memberHashMap);

                        }
                    });
                }
            });
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search(new STAllHeader());
            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }
            Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Searching for GroupMembers...");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            upnpService = null;
            Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Service disconnected");
        }
    };

    AudioCallMulticast audioCall;
    CreateMeetingBroadcast createMeeting;
    EndMeetingMulticast endMeeting;

    public String getMemberHashMapSize(){
        return String.valueOf(memberHashMap.size());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.group_discussion_page, container, false);
        leaveButton = view.findViewById(R.id.leave_button);
        muteUnmuteButton = view.findViewById(R.id.mute_unmute_button);
        memberName = view.findViewById(R.id.member_name);
        groupNameTextView =  view.findViewById(R.id.group_name);
        memberImageView = view.findViewById(R.id.memberImage);

        memberHashMap = new LinkedHashMap<>();

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
        requireActivity().bindService(new Intent(this.getContext(), AndroidUpnpServiceImpl.class), serviceConnection, Context.BIND_AUTO_CREATE);

        return view;
    }

    private void initializeMeeting(){
        audioCall.startCall();

        //mDNS
        //implement peer discovery

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
        dissembleConnections();

        endMeeting.stopListeningEndMeeting();
    }



    private void dissembleConnections(){

        Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Dissembling connections");

        // Stop monitoring
        LocalService<GroupMemberService> groupMemberService = getGroupMemberService();
        if (groupMemberService != null)
            groupMemberService.getManager().getImplementation().getPropertyChangeSupport().removePropertyChangeListener(this);
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }

        if(serviceConnection!=null)
            requireActivity().unbindService(serviceConnection);

//        upnpService.getRegistry().removeAllRemoteDevices();
//        upnpService.getControlPoint().search();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        dissembleConnections();
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
                } else {
                    Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Cannot remove member. " + name + " does not exist.");
                }

                //stop the 'create meeting broadcast' if the discussion group has maximum number of members
                if(createMeeting !=null && memberHashMap.size() == Constants.SMALL_GROUP_DISCUSSION_MEMBER_MAX_COUNT - 1) {
                    createMeeting.startBroadcasting();
                    createMeeting.broadcastCreate(this, Constants.CREATE_ACTION, groupName, multicastGroupAddress.getHostAddress());
                    return;
                }
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
        if(groupName!=null)
            groupNameTextView.setText(groupName);
        memberImageView.setImageResource(isAdmin? R.drawable.baseline_manage_accounts_24: R.drawable.baseline_person_24);

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
                //toggle sound
                groupMemberLocalService.getManager().getImplementation().setMute(isMute);
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

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("isMute")) {
            Object source = event.getSource();

            if (source instanceof GroupMemberService) {
                GroupMemberService service = (GroupMemberService) source;
                Device device = service.getLocalService().getDevice();
//                String memberName = device.getDetails().getFriendlyName();
//
//                memberHashMap.put(memberName, (Boolean) event.getNewValue());
                Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG," Property change: " );
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.updateData(memberHashMap);

                    }
                });
            }


        }
    }

    protected LocalService<GroupMemberService> getGroupMemberService() {
        if (upnpService == null) return null;
        LocalDevice groupMemberDevice;
        if ((groupMemberDevice = upnpService.getRegistry().getLocalDevice(udn, true)) == null)
            return null;
        return (LocalService<GroupMemberService>) groupMemberDevice.findService(new UDAServiceType("GroupMember", 1));
    }

    protected LocalDevice createDevice() throws LocalServiceBindingException, ValidationException {
        Log.i(Constants.GROUP_DISCUSSION_PAGE_LOG_TAG, "Creating GroupMember Device...");

        DeviceType type = new UDADeviceType("GroupMember", 1);
        DeviceDetails details = new DeviceDetails(name);
        LocalService service = new AnnotationLocalServiceBinder().read(GroupMemberService.class);
        service.setManager(new DefaultServiceManager<>(service, GroupMemberService.class));
        return new LocalDevice(new DeviceIdentity(udn), type, details, service);
    }
}
