package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Activity to view an incomplete transaction.
 * Created by kirstiebooras on 2/2/15.
 */
public class ViewTransactionActivity extends Activity {

    private LinearLayout mBaseLayout;
    private Resources mResources;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

        mBaseLayout = (LinearLayout) findViewById(R.id.baseLayout);

        mResources = getResources();
        mIntent = getIntent();

        String transactionId = mIntent.getStringExtra("parseObjectId");

        ParseQuery<ParseObject> transactionQuery = ParseQuery.getQuery("Transaction");
        transactionQuery.whereEqualTo("objectId", transactionId);
        transactionQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                setViewText(parseObjects.get(0));
                displayMembers(parseObjects.get(0));
            }
        });
    }

    private void setViewText(ParseObject object) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        LinearLayout layout1 = (LinearLayout) mBaseLayout.findViewById(R.id.layout1);
        LinearLayout layout2 = (LinearLayout) mBaseLayout.findViewById(R.id.layout2);

        TextView group = (TextView) layout1.findViewById(R.id.group);
        TextView transactionAmount = (TextView) layout1.findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) layout2.findViewById(R.id.transactionDescription);
        // TODO This changes
        TextView transactionStatus = (TextView) layout2.findViewById(R.id.transactionStatus);

        group.setText(String.format(mResources.getString(R.string.transaction_group_owes_you),
                object.getString("groupName")));
        transactionAmount.setText(symbol + object.getNumber("totalAmount").toString());
        transactionDescription.setText(String.format(
                mResources.getString(R.string.transaction_description),
                object.getString("description")));
        String status = mIntent.getStringExtra("transactionStatus");
        transactionStatus.setText(status);
        if (status.equals(mResources.getString(R.string.complete))) {
            transactionStatus.setTextColor(Color.parseColor("#5C5C5C"));
        } else {
            transactionStatus.setTextColor(Color.parseColor("#3B3B3B"));
        }

    }

    private void displayMembers(ParseObject object) {
        // Do not display yourself
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) object.get("paid");
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) object.get("members");
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) object.get("datePaid");

        for (int i = 0; i < paid.size(); i++) {
            View memberRow = View.inflate(this, R.layout.view_transaction_row, null);
            TextView member = (TextView) memberRow.findViewById(R.id.member);
            TextView status = (TextView) memberRow.findViewById(R.id.status);

            if (paid.get(i) == 0) {
                // If they have not paid, display what they owe
                member.setText(String.format(
                        mResources.getString(R.string.transaction_group_owes_you), members.get(i)));
                status.setText(symbol + object.getString("splitAmount"));
                status.setTextColor(Color.parseColor("#3B3B3B"));
            } else {
                // Otherwise, display the date paid
                member.setText(String.format(
                        mResources.getString(R.string.person_paid_you), members.get(i)));
                status.setText(datePaid.get(i));
                status.setTextColor(Color.parseColor("#3B3B3B"));
            }
            mBaseLayout.addView(memberRow);
        }
    }
}
