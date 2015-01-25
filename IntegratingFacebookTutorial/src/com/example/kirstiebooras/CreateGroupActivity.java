package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Activity to create a new group of users.
 * An unlimited number of group members is allowed, so EditTexts for email addresses are
 * created dynamically.
 * Created by kirstiebooras on 1/20/15.
 */
public class CreateGroupActivity extends Activity {

    private LinearLayout layout;
    private EditText groupName;
    private int editTextCount;
    private int editTextWidth;
    private int editTextMarginTop;
    private String editTextText;
    private static final float EDIT_TEXT_WIDTH_DP = 328.0f;
    private static final float EDIT_TEXT_MARGIN_TOP_DP = 11.0f;
    public static final String TAG = "CreateGroupActivity";
    private List<EditText> allEditTexts = new ArrayList<EditText>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_group_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        final float scale = getResources().getDisplayMetrics().density;
        editTextWidth = (int) (EDIT_TEXT_WIDTH_DP * scale + 0.5f);
        editTextMarginTop = (int) (EDIT_TEXT_MARGIN_TOP_DP * scale + 0.5f);
        editTextText = getResources().getString(R.string.email) + " ";

        layout = (LinearLayout) findViewById(R.id.layout);
        groupName = (EditText) findViewById(R.id.groupName);
        allEditTexts.add((EditText) findViewById(R.id.email1));

        editTextCount = 1;
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
        layout.addView(createNewEditTextView(), editTextCount + 1);
    }

    private EditText createNewEditTextView() {
        // TODO add ability to remove a text view
        final LayoutParams lparams = new LayoutParams(editTextWidth, LayoutParams.WRAP_CONTENT);
        lparams.setMargins(0, editTextMarginTop, 0, 0);
        lparams.gravity = Gravity.CENTER_HORIZONTAL;

        final EditText editText = new EditText(this);
        Log.v(TAG, "Created EditText " + editTextCount);
        editTextCount++;

        editText.setLayoutParams(lparams);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint(editTextText + editTextCount);

        allEditTexts.add(editText);
        return editText;
    }

    public void onCreateGroupClick(View v) {
        String groupNameTxt = groupName.getText().toString();

        // Create an array of all emails added in the EditTexts + the current user.
        String[] memberEmails = new String[allEditTexts.size() + 1];
        memberEmails[0] = ParseUser.getCurrentUser().getEmail();

        // Check that each email is valid and add to the array of members
        String email;
        for(int i=0; i < allEditTexts.size(); i++){
            email = allEditTexts.get(i).getText().toString();
            if (!isValidEmail(email)){
                // Display invalid email message
                displayInvalidEmailMessage(i+1);
                return;
            } else {
                memberEmails[i+1] = email;
            }
        }

        // Check for duplicate emails.
        HashSet<String> set = new HashSet<String>(Arrays.asList(memberEmails));
        if (set.size() < memberEmails.length) {
            // Display repeated email message
            displayRepeatedEmailMessage();
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
                .setTitle("Invalid Email")
                .setMessage("Email " + emailNumber + " is invalid. Correct this and try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    private void displayCreateGroupFailedMessage () {
        new AlertDialog.Builder(this)
                .setTitle("Create Group Failed")
                .setMessage("Create group failed. Please try again later.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    private void displayRepeatedEmailMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Email ")
                .setMessage("Hmm, you seem to have a duplicate email. Correct this and try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }
}
