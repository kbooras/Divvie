package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

    private Resources mResources;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

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

        TextView group = (TextView) findViewById(R.id.group);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);
        TextView transactionStatus = (TextView) findViewById(R.id.transactionStatus);

        group.setText(String.format(mResources.getString(R.string.transaction_group_owes_you),
                object.getString("groupName")));
        transactionAmount.setText(symbol + object.getString("totalAmount"));
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
        // Cycle through the members array and paid array
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) object.get("members");
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) object.get("paid");

        for (Integer p : paid) {
            if (p == 0) {
            }
        }
    }
}
