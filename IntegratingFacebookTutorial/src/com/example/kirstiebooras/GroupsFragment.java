package com.example.kirstiebooras;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

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

        //if (savedInstanceState == null) {
        // Get data from HomeActivity member variables
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
    private void bindData(List<ParseObject> data) {
        Log.v(TAG, "bindData");
        mGroups.clear();
        mGroups.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
    }

}