package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_transaction_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
        mSpinner = (Spinner) rl.findViewById(R.id.groupsDropDown);

        mGroupsList = new ArrayList<String>();
        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mGroupsList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);

        getGroupsFromParse();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.secondary_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                ParseUser.logOut();
                Log.v(TAG, "User signed out!");
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSplitBillClick() {
        String personOwed = ParseUser.getCurrentUser().getObjectId();
        String group = mSpinner.getSelectedItem().toString();
        EditText description = (EditText) findViewById(R.id.description);
        String descriptionTxt = description.getText().toString();
        EditText amount = (EditText) findViewById(R.id.amount);
        Double amountValue = Double.valueOf(amount.getText().toString());

        // TODO: add the group id instead of group name
        // Create a transaction
        ParseObject newTransaction = new ParseObject("Transaction");
        newTransaction.put("personOwedID", personOwed);
        newTransaction.put("group", group);
        newTransaction.put("description", descriptionTxt);
        newTransaction.put("totalAmount", amountValue);
        /*try {
            newTransaction.saveInBackground();
            Log.v(TAG, "Saved new transaction");
        } catch (ParseException e) {
            Log.v(TAG, "Save new transaction failed");
            e.printStackTrace();
        }*/

        // Create charges for other users
        splitBill(personOwed, group, descriptionTxt, amountValue);
    }

    private void splitBill(String personOwed, String group, String description, Double amount) {
        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo("objectID", group);
        groupQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // TODO: charge all members of the group an equal amount
                // TODO: account for odd numbers when splitting
            }
        });
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
                    // Query should generate Spinner data using an array adapter
                    // Create a key-value pairing the name to the object id so we can get the id
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
