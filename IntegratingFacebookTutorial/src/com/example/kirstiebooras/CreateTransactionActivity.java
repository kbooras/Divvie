package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to create a new transaction within a group.
 * Created by kirstiebooras on 1/29/15.
 */
public class CreateTransactionActivity extends Activity {

    private static final String TAG = "CreateTransactionActivity";

    private ArrayAdapter<String> mAdapter;
    private List<String> mGroupsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_transaction_activity);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
        Spinner spinner = (Spinner) rl.findViewById(R.id.groupsDropDown);

        mGroupsList = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mGroupsList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);

        getGroupsFromParse();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroupsFromParse();
    }

    private void getGroupsFromParse() {
        if (ParseUser.getCurrentUser() != null) {
            Log.v("current: ", ParseUser.getCurrentUser().getEmail());
            ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
            groupQuery.whereEqualTo("users", ParseUser.getCurrentUser().getEmail());
            groupQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    //Query should generate Spinner data using an array adapter
                    mGroupsList.clear();
                    for (ParseObject obj : parseObjects) {
                        if (obj != null) {
                            mGroupsList.add(obj.getString("name"));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
