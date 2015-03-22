package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity to create a new transaction within a group.
 * Created by kirstiebooras on 1/29/15.
 */
public class CreateTransactionActivity extends Activity {

    private static final String TAG = "CreateTransactionActivity";

    private ParseTools mParseTools;
    private ArrayAdapter<ParseObject> mAdapter;
    private ArrayList<ParseObject> mGroupsList;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.create_transaction_activity);

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
        mSpinner = (Spinner) rl.findViewById(R.id.groupsDropDown);

        mGroupsList = new ArrayList<ParseObject>();
        mAdapter = new GroupsSpinnerAdapter(getApplicationContext(), mGroupsList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);

        getGroupsFromParse();
        // TODO: get this from the HomeActivity. Just need to send groupName and group objectId
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
                Log.i(TAG, "User signed out!");
                mParseTools.unpinData(Constants.CLASSNAME_TRANSACTION);
                mParseTools.unpinData(Constants.CLASSNAME_GROUP);
                startSigninRegisterActivity();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSigninRegisterActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onSplitBillClick(View view) {
        if(!isNetworkConnected()) {
            Log.i(TAG, "Cannot create Transaction. Not connected to internet.");
            displayNoNetworkConnectionMessage();
            return;
        }
        // Get user input
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String amount = ((EditText) findViewById(R.id.amount)).getText().toString();

        // Check the form is complete
        if (description.equals("") || amount == null) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.complete_form_toast), Toast.LENGTH_LONG).show();
            return;
        }

        // Check for valid monetary input
        if (!validMonetaryInput(amount)) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.invalid_amount_toast),
                    Toast.LENGTH_LONG).show();
            return;
        }

        double totalAmount = Double.valueOf(amount);

        // Get person owed info
        ParseUser personOwed = ParseUser.getCurrentUser();
        String personOwedEmail = personOwed.getEmail();

        // Get group info
        ParseObject group = (ParseObject) mSpinner.getSelectedItem();
        String groupId = group.getObjectId();

        // Create the object
        mParseTools.createTransactionParseObject(groupId, personOwedEmail, description, totalAmount);

        // Send emails to group members
        // TODO sendEmails(personOwedName, groupName, description, splitAmount, (String[]) members.toArray());

        setResult(RESULT_OK);
        finish();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void displayNoNetworkConnectionMessage() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.no_network_connection))
                .setMessage(getString(R.string.network_connection_create_transaction_alert_message))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    /*
     * Check if the amount entered by the user is valid.
     * From www.RegExLib.com by Kirk Fuller, Gregg Durishan.
     */
    private boolean validMonetaryInput(String amount) {
        return amount.matches("^\\$?\\-?([1-9]{1}[0-9]{0,2}(\\,\\d{3})*(\\.\\d{0,2})?|[1-9]{1}\\d{0,}(\\.\\d{0,2})?|0(\\.\\d{0,2})?|(\\.\\d{1,2}))$|^\\-?\\$?([1-9]{1}\\d{0,2}(\\,\\d{3})*(\\.\\d{0,2})?|[1-9]{1}\\d{0,}(\\.\\d{0,2})?|0(\\.\\d{0,2})?|(\\.\\d{1,2}))$|^\\(\\$?([1-9]{1}\\d{0,2}(\\,\\d{3})*(\\.\\d{0,2})?|[1-9]{1}\\d{0,}(\\.\\d{0,2})?|0(\\.\\d{0,2})?|(\\.\\d{1,2}))\\)$");
    }

    /*
     * Send email to every one splitting the transaction.
     */
    private void sendEmails(String fromName, String groupName, String chargeDescription,
                            String amount, String[] members) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("fromName", fromName);
        map.put("groupName", groupName);
        map.put("chargeDescription", chargeDescription);
        map.put("amount", amount);
        map.put("key", getString(R.string.MANDRILL_API_KEY));
        mParseTools.sendNewTransactionEmails(map, members);
    }

    private void getGroupsFromParse() {
        if (ParseUser.getCurrentUser() != null) {
            ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
            groupQuery.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
            groupQuery.fromLocalDatastore();
            try {
                List<ParseObject> groups = groupQuery.find();
                Log.i(TAG, "Found " + groups.size() + " objects.");
                // Query should generate Spinner data using an array adapter
                // Create a key-value pairing the name to the object id so we can get the id
                mGroupsList.clear();
                mGroupsList.addAll(groups);
                mAdapter.notifyDataSetChanged();
            }
            catch (ParseException e) {
                Log.e(TAG, "Query error: " + e.getMessage());
            }
        }
    }
}
