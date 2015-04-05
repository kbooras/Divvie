package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.ParseTools;
import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

/**
 * Activity to view an incomplete transaction.
 * Created by kirstiebooras on 2/2/15.
 */
public class ViewTransactionActivity extends Activity {

    private static final String TAG = "ViewTransactionActivity";
    private static final int OVERRIDE_PAYMENT_REQUEST_CODE = 1;

    private ParseTools mParseTools;
    private LinearLayout mBaseLayout;
    private String mTransactionId;
    private String mSplitAmount;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();
        mParseTools.setSendReminderEmailListener(new ParseTools.SendReminderEmailListener() {
            @Override
            public void onReminderEmailSent(boolean sent) {
                if (sent) {
                    Toast.makeText(getApplicationContext(), R.string.sent_reminder_email,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.send_reminder_email_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mBaseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        Intent intent = getIntent();

        mTransactionId = intent.getStringExtra("parseObjectId");

        ParseObject object = mParseTools.findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION,
                mTransactionId);
        if (object != null) {
            setViewText(object);
            displayMembers(object);
        }
    }

    private void setViewText(ParseObject object) {
        Log.d(TAG, "setViewText");
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        mSplitAmount = symbol + object.getString(Constants.TRANSACTION_TOTAL_AMOUNT);
        mDescription = object.getString(Constants.TRANSACTION_DESCRIPTION);

        TextView group = (TextView) findViewById(R.id.group);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);

        group.setText(String.format(getString(R.string.transaction_group_owes_you),
                object.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setText(mSplitAmount);
        transactionDescription.setText(String.format(getString(R.string.transaction_description),
                mDescription));
    }

    @SuppressWarnings("unchecked")
    private void displayMembers(ParseObject object) {
        Log.d(TAG, "displayMembers");
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        ArrayList<String> datePaid = (ArrayList<String>) object.get(Constants.TRANSACTION_DATE_PAID);
        final ArrayList<String> memberEmails = (ArrayList<String>) object.get(Constants.GROUP_MEMBERS);

        ParseObject group = mParseTools.findLocalParseObjectById(Constants.CLASSNAME_GROUP,
                object.getString(Constants.TRANSACTION_GROUP_ID));
        final ArrayList<String> displayNames =
                (ArrayList<String>) group.get(Constants.GROUP_DISPLAY_NAMES);

        for (int i = 0; i < displayNames.size(); i++) {
            final int index = i;
            if (displayNames.get(i).equals(ParseUser.getCurrentUser()
                    .getString(Constants.USER_FULL_NAME))) {
                // Do not display the current user as part of the transaction
                continue;
            }
            View memberRow = View.inflate(this, R.layout.view_transaction_row, null);
            memberRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createRemindOverridePopup(displayNames.get(index), memberEmails.get(index), index);
                }
            });
            memberRow.setId(i);
            TextView member = (TextView) memberRow.findViewById(R.id.member);
            TextView status = (TextView) memberRow.findViewById(R.id.status);

            if (datePaid.get(i).equals("")) {
                // If they have not paid, display what they owe
                member.setText(String.format(getString(R.string.transaction_group_owes_you),
                        displayNames.get(i)));
                status.setText(symbol + object.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
                status.setTextColor(getResources().getColor(R.color.pink));
            } else {
                // Otherwise, display the date paid
                member.setText(String.format(getString(R.string.person_paid_you),
                        displayNames.get(i)));
                status.setText(datePaid.get(i));
                status.setTextColor(getResources().getColor(R.color.dark_grey));
            }
            mBaseLayout.addView(memberRow);
        }
    }

    private void createRemindOverridePopup(final String name, final String email, final int index) {
        String message = String.format(getString(R.string.person_owes_you), name, mSplitAmount);
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.override), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(),
                                OverridePaymentActivity.class);
                        intent.putExtra("memberName", name);
                        intent.putExtra("memberIndex", index);
                        intent.putExtra("splitAmount", mSplitAmount);
                        intent.putExtra("description", mDescription);
                        intent.putExtra("transactionId", mTransactionId);
                        startActivityForResult(intent, OVERRIDE_PAYMENT_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(R.string.remind), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mParseTools.sendReminderEmail(email, name, mDescription,
                                ParseUser.getCurrentUser().getString(Constants.USER_FULL_NAME));
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == OVERRIDE_PAYMENT_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult");
            int memberIndex = data.getIntExtra("memberIndex", -1);
            String date = data.getStringExtra("datePaid");
            updateMemberRow(memberIndex, date);
        }
    }

    // Update member row showing the member has paid
    private void updateMemberRow(int index, String date) {
        View memberRow = findViewById(index);
        TextView status = (TextView) memberRow.findViewById(R.id.status);
        status.setText(date);
        status.setTextColor(getResources().getColor(R.color.dark_grey));
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
}
