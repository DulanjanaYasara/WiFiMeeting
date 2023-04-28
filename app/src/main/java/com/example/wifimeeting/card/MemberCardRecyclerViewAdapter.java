package com.example.wifimeeting.card;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Adapter used to show a simple grid of members.
 */
public class MemberCardRecyclerViewAdapter extends RecyclerView.Adapter<MemberCardViewHolder> {

    private LinkedHashMap<String, Boolean> memberList;
    private View layoutView;

    public MemberCardRecyclerViewAdapter(LinkedHashMap<String, Boolean> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_card, parent, false);
        return new MemberCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemberCardViewHolder holder, final int position) {

        if (memberList != null && position < memberList.size()) {

            Set<String> keySet = memberList.keySet();
            List<String> listKeys = new ArrayList<>(keySet);
            String key = listKeys.get(position);
            Boolean value = memberList.get(key);

            holder.memberName.setText(key);

            //background color change based on mute unmute
            holder.materialCardViewLayout.setCardBackgroundColor(
                    Boolean.TRUE.equals(value) ?
                            ContextCompat.getColor(layoutView.getContext(), R.color.toolbarIconColor):
                            ContextCompat.getColor(layoutView.getContext(), R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(LinkedHashMap<String, Boolean> newMemberList) {
        memberList = newMemberList;

        // notify the adapter that the data has changed
        notifyDataSetChanged();
    }
}
