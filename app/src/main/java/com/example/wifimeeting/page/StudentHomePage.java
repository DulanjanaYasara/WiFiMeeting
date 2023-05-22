package com.example.wifimeeting.page;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.wifimeeting.R;
import com.example.wifimeeting.components.moduleitem.ListModuleItemAdapter;
import com.example.wifimeeting.components.moduleitem.ModuleDetailItem;
import com.example.wifimeeting.navigation.BackPressedListener;
import com.example.wifimeeting.navigation.NavigationHost;
import com.example.wifimeeting.transmission.CreateMeetingBroadcast;
import com.example.wifimeeting.utils.BroadcastAddressGenerator;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.GroupDiscussionMember;
import com.example.wifimeeting.utils.LectureSessionMember;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StudentHomePage extends Fragment implements BackPressedListener {

    MaterialButton joinLectureButton;
    MaterialButton smallGroupDiscussionButton;
    TextInputLayout selectModuleTextInput;
    TextInputLayout studentNameTextInput;
    TextInputEditText studentNameEditText;
    AutoCompleteTextView moduleListTextView;
    ListModuleItemAdapter listModuleItemAdapter;
    private int selectedModuleIndex;
    private ArrayList<ModuleDetailItem> moduleDetails = new ArrayList<ModuleDetailItem>();
    Handler handler = new Handler();
    private InetAddress broadcastIp;
    CreateMeetingBroadcast createMeetingBroadcast;
    ModuleDetailsUpdate moduleDetailsUpdate;
    public static BackPressedListener backPressedListener;

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

                        //stop listening
                        if (createMeetingBroadcast != null)
                            createMeetingBroadcast.stopListeningCreateMeeting();
                        if(moduleDetailsUpdate!=null)
                            moduleDetailsUpdate.stop();

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

                        if (selectedModuleIndex >= moduleDetails.size()){
                            Snackbar.make(view, R.string.module_does_not_exist, Snackbar.LENGTH_SHORT).show();
                        } else {
                            //stop listening
                            if (createMeetingBroadcast != null)
                                createMeetingBroadcast.stopListeningCreateMeeting();
                            if(moduleDetailsUpdate!=null)
                                moduleDetailsUpdate.stop();

                            Bundle bundle = new Bundle();
                            bundle.putString(LectureSessionMember.NAME.toString(), Objects.requireNonNull(studentNameEditText.getText()).toString().trim());
                            bundle.putString(LectureSessionMember.ROLE.toString(), Constants.STUDENT_ROLE.toString());
                            bundle.putString(LectureSessionMember.MULTICAST_GROUP_ADDRESS.toString(), moduleDetails.get(selectedModuleIndex).getMulticastGroupAddress().getHostAddress());
                            bundle.putString(LectureSessionMember.MODULE_CODE.toString(), moduleDetails.get(selectedModuleIndex).getModuleCode());

                            LectureSessionPage lectureSessionPage = new LectureSessionPage();
                            lectureSessionPage.setArguments(bundle);
                            // Navigate to the next Fragment
                            ((NavigationHost) getActivity()).navigateTo(lectureSessionPage, true);
                        }

                    }
                }

            }
        });

        initiateModuleListTextView(view);

        BroadcastAddressGenerator addressGenerator = new BroadcastAddressGenerator(view);
        broadcastIp = addressGenerator.getBroadcastIp();
        initializeLectureSession();
        return view;
    }

    public synchronized void updateModuleDetails(String moduleCode, String multicastIpAddress){
        try {
            if(moduleDetails.contains(new ModuleDetailItem(moduleCode))) {
                for (int i = 0; i< moduleDetails.size(); i++){
                    if(moduleDetails.get(i).getModuleCode().equals(moduleCode)){
                        moduleDetails.set(i, new ModuleDetailItem(moduleCode, InetAddress.getByName(multicastIpAddress), System.currentTimeMillis()));
                        Log.i(Constants.STUDENT_HOME_PAGE_LOG_TAG, "Module Details Updated: Heartbeat");
                        break;
                    }
                }
            } else {
                moduleDetails.add(new ModuleDetailItem(moduleCode, InetAddress.getByName(multicastIpAddress), System.currentTimeMillis()));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listModuleItemAdapter.notifyDataSetChanged();
                    }
                });
                Log.i(Constants.STUDENT_HOME_PAGE_LOG_TAG, "Module Details Adding Group: "+ moduleCode);
            }

        } catch (Exception ex){
            Log.e(Constants.STUDENT_HOME_PAGE_LOG_TAG, "Update Module Details Failed!!! ");
        }

    }

    private void initializeLectureSession(){
        //ModuleDetails information update
        moduleDetailsUpdate = new ModuleDetailsUpdate();
        Thread moduleDetailsUpdateThread = new Thread(moduleDetailsUpdate);
        moduleDetailsUpdateThread.start();

        //start the listening of the broadcast
        createMeetingBroadcast = new CreateMeetingBroadcast( broadcastIp);
        createMeetingBroadcast.listenCreateMeeting(this);

    }

    private class ModuleDetailsUpdate implements Runnable{
        private volatile boolean exit = false;
        @Override
        public void run() {
            try {
                while(!exit){

                    // check if any group discussions have timed out, if so remove them
                    long now = System.currentTimeMillis();

                    List<Integer> timedOutPeerIndices = new ArrayList<>();
                    for (int i =0; i < moduleDetails.size(); i++) {
                        if (now - moduleDetails.get(i).getHeartbeat() > Constants.CLASSROOM_LECTURE_DISCOVERY_TIMEOUT_MILLISECONDS) {
                            timedOutPeerIndices.add(i);
                        }
                    }

                    if (timedOutPeerIndices.size()>0){
                        for (Integer peer: timedOutPeerIndices) {
                            Log.i(Constants.STUDENT_HOME_PAGE_LOG_TAG, "Group Discussion Details Removing group: "+moduleDetails.get(peer).getModuleCode());
                            moduleDetails.remove(peer.intValue());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listModuleItemAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                    Thread.sleep(Constants.CLASSROOM_LECTURE_HEARTBEAT_INTERVAL);

                }
            } catch (InterruptedException e) {
                Log.e(Constants.STUDENT_HOME_PAGE_LOG_TAG, "Update Group Discussion Details Failed!!! ");
            }
        }

        public void stop() {
            exit = true;
        }
    }

    private void initiateModuleListTextView(View view){

        listModuleItemAdapter = new ListModuleItemAdapter(this.requireContext(), moduleDetails);
        moduleListTextView.setAdapter(listModuleItemAdapter);

        moduleListTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < parent.getCount()) {
                    selectedModuleIndex = position;
                } else {
                    selectedModuleIndex = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where no item is selected
            }
        });

        moduleListTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedModuleIndex = position;
            }
        });

    }

    @Override
    public void onPause() {
        backPressedListener=null;
        super.onPause();
        // Clear the selected value
        selectedModuleIndex = -1;
//        if(moduleListTextView!=null)
//            moduleListTextView.setText("");
//        if(listModuleItemAdapter!=null)
//            listModuleItemAdapter.clear();
    }

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
        if(moduleDetailsUpdate!=null)
            moduleDetailsUpdate.stop();
        requireFragmentManager().popBackStack();
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
