package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private LinearLayout mBaseLayout;
    private Resources mResources;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mBaseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        mResources = getResources();
        mIntent = getIntent();

        String transactionId = mIntent.getStringExtra("parseObjectId");

        ParseObject object = ParseMethods.findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION,
                transactionId);
        if (object != null) {
            setViewText(object);
            displayMembers(object);
        }
    }

    private void setViewText(ParseObject object) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        TextView group = (TextView) findViewById(R.id.group);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);
        TextView transactionStatus = (TextView) findViewById(R.id.transactionStatus);

        group.setText(String.format(mResources.getString(R.string.transaction_group_owes_you),
                object.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setText(symbol + object.getString(Constants.TRANSACTION_TOTAL_AMOUNT));
        transactionDescription.setText(String.format(
                mResources.getString(R.string.transaction_description),
                object.getString(Constants.TRANSACTION_DESCRIPTION)));
        String status = mIntent.getStringExtra("transactionStatus");
        transactionStatus.setText(status);
        if (status.equals(mResources.getString(R.string.complete))) {
            transactionStatus.setTextColor(mResources.getColor(R.color.light_grey));
        } else {
            transactionStatus.setTextColor(mResources.getColor(R.color.pink));
        }

    }

    private void displayMembers(ParseObject object) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) object.get(Constants.TRANSACTION_PAID);
        @SuppressWarnings("unchecked")
        ArrayList<String> displayName = (ArrayList<String>) object.get(Constants.GROUP_DISPLAY_NAMES);
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) object.get(Constants.TRANSACTION_DATE_PAID);

        for (int i = 0; i < displayName.size(); i++) {
            if (displayName.get(i).equals(ParseUser.getCurrentUser()
                    .getString(Constants.USER_FULL_NAME))) {
                // Do not display the current user as part of the transaction
                continue;
            }
            View memberRow = View.inflate(this, R.layout.view_transaction_row, null);
            TextView member = (TextView) memberRow.findViewById(R.id.member);
            TextView status = (TextView) memberRow.findViewById(R.id.status);

            if (paid.get(i) == 0) {
                // If they have not paid, display what they owe
                member.setText(String.format(
                        mResources.getString(R.string.transaction_group_owes_you),
                        displayName.get(i)));
                status.setText(symbol + object.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
                status.setTextColor(mResources.getColor(R.color.pink));
            } else {
                // Otherwise, display the date paid
                member.setText(String.format(mResources.getString(R.string.person_paid_you),
                        displayName.get(i)));
                status.setText(datePaid.get(i));
                status.setTextColor(mResources.getColor(R.color.dark_grey));
            }
            mBaseLayout.addView(memberRow);
        }
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
                ParseMethods.unpinData(Constants.CLASSNAME_TRANSACTION);
                ParseMethods.unpinData(Constants.CLASSNAME_GROUP);
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
