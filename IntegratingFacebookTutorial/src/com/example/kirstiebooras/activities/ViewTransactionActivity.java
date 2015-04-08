package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
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
        mSplitAmount = symbol + object.getString(Constants.TRANSACTION_SPLIT_AMOUNT);
        mDescription = object.getString(Constants.TRANSACTION_DESCRIPTION);

        TextView group = (TextView) findViewById(R.id.group);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);

        group.setText(String.format(getString(R.string.transaction_owes_you),
                object.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setText(mSplitAmount);
        transactionDescription.setText(String.format(getString(R.string.transaction_description),
                mDescription));
    }

    @SuppressWarnings("unchecked")
    private void displayMembers(ParseObject object) {
        Log.d(TAG, "displayMembers");
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        final ArrayList<String> datePaid = (ArrayList<String>) object.get(Constants.TRANSACTION_DATE_PAID);
        final ArrayList<String> memberEmails = (ArrayList<String>) object.get(Constants.GROUP_MEMBERS);

        ParseObject group = mParseTools.findLocalParseObjectById(Constants.CLASSNAME_GROUP,
                object.getString(Constants.TRANSACTION_GROUP_ID));
        final ArrayList<String> displayNames =
                (ArrayList<String>) group.get(Constants.GROUP_DISPLAY_NAMES);

        for (int i = 0; i < displayNames.size(); i++) {
            final int index = i;
            // Do not display the current user as part of the transaction
            if (displayNames.get(i).equals(ParseUser.getCurrentUser()
                    .getString(Constants.USER_FULL_NAME))) {
                continue;
            }
            View memberRow = View.inflate(this, R.layout.view_transaction_row, null);
            TextView member = (TextView) memberRow.findViewById(R.id.member);
            final TextView status = (TextView) memberRow.findViewById(R.id.status);
            memberRow.setId(i);

            if (datePaid.get(i).equals("")) {
                // If they have not paid, display what they owe
                member.setText(String.format(getString(R.string.transaction_owes_you),
                        displayNames.get(i)));
                status.setText(symbol + object.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
                status.setTextColor(getResources().getColor(R.color.pink));
            } else if (datePaid.get(i).charAt(0) == 'p') {
                // Mark as pending
                member.setText(String.format(getString(R.string.person_paid_you),
                        displayNames.get(i)));
                status.setText(getString(R.string.transaction_pending));
                status.setTextColor(getResources().getColor(R.color.dark_grey));
                status.setTypeface(null, Typeface.ITALIC);
            } else {
                // Otherwise, display the date paid
                member.setText(String.format(getString(R.string.person_paid_you),
                        displayNames.get(i)));
                status.setText(datePaid.get(i));
                status.setTextColor(getResources().getColor(R.color.dark_grey));
            }
            memberRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String statusTxt = status.getText().toString();
                    if (statusTxt.equals(getString(R.string.transaction_pending))) {
                        createValidatePopup(displayNames.get(index), datePaid.get(index), index);
                    }
                    else if (statusTxt.equals(mSplitAmount)){
                        createRemindOverridePopup(displayNames.get(index), memberEmails.get(index), index);
                    }
                }
            });
            mBaseLayout.addView(memberRow);
        }
    }

    private void createValidatePopup(final String name, final String date, final int index) {
        String message = String.format(getString(R.string.transaction_pending_message), name);
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mParseTools.updatePendingPayment(mTransactionId, index, true);
                        updateMemberRow(index, date.substring(1));
                    }
                })
                .setNegativeButton(getString(R.string.deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mParseTools.updatePendingPayment(mTransactionId, index, false);
                        updateMemberRow(index, mSplitAmount);
                    }
                })
                .setNeutralButton(getString(R.string.cancel), null)
                .show();
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
            // TODO should get this from the object
            int memberIndex = data.getIntExtra("memberIndex", -1);
            String date = data.getStringExtra("datePaid");
            updateMemberRow(memberIndex, date);
        }
    }

    // Update member row showing the member has paid or not
    private void updateMemberRow(int index, String status) {
        View memberRow = findViewById(index);
        TextView statusView = (TextView) memberRow.findViewById(R.id.status);
        statusView.setText(status);
        if (status.equals(mSplitAmount)) {
            statusView.setTextColor(getResources().getColor(R.color.pink));
            statusView.setTypeface(null, Typeface.NORMAL);
        }
        else {
            statusView.setTextColor(getResources().getColor(R.color.dark_grey));
            statusView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
