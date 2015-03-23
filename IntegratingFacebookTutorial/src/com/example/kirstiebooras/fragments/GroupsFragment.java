package com.example.kirstiebooras.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;

import com.example.kirstiebooras.activities.HomeActivity;
import com.example.kirstiebooras.adapters.GroupsAdapter;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing the groups view.
 * Displays all groups the user has created.
 * Created by kirstiebooras on 1/19/15.
 */
public class GroupsFragment extends ListFragment {

    private static final String TAG = "GroupsFragment";
    private ArrayList<ParseObject> mGroups;
    private GroupsAdapter mAdapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGroups = new ArrayList<ParseObject>();
        mAdapter = new GroupsAdapter(getActivity().getBaseContext(), mGroups);
        setListAdapter(mAdapter);

        // Workaround for problem where fragments are not available in the HomeActivity onCreate method
        // to initially set the data.
        HomeActivity homeActivity = (HomeActivity) getActivity();
        bindData(homeActivity.getGroupsData());

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove lines between list views
        getListView().setDivider(null);
    }

    /**
     * Attach the data passed in from HomeActivity to the adapter
     * @param data: The data from HomeActivity
     */
    public void bindData(List<ParseObject> data) {
        Log.v(TAG, "bindData");
        if (data == null) {
            Log.e(TAG, "Data is null");
            return;
        }
        mGroups.clear();
        mGroups.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

}