package com.example.kirstiebooras;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.parse.integratingfacebooktutorial.R;

import java.util.Currency;
import java.util.Locale;

/**
 * Activity to view an incomplete transaction.
 * Created by kirstiebooras on 2/2/15.
 */
public class ViewTransactionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_transaction_activity);

        Resources res = getResources();
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        Intent intent = getIntent();

        TextView group = (TextView) findViewById(R.id.group);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);
        TextView transactionStatus = (TextView) findViewById(R.id.transactionStatus);

        group.setText(String.format(res.getString(R.string.transaction_group_owes_you),
                intent.getStringExtra("group")));
        transactionAmount.setText(symbol + intent.getStringExtra("amount"));
        transactionDescription.setText(String.format(res.getString(R.string.transaction_description),
                intent.getStringExtra("description")));
        if (intent.hasExtra("owed")) {
            transactionStatus.setText(String.format(res.getString(R.string.transaction_amount_owed),
                    symbol, intent.getStringExtra("owed")));
            transactionStatus.setTextColor(Color.parseColor("#5C5C5C"));
        } else {
            transactionStatus.setText(res.getString(R.string.complete));
            transactionStatus.setTextColor(Color.parseColor("#3B3B3B"));
        }

    }
}
