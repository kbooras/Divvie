package com.example.kirstiebooras;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment representing the groups view.
 * Displays all groups the user has created.
 * Created by kirstiebooras on 1/19/15.
 */
public class GroupsFragment extends ListFragment {

    private static final String TAG = "GroupsFragment";
    private static final String GROUPS_LABEL = "groups";
    private ArrayList<ParseObject> mGroups;
    private GroupsAdapter mAdapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGroups = new ArrayList<ParseObject>();
        mAdapter = new GroupsAdapter(getActivity().getBaseContext(), mGroups);
        setListAdapter(mAdapter);

        if (ParseUser.getCurrentUser() != null) {
            getDataFromParse();
            Log.d(TAG, "onCreate");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove lines between list views
        getListView().setDivider(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getDataFromParse();
    }

    private void getDataFromParse() {
        if (ParseUser.getCurrentUser() != null) {
//            HashMap<String, Object> map = new HashMap<String, Object>();
//            map.put("currentUser", ParseUser.getCurrentUser().getEmail());
//            ParseCloud.callFunctionInBackground("getGroupsDescending", map,
//                    new FunctionCallback<Object>() {
//                        public void done(Object results, ParseException e) {
//                            if (e == null) {
//                                mGroups.clear();
//                                mGroups.addAll((List<ParseObject>) results);
//                                mAdapter.notifyDataSetChanged();
//                            }
//                            else {
//                                Log.v(TAG, e.toString());
//                            }
//                        }
//                    }
//            );
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
            query.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        // There was an error or the network wasn't available.
                        Log.v(TAG, "Query error: " + e.getMessage());
                        return;
                    }

                    // Release any objects previously pinned for this query.
                    Log.v(TAG, "Found " + parseObjects.size() + " groups");
                    ParseObject.unpinAllInBackground(GROUPS_LABEL, parseObjects, new DeleteCallback() {
                        public void done(ParseException e) {
                            if (e != null) {
                                // There was some error.
                                Log.v(TAG, "Unpin error: " + e.getMessage());
                                return;
                            }

                            // Add the latest results for this query to the cache.
                            Log.v(TAG, "Pinned " + parseObjects.size() + " groups");
                            ParseObject.pinAllInBackground(GROUPS_LABEL, parseObjects);
                        }
                    });
                    mGroups.clear();
                    mGroups.addAll(parseObjects);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
    }

}