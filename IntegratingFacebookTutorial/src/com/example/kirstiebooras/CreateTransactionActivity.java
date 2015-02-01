package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity to create a new transaction within a group.
 * Created by kirstiebooras on 1/29/15.
 */
public class CreateTransactionActivity extends Activity {

    private static final String TAG = "CreateTransactionActivity";

    private ArrayAdapter<ParseObject> mAdapter;
    private ArrayList<ParseObject> mGroupsList;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_transaction_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
        mSpinner = (Spinner) rl.findViewById(R.id.groupsDropDown);

        mGroupsList = new ArrayList<ParseObject>();
        mAdapter = new GroupsSpinnerAdapter(getApplicationContext(), mGroupsList);
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

    public void onSplitBillClick(View view) {
        final String personOwed = ParseUser.getCurrentUser().getEmail();

        ParseObject group = (ParseObject) mSpinner.getSelectedItem();
        final String groupId = group.getObjectId();
        final String groupName = group.getString("name");

        EditText description = (EditText) findViewById(R.id.description);
        final String descriptionTxt = description.getText().toString();
        // TODO: validate edit text value. Maybe create monetary input

        EditText amount = (EditText) findViewById(R.id.amount);
        final double amountValue = Double.valueOf(amount.getText().toString());

        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo("objectId", groupId);
        groupQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<String> members = (ArrayList<String>) parseObjects.get(0).get("users");
                members.remove(personOwed);

                double dividedAmount = amountValue / (members.size() + 1);
                BigDecimal bd = new BigDecimal(dividedAmount);
                String charge = bd.setScale(2,BigDecimal.ROUND_FLOOR).toString();

                createTransactionParseObject(groupId, groupName, personOwed, descriptionTxt,
                        amountValue, members, Double.valueOf(charge));

                // TODO: send emails after you create the object
                finish();
            }
        });
    }

    private void createTransactionParseObject(String groupId, String groupName,
                                              String personOwed, String descriptionTxt,
                                              double amountValue, ArrayList<String> members,
                                              double splitAmount) {
        ParseObject newTransaction = new ParseObject("Transaction");

        newTransaction.put("groupId", groupId);
        newTransaction.put("groupName", groupName);
        newTransaction.put("personOwed", personOwed);
        newTransaction.put("description", descriptionTxt);
        newTransaction.put("totalAmount", amountValue);
        newTransaction.put("splitAmount", splitAmount);
        newTransaction.put("members", members);

       Integer[] paid = new Integer[members.size()];
        for (int i = 0; i < paid.length; i++) {
            paid[i]= 0;
        }

        newTransaction.addAll("paid", Arrays.asList(paid));
        newTransaction.put("complete", false);
        newTransaction.saveInBackground();

        Log.v(TAG, "Saved new transaction");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Resume");
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
                            mGroupsList.add(obj);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    Log.v(TAG, "Notify data set changed");
                }
            });
        }
    }
}
