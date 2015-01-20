package com.example.kirstiebooras;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import com.parse.integratingfacebooktutorial.R;

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
}
