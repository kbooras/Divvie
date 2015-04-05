package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.ParseTools;
import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Activity to create a new group of users.
 * An unlimited number of group members is allowed, so EditTexts for email addresses are
 * created dynamically.
 * Created by kirstiebooras on 1/20/15.
 */
public class CreateGroupActivity extends Activity {

    private static final String TAG = "CreateGroupActivity";

    private ParseTools mParseTools;
    private LinearLayout mLayout;
    private EditText mGroupName;
    private int mEmailViewCount;
    private HashMap<Integer, EditText> mAllEditTexts;
    private static final int LAYOUT_ID_CONSTANT = 1000;
    private static final int EDIT_TEXT_ID_CONSTANT = 2000;
    private static final int BUTTON_ID_CONSTANT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_group_activity);

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ScrollView mScrollView = (ScrollView) findViewById(R.id.scroll);
        mLayout = (LinearLayout) mScrollView.findViewById(R.id.layout);
        mGroupName = (EditText) mScrollView.findViewById(R.id.groupName);
        mAllEditTexts = new HashMap<Integer, EditText>();
        mAllEditTexts.put(1, (EditText) findViewById(R.id.email1));

        mEmailViewCount = 1;
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

    public void onAddEditTextClick(View v) {
        // Dynamically adds a new EditText below the last EditText.
        // +1 because starts at index 0 and the TextView is above all EditTexts
        mLayout.addView(createNewEmailView(), mEmailViewCount + 1);
    }

    private LinearLayout createNewEmailView() {
        // Create FrameLayout which holds EditText and delete button
        mEmailViewCount++;
        Log.v(TAG, "Create email view " + mEmailViewCount);

        LinearLayout ll = new LinearLayout(this);
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        ll.setLayoutParams(params);
        ll.setId(LAYOUT_ID_CONSTANT + mEmailViewCount);
        ll.addView(View.inflate(getBaseContext(), R.layout.create_group_email_row, null));

        EditText editText = (EditText) ll.findViewById(R.id.emailText);
        editText.setId(EDIT_TEXT_ID_CONSTANT + mEmailViewCount);
        editText.setHint(String.format(getString(R.string.create_group_email), mEmailViewCount));

        mAllEditTexts.put(mEmailViewCount, editText);

        Button button = (Button) ll.findViewById(R.id.deleteButton);
        button.setId(BUTTON_ID_CONSTANT + mEmailViewCount);

        return ll;
    }

    public void onDeleteClick(View view) {
        int emailRow = view.getId() - BUTTON_ID_CONSTANT;
        Log.v(TAG, "Delete email row " + emailRow);
        LinearLayout ll = (LinearLayout) findViewById(LAYOUT_ID_CONSTANT + emailRow);
        ll.setVisibility(View.GONE);
        // Delete the EditText from the hashmap
        mAllEditTexts.remove(emailRow);
        // TODO: Adjust the numbers of the other EditTexts
    }

    public void onCreateGroupClick(View v) {
        if (!isNetworkConnected()) {
            Log.i(TAG, "Cannot create Group. Not connected to internet.");
            displayNoNetworkConnectionMessage();
            return;
        }
        String groupNameTxt = mGroupName.getText().toString();
        ParseUser current = ParseUser.getCurrentUser();

        // Create arrays of all emails and names added in the EditTexts + the current user.
        ArrayList<String> memberEmails = validateEmails(mAllEditTexts.values());
        if (memberEmails == null) {
            // There is an invalid email
            return;
        }
        memberEmails.add(current.getEmail());

        mParseTools.createGroupParseObject(groupNameTxt, memberEmails);

        finish();
    }

    /*
     * Checks for network connection and returns true if connected.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /*
     * Validate all the emails from the EditTexts and check for duplicates.
     * @param emailTexts: The collection of EditTexts containing the member emails.
     * @return the ArrayList of memberEmails
     */
    private ArrayList<String> validateEmails(Collection<EditText> emailTexts) {
        ArrayList<String> memberEmails = new ArrayList<String>(emailTexts.size());
        int i = 1;
        for (EditText text :emailTexts) {
            String email = text.getText().toString().toLowerCase();
            if (!isValidEmail(email)){
                displayInvalidEmailMessage(i);
                return null;
            } else {
                memberEmails.add(email);
            }
            i++;
        }

        // Check for duplicate emails.
        HashSet<String> set = new HashSet<String>(memberEmails);
        if (set.size() < memberEmails.size()) {
            // Display repeated email message
            displayDuplicateEmailMessage();
            return null;
        }

        return memberEmails;
    }

    private boolean isValidEmail(CharSequence emailTxt) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches();
    }

    private void displayNoNetworkConnectionMessage() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.no_network_connection))
                .setMessage(getString(R.string.network_connection_create_group_alert_message))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void displayInvalidEmailMessage (int emailNumber) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.group_invalid_email_alert_title))
                .setMessage(String.format(getString(
                        R.string.group_invalid_email_alert_message), emailNumber))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    private void displayDuplicateEmailMessage() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.duplicate_email_alert_title))
                .setMessage(getString(R.string.duplicate_email_alert_message))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }
}