package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        // TODO fix formatting of array adapter
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

    public void onSplitBillClick() {
        String personOwed = ParseUser.getCurrentUser().getEmail();

        ParseObject group = (ParseObject) mSpinner.getSelectedItem();
        String groupID = group.getObjectId();

        EditText description = (EditText) findViewById(R.id.description);
        String descriptionTxt = description.getText().toString();

        EditText amount = (EditText) findViewById(R.id.amount);
        double amountValue = Double.valueOf(amount.getText().toString());

        // Create a transaction
        ParseObject newTransaction = new ParseObject("Transaction");
        String transactionID = newTransaction.getObjectId();

        newTransaction.put("groupID", groupID);
        newTransaction.put("personOwed", personOwed);
        newTransaction.put("description", descriptionTxt);
        newTransaction.put("totalAmount", amountValue);
        newTransaction.put("amountOwed", amountValue);

        newTransaction.saveInBackground();
        Log.v(TAG, "Saved new transaction");

        // Create charges for other users
        splitBill(transactionID, personOwed, groupID, amountValue);
    }

    private void splitBill(final String transactionID, final String personOwed, String groupID,
                           final double amount) {
        // Charge every member of the group except the person owed
        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo("objectID", groupID);
        groupQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<String> members = (ArrayList<String>) parseObjects.get(0).get("users");
                // Split the amount equally
                double dividedAmount = amount / members.size();
                BigDecimal bd = new BigDecimal(dividedAmount);
                String charge = bd.setScale(2,BigDecimal.ROUND_FLOOR).toString();
                for (String member : members) {
                    // Skip the person owed
                    if (!member.equals(personOwed)) {
                        ParseObject newCharge = new ParseObject("IndividualCharge");
                        newCharge.put("transactionID", transactionID);
                        newCharge.put("user", member);
                        newCharge.put("charge", charge);
                        newCharge.put("paid", false);

                        newCharge.saveInBackground();
                        Log.v(TAG, "Saved individual charge.");
                    }
                }
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
                            mGroupsList.add(obj);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
