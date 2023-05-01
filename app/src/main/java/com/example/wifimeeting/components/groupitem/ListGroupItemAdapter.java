package com.example.wifimeeting.components.groupitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wifimeeting.R;

import java.util.ArrayList;

public class ListGroupItemAdapter extends ArrayAdapter<DiscussionGroupItem> {

    public ListGroupItemAdapter(@NonNull Context context, ArrayList<DiscussionGroupItem> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.option_group_item, parent, false);
        }
        TextView groupNameOptionItem = currentItemView.findViewById(R.id.group_name_option_item);
        TextView memberCountOptionItem = currentItemView.findViewById(R.id.members_count_option_item);

        DiscussionGroupItem groupItem = getItem(position);
        groupNameOptionItem.setText(groupItem.getGroupName());
        memberCountOptionItem.setText(groupItem.getNoOfMembers());

        return currentItemView;
    }

}
