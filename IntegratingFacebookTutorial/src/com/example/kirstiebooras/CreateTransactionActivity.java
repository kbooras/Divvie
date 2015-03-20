package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
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

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
        // Get user input
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String amount = ((EditText) findViewById(R.id.amount)).getText().toString();

        // Check the form is complete
        if (description.equals("") || amount == null) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Check for valid monetary input
        if (!validMonetaryInput(amount)) {
            return;
        }

        double amountValue = Double.valueOf(amount);
        String totalAmount = String.format("%.2f", amountValue);

        // Get person owed info
        ParseUser personOwed = ParseUser.getCurrentUser();
        String personOwedEmail = personOwed.getEmail();
        String personOwedName = personOwed.getString(Constants.USER_FULL_NAME);

        // Get group info
        ParseObject group = (ParseObject) mSpinner.getSelectedItem();
        String groupId = group.getObjectId();
        String groupName = group.getString(Constants.GROUP_NAME);

        // Get members of the group to create arrays for paid and datePaid and to get split amount
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) group.get(Constants.GROUP_MEMBERS);
        @SuppressWarnings("unchecked")
        ArrayList<String> displayNames = (ArrayList<String>) group.get(Constants.GROUP_DISPLAY_NAMES);

        // Get split amount
        String splitAmount = getSplitAmount(amountValue, members.size());

        createTransactionParseObject(groupId, groupName, personOwedEmail, description,
                totalAmount, members, displayNames, splitAmount);

        // Send emails to group members
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("toEmail", personOwedEmail);
        map.put("fromName", personOwedName);
        map.put("groupName", groupName);
        map.put("chargeDescription", description);
        map.put("amount", splitAmount);

        for (String email : members) {
            if (!email.equals(personOwedEmail)) {
                // sendEmails(email, map);
            }
        }

        finish();
    }

    private boolean validMonetaryInput(String amount) {
        int decimal = amount.lastIndexOf('.');
        int decimalPlaces = amount.substring(decimal+1).length();
        if (decimalPlaces > 2 || decimalPlaces < -1) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.invalid_amount_toast),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private String getSplitAmount(double amountValue, double numMembers) {
        double dividedAmount = amountValue / numMembers;
        BigDecimal bd = new BigDecimal(dividedAmount);
        return bd.setScale(2, BigDecimal.ROUND_FLOOR).toString();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        // TODO Tell homeactivity specifically what object to get using data.putExtra()
        data.putExtra("className", "Transaction");
        setResult(RESULT_OK, data);
        super.finish();
    }

    private void createTransactionParseObject(String groupId, String groupName,
                                              String personOwed, String descriptionTxt,
                                              String totalAmount, ArrayList<String> members,
                                              ArrayList<String> displayNames, String splitAmount) {
        ParseObject newTransaction = new ParseObject("Transaction");

        newTransaction.put(Constants.TRANSACTION_GROUP_ID, groupId);
        newTransaction.put(Constants.TRANSACTION_GROUP_NAME, groupName);
        newTransaction.put(Constants.TRANSACTION_PERSON_OWED, personOwed);
        newTransaction.put(Constants.TRANSACTION_DESCRIPTION, descriptionTxt);
        newTransaction.put(Constants.TRANSACTION_TOTAL_AMOUNT, totalAmount);
        newTransaction.put(Constants.TRANSACTION_SPLIT_AMOUNT, splitAmount);
        newTransaction.put(Constants.GROUP_MEMBERS, members);
        newTransaction.put(Constants.GROUP_DISPLAY_NAMES, displayNames);

        // Set paid values and date paid values. PersonOwed is set as paid.
        ArrayList<Integer> paid = new ArrayList<Integer>(members.size());
        ArrayList<String> datePaid = new ArrayList<String>(members.size());
        for (String user : members) {
            if (user.equals(personOwed)) {
                paid.add(1);
                String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
                String date = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
                datePaid.add(month + "/" + date);
            } else {
                paid.add(0);
                datePaid.add("");
            }
        }

        newTransaction.put(Constants.TRANSACTION_PAID, paid);
        newTransaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);
        newTransaction.put(Constants.TRANSACTION_COMPLETE, false);
        newTransaction.saveEventually();
        Log.v(TAG, "Saved new transaction");
        
    }

    private void sendEmails(String email, HashMap<String, Object> map) {
        map.put("key", getString(R.string.MANDRILL_API_KEY));
        map.put("toEmail", email);
        ParseCloud.callFunctionInBackground("sendChargeEmail", map, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Log.v(TAG, (String) o);
                } else {
                    e.printStackTrace();
                    Log.v(TAG, "Send email error: " + e.toString());
                }
            }
        });
    }

    private void getGroupsFromParse() {
        if (ParseUser.getCurrentUser() != null) {
            ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
            groupQuery.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
            // TODO find in background
            try {
                List<ParseObject> groups = groupQuery.find();
                // Query should generate Spinner data using an array adapter
                // Create a key-value pairing the name to the object id so we can get the id
                mGroupsList.clear();
                mGroupsList.addAll(groups);
                mAdapter.notifyDataSetChanged();
                Log.v(TAG, "Notify data set changed");
            }
            catch (ParseException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}
