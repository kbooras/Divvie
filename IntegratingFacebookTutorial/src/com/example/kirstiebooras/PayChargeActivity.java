package com.example.kirstiebooras;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Activity for a user to pay a charge.
 * Created by kirstiebooras on 2/2/15.
 */
public class PayChargeActivity extends Activity {

    private static final String TAG = "PayChargeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pay_charge_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Transaction");
        groupQuery.whereEqualTo("objectId", getIntent().getStringExtra("parseObjectId"));
        groupQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Should fill in the text in the view
                ParseObject transaction = parseObjects.get(0);
                setViewText(transaction.getString("personOwed"),
                        transaction.getNumber("splitAmount").doubleValue());
            }
        });
    }

    private void setViewText(final String personOwed, final double splitAmount) {
        final TextView payPerson = (TextView) findViewById(R.id.payPerson);
        final TextView payAmount = (TextView) findViewById(R.id.payAmount);

        ParseQuery<ParseUser> groupQuery = ParseUser.getQuery();
        groupQuery.whereEqualTo("email", personOwed);
        groupQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                // Should fill in the text in the view
                ParseObject email = results.get(0);
                payPerson.setText(String.format(getResources().getString(R.string.pay_person),
                        email.getString("fullName")));
                payAmount.setText(Currency.getInstance(Locale.getDefault()).getSymbol() + splitAmount);
            }
        });
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
                Log.v(TAG, "User signed out!");
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
