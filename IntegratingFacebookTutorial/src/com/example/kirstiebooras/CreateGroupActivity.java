package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.Arrays;

/**
 * Activity to create a new group of users.
 * Created by kirstiebooras on 1/20/15.
 */
public class CreateGroupActivity extends Activity {

    private EditText groupName;
    private EditText member1;
    private EditText member2;
    private EditText member3;
    private EditText member4;
    private String groupNameTxt;
    private String member1Txt;
    private String member2Txt;
    private String member3Txt;
    private String member4Txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_group_activity);

        groupName = (EditText) findViewById(R.id.groupName);
        member1 = (EditText) findViewById(R.id.member1);
        member2 = (EditText) findViewById(R.id.member2);
        member3 = (EditText) findViewById(R.id.member3);
        member4 = (EditText) findViewById(R.id.member4);

    }

    public void onCreateGroupClick(View v) {
        groupNameTxt = groupName.getText().toString();
        member1Txt = member1.getText().toString();
        member2Txt = member2.getText().toString();
        member3Txt = member3.getText().toString();
        member4Txt = member4.getText().toString();
        String[] memberEmails = {member1Txt, member2Txt, member3Txt, member4Txt};
        for (int i = 0 ; i < memberEmails.length; i++) {
            if(!isValidEmail(memberEmails[i])){
                //Emails not valid message
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
