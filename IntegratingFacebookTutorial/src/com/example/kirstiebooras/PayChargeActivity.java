package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Activity for a user to pay a charge.
 * Created by kirstiebooras on 2/2/15.
 */
public class PayChargeActivity extends Activity {

    private static final String TAG = "PayChargeActivity";
    private String mTransactionObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pay_charge_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mTransactionObjectId = getIntent().getStringExtra("parseObjectId");

        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Transaction");
        groupQuery.whereEqualTo("objectId", mTransactionObjectId);
        groupQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Should fill in the text in the view
                ParseObject object = parseObjects.get(0);
                setViewText(object.getString("personOwed"),
                        object.getNumber("splitAmount").doubleValue());
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

    public void cashPaymentClick(View v) {
        Resources res = getResources();
        new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.record_cash_payment))
                .setMessage(res.getString(R.string.verify_record_cash_payment))
                .setPositiveButton(res.getString(R.string.record),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Mark charge as paid
                                markChargePaid();
                                finish();
                            }
                        })
                .setNegativeButton(res.getString(R.string.cancel), null)
                .show();
    }

    private void markChargePaid() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");

        // Retrieve the object by id
        query.getInBackground(mTransactionObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> members = (ArrayList<String>) parseObject.get("members");
                    @SuppressWarnings("unchecked")
                    ArrayList<Integer> paid = (ArrayList<Integer>) parseObject.get("paid");

                    // Mark this person as paid
                    String currentUser = ParseUser.getCurrentUser().getEmail();
                    boolean complete = true;
                    int index;
                    for (index = 0; index < members.size(); index++) {
                        if (members.get(index).equals(currentUser)) {
                            paid.set(index, 1);
                            break;
                        }
                        if (paid.get(index) == 0) {
                            complete = false;
                        }
                    }

                    parseObject.put("paid", paid);
                    if (complete) {
                        parseObject.put("complete", true);
                        Log.v(TAG, "Charge completed");
                    }
                    parseObject.saveInBackground();
                    Log.v(TAG, "Mark charge as paid");
                }
            }
        });

    }
}
