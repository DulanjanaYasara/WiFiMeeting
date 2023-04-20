package com.example.wifimeeting.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimeeting.card.MemberCardRecyclerViewAdapter;
import com.example.wifimeeting.card.MemberEntry;
import com.example.wifimeeting.card.MemberGridItemDecoration;
import com.example.wifimeeting.R;

public class MeetingPage extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.meeting_page, container, false);
        
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
        
        return view;
    }
}
