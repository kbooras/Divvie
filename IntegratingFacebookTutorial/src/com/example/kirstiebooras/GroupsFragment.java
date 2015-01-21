package com.example.kirstiebooras;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment representing the groups view.
 * Displays all groups the user has created.
 * Created by kirstiebooras on 1/19/15.
 */
public class GroupsFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get data
        final ArrayList<ParseObject> groups = new ArrayList<ParseObject>();
        final GroupsAdapter adapter = new GroupsAdapter(getActivity().getBaseContext(),
                groups);
        setListAdapter(adapter);

        if (ParseUser.getCurrentUser() != null) {
            Log.v("current: ", ParseUser.getCurrentUser().getEmail());
            ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
            groupQuery.whereEqualTo("users", ParseUser.getCurrentUser().getEmail());
            groupQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    //Query should generate group listview using an array adapter
                    for (int i = 0; i < parseObjects.size(); i++) {
                        Log.v("groups: ", parseObjects.get(i).get("name").toString() +
                                parseObjects.get(i).get("users").toString());
                        groups.add(parseObjects.get(i));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // do something with the data
    }

}