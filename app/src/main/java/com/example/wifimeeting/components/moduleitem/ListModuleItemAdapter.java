package com.example.wifimeeting.components.moduleitem;

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

public class ListModuleItemAdapter extends ArrayAdapter<ModuleDetailItem> {

    public ListModuleItemAdapter(@NonNull Context context, ArrayList<ModuleDetailItem> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.option_module_item, parent, false);
        }
        TextView moduleNameOptionItem = currentItemView.findViewById(R.id.module_name_option_item);

        ModuleDetailItem groupItem = getItem(position);
        moduleNameOptionItem.setText(groupItem.getModuleCode());

        return currentItemView;
    }

}
