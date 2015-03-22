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

    private ParseTools mParseTools;
    private LinearLayout mBaseLayout;
    private Resources mResources;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mBaseLayout = (LinearLayout) findViewById(R.id.baseLayout);
        mResources = getResources();
        mIntent = getIntent();

        String transactionId = mIntent.getStringExtra("parseObjectId");

        ParseObject object = mParseTools.findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION,
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

        group.setText(String.format(mResources.getString(R.string.transaction_group_owes_you),
                object.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setText(symbol + object.getString(Constants.TRANSACTION_TOTAL_AMOUNT));
        transactionDescription.setText(String.format(
                mResources.getString(R.string.transaction_description),
                object.getString(Constants.TRANSACTION_DESCRIPTION)));
    }

    private void displayMembers(ParseObject object) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) object.get(Constants.TRANSACTION_PAID);
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) object.get(Constants.TRANSACTION_DATE_PAID);

        ParseObject group = mParseTools.findLocalParseObjectById(Constants.CLASSNAME_GROUP,
                object.getString(Constants.TRANSACTION_GROUP_ID));
        @SuppressWarnings("unchecked")
        ArrayList<String> displayNames = (ArrayList<String>) group.get(Constants.GROUP_DISPLAY_NAMES);

        for (int i = 0; i < displayNames.size(); i++) {
            if (displayNames.get(i).equals(ParseUser.getCurrentUser()
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
                        displayNames.get(i)));
                status.setText(symbol + object.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
                status.setTextColor(mResources.getColor(R.color.pink));
            } else {
                // Otherwise, display the date paid
                member.setText(String.format(mResources.getString(R.string.person_paid_you),
                        displayNames.get(i)));
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
