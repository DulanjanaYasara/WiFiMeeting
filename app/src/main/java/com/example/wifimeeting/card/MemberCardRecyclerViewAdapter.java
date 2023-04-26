package com.example.wifimeeting.card;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;

import java.util.List;

/**
 * Adapter used to show a simple grid of members.
 */
public class MemberCardRecyclerViewAdapter extends RecyclerView.Adapter<MemberCardViewHolder> {

    private List<MemberEntry> memberList;
    private View layoutView;

    public MemberCardRecyclerViewAdapter(List<MemberEntry> productList) {
        this.memberList = productList;
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
            MemberEntry member = memberList.get(position);
            holder.memberName.setText(member.memberName);

            //background color change based on mute unmute
            holder.materialCardViewLayout.setCardBackgroundColor(
                    member.isMute?
                            ContextCompat.getColor(layoutView.getContext(), R.color.toolbarIconColor):
                            ContextCompat.getColor(layoutView.getContext(), R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }
}
