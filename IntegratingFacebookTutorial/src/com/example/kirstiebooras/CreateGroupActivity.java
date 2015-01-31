package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.Arrays;
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

    private ScrollView mScrollView;
    private LinearLayout mLayout;
    private EditText mGroupName;
    private int mEmailViewCount;
    private HashMap<Integer, EditText> mAllEditTexts = new HashMap<Integer, EditText>();
    private Resources mResources;
    private static final int LAYOUT_ID_CONSTANT = 1000;
    private static final int EDIT_TEXT_ID_CONSTANT = 2000;
    private static final int BUTTON_ID_CONSTANT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_group_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mResources = getResources();

        mScrollView = (ScrollView) findViewById(R.id.scroll);
        mLayout = (LinearLayout) mScrollView.findViewById(R.id.layout);
        mGroupName = (EditText) mScrollView.findViewById(R.id.groupName);
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
                Log.v(TAG, "User signed out!");
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

        final LinearLayout ll = new LinearLayout(this);
        final LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        ll.setLayoutParams(params);
        ll.setId(LAYOUT_ID_CONSTANT + mEmailViewCount);
        ll.addView(View.inflate(getBaseContext(), R.layout.create_group_email_row, null));

        EditText editText = (EditText) ll.findViewById(R.id.emailText);
        editText.setId(EDIT_TEXT_ID_CONSTANT + mEmailViewCount);
        editText.setHint(String.format(mResources.getString(R.string.create_group_email),
                mEmailViewCount));

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
        String groupNameTxt = mGroupName.getText().toString();

        // Create an array of all emails added in the EditTexts + the current user.
        String[] memberEmails = new String[mAllEditTexts.size() + 1];
        memberEmails[0] = ParseUser.getCurrentUser().getEmail();

        // Check that each email is valid and add to the array of members
        String email;
        int i = 1;
        for (EditText text : mAllEditTexts.values()) {
            email = text.getText().toString();
            if (!isValidEmail(email)){
                // Display invalid email message
                displayInvalidEmailMessage(i);
                return;
            } else {
                memberEmails[i] = email;
            }
            i++;
        }

        // Check for duplicate emails.
        HashSet<String> set = new HashSet<String>(Arrays.asList(memberEmails));
        if (set.size() < memberEmails.length) {
            // Display repeated email message
            displayDuplicateEmailMessage();
            return;
        }

        createParseObjectGroup(groupNameTxt, memberEmails);
        finish();
    }

    private void createParseObjectGroup(String name, String[] memberEmails){
        ParseObject newGroup = new ParseObject("Group");
        newGroup.put("name", name);
        newGroup.addAll("users", Arrays.asList(memberEmails));
        try {
            newGroup.save();
            Log.v(TAG, "Saved new group successfully!");
            // Update the ArrayAdapter
        } catch (ParseException e) {
            // Display error message.
            displayCreateGroupFailedMessage();
            Log.v(TAG, "Save new group failed :(");
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(CharSequence emailTxt) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches();
    }

    private void displayInvalidEmailMessage (int emailNumber) {
        new AlertDialog.Builder(this)
                .setTitle(mResources.getString(R.string.group_invalid_email_alert_title))
                .setMessage(String.format(mResources.getString(
                        R.string.group_invalid_email_alert_message), emailNumber))
                .setPositiveButton(mResources.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                }).show();
    }

    private void displayCreateGroupFailedMessage () {
        new AlertDialog.Builder(this)
                .setTitle(mResources.getString(R.string.create_group_failed_alert_title))
                .setMessage(mResources.getString(R.string.create_group_failed_alert_message))
                .setPositiveButton(mResources.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                }).show();
    }

    private void displayDuplicateEmailMessage() {
        new AlertDialog.Builder(this)
                .setTitle(mResources.getString(R.string.duplicate_email_alert_title))
                .setMessage(mResources.getString(R.string.duplicate_email_alert_message))
                .setPositiveButton(mResources.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                }).show();
    }
}
