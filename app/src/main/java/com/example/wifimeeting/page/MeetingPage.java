package com.example.wifimeeting.page;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.example.wifimeeting.card.MemberEntry;
import com.example.wifimeeting.card.MemberGridItemDecoration;
import com.example.wifimeeting.utils.Constants;
import com.example.wifimeeting.utils.MyDetails;
import com.example.wifimeeting.utils.Role;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MeetingPage extends Fragment {

    MaterialButton leaveButton, muteUnmuteButton;
    TextView memberName;

    private long muteUnmuteButtonLastClickTime = 0;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.meeting_page, container, false);
        leaveButton = view.findViewById(R.id.leave_button);
        muteUnmuteButton = view.findViewById(R.id.mute_unmute_button);
        memberName = view.findViewById(R.id.member_name);

        if(this.getArguments() != null){
            readyUiView(this.getArguments());
        }

        // Set up the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
        MemberCardRecyclerViewAdapter adapter = new MemberCardRecyclerViewAdapter(
                MemberEntry.initProductEntryList(getResources()));
        recyclerView.setAdapter(adapter);
        int largePadding = getResources().getDimensionPixelSize(R.dimen.member_grid_spacing_small);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.member_grid_spacing_small);
        recyclerView.addItemDecoration(new MemberGridItemDecoration(largePadding, smallPadding));

        leaveButton.setOnClickListener(leaveButtonClickEvent());
        muteUnmuteButton.setOnClickListener(muteUnmuteButtonClickEvent());
        
        return view;
    }

    private void readyUiView (Bundle bundle){
        if(bundle.getString(MyDetails.NAME.toString())!= null)
            memberName.setText(bundle.getString(MyDetails.NAME.toString()));
        if(bundle.getBoolean(MyDetails.IS_MUTE.toString())){

            if(bundle.getString(MyDetails.ROLE.toString()) != null){
                muteUnmuteButton.setText(
                        bundle.getString(MyDetails.ROLE.toString()).equals(Role.LECTURER.toString())?
                                R.string.mute:
                                R.string.unmute);
                muteUnmuteButton.setIcon(
                        bundle.getString(MyDetails.ROLE.toString()).equals(Role.LECTURER.toString())?
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

                muteUnmuteButton.setText(
                        muteUnmuteButton.getText().toString().equals(getString(R.string.unmute))?
                                R.string.mute:
                                R.string.unmute);
                muteUnmuteButton.setIcon(
                        muteUnmuteButton.getText().toString().equals(getString(R.string.unmute))?
                                getResources().getDrawable(R.drawable.baseline_mic_24):
                                getResources().getDrawable(R.drawable.baseline_mic_off_24));

            }
        };
    }

    private View.OnClickListener leaveButtonClickEvent (){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.leave_meeting_confirmation)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        };
    }
}
