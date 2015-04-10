package com.example.kirstiebooras.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.kirstiebooras.helpers.Constants;
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
public class ImportGroupsActivity extends ListActivity {

    private static final String TAG = "ImportGroupsActivity";
    private static final String FB_GROUPS = "FBGroups";
    private static final String FB_GROUP_IDS = "FBGroupIDs";
    private ArrayList<String> mFBGroups;
    private ArrayList<String> mFBGroupIDs;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mFBGroups = savedInstanceState.getStringArrayList(FB_GROUPS);
            mFBGroupIDs = savedInstanceState.getStringArrayList(FB_GROUP_IDS);
        }
        else {
            mFBGroups = new ArrayList<String>();
            mFBGroupIDs = new ArrayList<String>();
            importGroupsFromFacebook();
        }

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, mFBGroups);
        setListAdapter(mAdapter);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(FB_GROUPS, mFBGroups);
        outState.putStringArrayList(FB_GROUP_IDS, mFBGroupIDs);
        super.onSaveInstanceState(outState);
    }

    private void importGroupsFromFacebook() {
        Log.d(TAG, "importGroupsFromFacebook");
        // TODO perhaps move to the cloud since we are parsing
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
                                    mFBGroups.add(i, jsonArray.getJSONObject(i).getString("name"));
                                    mFBGroupIDs.add(i, jsonArray.getJSONObject(i).getString("id"));
                                }
                                mAdapter.notifyDataSetChanged();
                                // TODO pass the hashmap to method to create page to select groups to import
                            } catch (JSONException e) {
                                Log.e(TAG, "Error fetching FB groups: " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();
        }
    }

    private void getGroupMembers(String groupId) {
        // TODO perhaps move to the cloud since we are parsing
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            final HashMap<String, String> facebookGroupMembers = new HashMap<String, String>();
            new Request(
                    session,
                    "/" + groupId + "/?fields=members",
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
                                    // TODO Then, have to query if they have a parse acct linked with id or if their email is public
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error fetching FB group members: " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
