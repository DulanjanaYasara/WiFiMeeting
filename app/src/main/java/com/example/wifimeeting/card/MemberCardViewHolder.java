package com.example.wifimeeting.card;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.R;

public class MemberCardViewHolder extends RecyclerView.ViewHolder {

    public TextView memberName;

    public MemberCardViewHolder(@NonNull View itemView) {
        super(itemView);
        memberName = itemView.findViewById(R.id.member_name);
    }
}
