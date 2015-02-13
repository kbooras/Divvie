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
import android.widget.Toast;

import com.parse.FindCallback;
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
        ParseUser personOwed = ParseUser.getCurrentUser();
        final String personOwedEmail = personOwed.getEmail();
        final String personOwedName = personOwed.getString("fullName");

        ParseObject group = (ParseObject) mSpinner.getSelectedItem();
        final String groupId = group.getObjectId();
        final String groupName = group.getString("name");

        EditText description = (EditText) findViewById(R.id.description);
        final String descriptionTxt = description.getText().toString();

        EditText amount = (EditText) findViewById(R.id.amount);
        String amountTxt = amount.getText().toString();

        if (descriptionTxt.equals("") || amountTxt == null) {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.complete_form_toast),
                    Toast.LENGTH_LONG).show();
            return;
        }

        final double amountValue = Double.valueOf(amountTxt);
        if ((amountValue % 0.01) < 0.0099) {
            Log.v(TAG, String.valueOf(amountValue % 0.01));
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.invalid_amount_toast),
                    Toast.LENGTH_LONG).show();
            return;
        }
        final String totalAmount = String.format("%.2f", amountValue);

        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo("objectId", groupId);
        groupQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> users = (ArrayList<String>) parseObject.get("users");
                    @SuppressWarnings("unchecked")
                    ArrayList<String> displayNames =
                            (ArrayList<String>) parseObject.get("displayNames");

                    double dividedAmount = amountValue / users.size();
                    BigDecimal bd = new BigDecimal(dividedAmount);
                    String splitAmount = bd.setScale(2, BigDecimal.ROUND_FLOOR).toString();

                    createTransactionParseObject(groupId, groupName, personOwedEmail, descriptionTxt,
                            totalAmount, users, displayNames, splitAmount);

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("toEmail", personOwedEmail);
                    map.put("fromName", personOwedName);
                    map.put("groupName", groupName);
                    map.put("chargeDescription", descriptionTxt);
                    map.put("amount", splitAmount);

                    for (String email : users) {
                        if (!email.equals(personOwedEmail)) {
                            // sendEmails(email, map);
                        }
                    }

                    finish();
                } else {
                    Log.v(TAG, e.toString());
                }
            }
        });
    }

    private void createTransactionParseObject(String groupId, String groupName,
                                              String personOwed, String descriptionTxt,
                                              String totalAmount, ArrayList<String> users,
                                              ArrayList<String> displayNames, String splitAmount) {
        ParseObject newTransaction = new ParseObject("Transaction");

        newTransaction.put("groupId", groupId);
        newTransaction.put("groupName", groupName);
        newTransaction.put("personOwed", personOwed);
        newTransaction.put("description", descriptionTxt);
        newTransaction.put("totalAmount", totalAmount);
        newTransaction.put("splitAmount", splitAmount);
        newTransaction.put("users", users);
        newTransaction.put("displayNames", displayNames);

        // Set paid values and date paid values. PersonOwed is set as paid.
        ArrayList<Integer> paid = new ArrayList<Integer>(users.size());
        ArrayList<String> datePaid = new ArrayList<String>(users.size());
        for (String user : users) {
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

        newTransaction.put("paid", paid);
        newTransaction.put("datePaid", datePaid);
        newTransaction.put("complete", false);
        try {
            newTransaction.save();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.v(TAG, e.toString());
        }

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
