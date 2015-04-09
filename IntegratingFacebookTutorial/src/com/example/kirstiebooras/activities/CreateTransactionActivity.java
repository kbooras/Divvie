package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.adapters.GroupsSpinnerAdapter;
import com.example.kirstiebooras.helpers.ParseTools;
import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity to create a new transaction within a group.
 * Created by kirstiebooras on 1/29/15.
 */
public class CreateTransactionActivity extends Activity {

    private static final String TAG = "CreateTransaction";

    private ParseTools mParseTools;
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

        ParseTools parseTools = ((DivvieApplication) getApplication()).getParseTools();
        ArrayList<ParseObject> groupsList = new ArrayList<ParseObject>();
        groupsList.addAll(parseTools.getLocalData(Constants.CLASSNAME_GROUP));

        ArrayAdapter<ParseObject> mAdapter =
                new GroupsSpinnerAdapter(getApplicationContext(), groupsList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);

        // If we are directed here from a click in the groups fragment, set that group as selected
        if(getIntent().hasExtra("groupIndex")) {
            mSpinner.setSelection(getIntent().getIntExtra("groupIndex", 0));
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
}