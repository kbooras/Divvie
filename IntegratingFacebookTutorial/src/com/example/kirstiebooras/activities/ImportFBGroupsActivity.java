package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.Constants;
import com.example.kirstiebooras.helpers.ParseTools;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity to import a user's groups from Facebook.
 * Created by kirstiebooras on 4/9/15.
 */
public class ImportFBGroupsActivity extends Activity {

    private static final String TAG = "ImportGroupsActivity";
    private static final String FB_GROUPS = "FBGroups";
    private static final String FB_GROUP_IDS = "FBGroupIDs";
    private static final String NUM_GROUPS_SELECTED = "numGroupsSelected";
    private ListView mListView;
    private ArrayList<String> mFBGroups;
    private ArrayList<String> mFBGroupIDs;
    private ArrayAdapter<String> mAdapter;
    private int mNumGroupsSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.import_fb_groups_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mFBGroups = savedInstanceState.getStringArrayList(FB_GROUPS);
            mFBGroupIDs = savedInstanceState.getStringArrayList(FB_GROUP_IDS);
            mNumGroupsSelected = savedInstanceState.getInt(NUM_GROUPS_SELECTED);
        }
        else {
            mFBGroups = new ArrayList<String>();
            mFBGroupIDs = new ArrayList<String>();
            mNumGroupsSelected = 0;
            importGroupsFromFacebook();
        }

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, mFBGroups);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        final TextView numGroupsTextView = (TextView) findViewById(R.id.numGroupsSelected);
        numGroupsTextView.setText(
                String.format(getString(R.string.num_groups_selected),
                        mNumGroupsSelected));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mNumGroupsSelected = mListView.getCheckedItemCount();
                numGroupsTextView.setText(
                        String.format(getString(R.string.num_groups_selected),mNumGroupsSelected));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(FB_GROUPS, mFBGroups);
        outState.putStringArrayList(FB_GROUP_IDS, mFBGroupIDs);
        outState.putInt(NUM_GROUPS_SELECTED, mNumGroupsSelected);
        super.onSaveInstanceState(outState);
    }

    private void importGroupsFromFacebook() {
        Log.d(TAG, "importGroupsFromFacebook");
        // TODO grey out any groups already imported
        ParseFacebookUtils.initialize(getString(R.string.app_id));
        Session session = ParseFacebookUtils.getSession();
        String facebookId = ParseUser.getCurrentUser().getString(Constants.FACEBOOK_ID);
        if (session != null && session.isOpened()) {
            new Request(
                    session,
                    "/" + facebookId + "/groups",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            JSONObject jsonObject = response.getGraphObject().getInnerJSONObject();
                            try {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    mFBGroupIDs.add(i, jsonArray.getJSONObject(i).getString("id"));
                                    mFBGroups.add(i, jsonArray.getJSONObject(i).getString("name"));
                                }
                                mAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                Log.e(TAG, "Error fetching FB groups: " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.import_fb_groups_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.done:
                ParseTools parseTools = ((DivvieApplication) getApplication()).getParseTools();
                SparseBooleanArray checked = mListView.getCheckedItemPositions();

                for (int i = 0; i < checked.size(); i++) {
                    if (checked.valueAt(i)) {
                        HashMap<String, String> groupMembers = getGroupMembers(mFBGroupIDs.get(i));
                        parseTools.createFBGroupParseObject(mFBGroups.get(i), groupMembers);
                    }
                }

                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private HashMap<String, String> getGroupMembers(String groupId) {
        final HashMap<String, String> facebookGroupMembers = new HashMap<String, String>();
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            new Request(
                    session,
                    "/" + groupId + "/members",
                    null,
                    HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            JSONObject jsonObject = response.getGraphObject().getInnerJSONObject();
                            try {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    facebookGroupMembers.put(
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("id"));
                                }

                            } catch (JSONException e) {
                                Log.e(TAG, "Error fetching FB group members: " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();
        }
        return facebookGroupMembers;
    }
}
