package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.Arrays;

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
    private static final float EDIT_TEXT_WIDTH_DP = 250.0f;
    private static final float EDIT_TEXT_MARGIN_TOP_DP = 11.0f;

    private String groupNameTxt;
    private String groupMember1Txt;
    private String groupMember2Txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_group_activity);

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

    public void onAddEditTextClick(View v) {
        layout.addView(createNewEditTextView(), editTextCount);
    }

    private EditText createNewEditTextView() {
        final LayoutParams lparams = new LayoutParams(editTextWidth, LayoutParams.WRAP_CONTENT);
        lparams.setMargins(0, editTextMarginTop, 0, 0);

        final EditText editText = new EditText(this);
        editTextCount++;

        editText.setId(editTextCount);
        editText.setLayoutParams(lparams);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editText.setHint(editTextText + editTextCount);

        return editText;
    }

    public void onCreateGroupClick(View v) {
        groupNameTxt = groupName.getText().toString();
        groupMember1Txt = groupMember1.getText().toString();

        String[] memberEmails = {groupMember1Txt};
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
        } catch (ParseException e) {
            // Display error message.
            displayCreateGroupFailedMessage();
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
