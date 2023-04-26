package com.example.wifimeeting.card;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;
import com.google.android.material.card.MaterialCardView;

public class MemberCardViewHolder extends RecyclerView.ViewHolder {

    public TextView memberName;
    public MaterialCardView materialCardViewLayout;

    public MemberCardViewHolder(@NonNull View itemView) {
        super(itemView);
        memberName = (TextView)itemView.findViewById(R.id.member_name);
        materialCardViewLayout = (MaterialCardView)itemView.findViewById(R.id.material_card_view_layout);
    }
}
