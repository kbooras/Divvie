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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Arrays;
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
    private EditText groupMember1;
    private EditText groupMember2;
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
        groupMember1 = (EditText) findViewById(R.id.email1);
        groupMember2 = (EditText) findViewById(R.id.email2);

        editTextCount = 2;
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
                //startSigninRegisterActivity();
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
        final LayoutParams lparams = new LayoutParams(editTextWidth, LayoutParams.WRAP_CONTENT);
        lparams.setMargins(0, editTextMarginTop, 0, 0);
        lparams.setLayoutDirection(Gravity.CENTER_HORIZONTAL);

        final EditText editText = new EditText(this);
        Log.v(TAG, "Created EditText " + editTextCount);
        editTextCount++;

        editText.setId(editTextCount);
        editText.setLayoutParams(lparams);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint(editTextText + editTextCount);

        allEditTexts.add(editText);
        return editText;
    }

    public void onCreateGroupClick(View v) {
        String groupNameTxt = groupName.getText().toString();
        String groupMember1Txt = groupMember1.getText().toString();
        String groupMember2Txt = groupMember2.getText().toString();

        // Create an array of all emails added in the EditTexts.
        String[] memberEmails = new String[allEditTexts.size() + 2];
        memberEmails[0] = groupMember1Txt;
        memberEmails[1] = groupMember2Txt;
        for(int i=0; i < allEditTexts.size(); i++){
            memberEmails[i+2] = allEditTexts.get(i).getText().toString();
        }

        // Check each email is valid.
        for (int i = 0 ; i < memberEmails.length; i++) {
            if(!isValidEmail(memberEmails[i])){
                // Display invalid email message
                displayInvalidEmailMessage(i+1);
                return;
            }
        }

        createParseObjectGroup(groupNameTxt, memberEmails);
        finish();
    }

    public void createParseObjectGroup(String name, String[] memberEmails){
        ParseObject newGroup = new ParseObject("Group");
        newGroup.put("name", name);
        newGroup.addAll("users", Arrays.asList(memberEmails));
        try {
            newGroup.save();
            Log.v(TAG, "Saved new group successfully!");
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

    public void displayInvalidEmailMessage (int emailNumber) {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Email")
                .setMessage("Email " + emailNumber + " is invalid. Correct this and try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    public void displayCreateGroupFailedMessage () {
        new AlertDialog.Builder(this)
                .setTitle("Create Group Failed")
                .setMessage("Create group failed. Please try again later.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }
}
