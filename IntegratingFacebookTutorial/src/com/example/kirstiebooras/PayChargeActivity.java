package com.example.kirstiebooras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
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

import com.example.kirstiebooras.VenmoLibrary.VenmoResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Activity for a user to pay a charge.
 * Created by kirstiebooras on 2/2/15.
 */
public class PayChargeActivity extends Activity {

    private static final String TAG = "PayChargeActivity";
    private static final int REQUEST_CODE_VENMO_APP_SWITCH = 1;
    private String mTransactionObjectId;
    private String mDescription;
    private String mPersonOwed;
    private String mSplitAmount;
    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        mResources = getResources();

        mTransactionObjectId = getIntent().getStringExtra("parseObjectId");

        ParseQuery<ParseObject> transactionQuery = ParseQuery.getQuery("Transaction");
        transactionQuery.whereEqualTo(Constants.OBJECT_ID, mTransactionObjectId);
        transactionQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                // Should fill in the text in the view
                setContentView(R.layout.pay_charge_activity);
                ParseObject object = parseObjects.get(0);
                mPersonOwed = object.getString(Constants.TRANSACTION_PERSON_OWED);
                mSplitAmount = object.getString(Constants.TRANSACTION_SPLIT_AMOUNT);
                mDescription = object.getString(Constants.TRANSACTION_DESCRIPTION);
                setViewText();
            }
        });
    }

    private void setViewText() {
        final TextView payPerson = (TextView) findViewById(R.id.payPerson);
        final TextView payAmount = (TextView) findViewById(R.id.payAmount);

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo(Constants.USER_EMAIL, mPersonOwed);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                // Should fill in the text in the view
                ParseObject email = results.get(0);
                payPerson.setText(String.format(getResources().getString(R.string.pay_person),
                        email.getString(Constants.USER_FULL_NAME)));
                payAmount.setText(Currency.getInstance(Locale.getDefault()).getSymbol() + mSplitAmount);
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
        new AlertDialog.Builder(this)
                .setTitle(mResources.getString(R.string.record_cash_payment))
                .setMessage(mResources.getString(R.string.verify_record_cash_payment))
                .setPositiveButton(mResources.getString(R.string.record),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Mark charge as paid
                                markChargePaid();
                                finish();
                            }
                        })
                .setNegativeButton(mResources.getString(R.string.cancel), null)
                .show();
    }

    private void markChargePaid() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Transaction");

        query.getInBackground(mTransactionObjectId, new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> members =
                            (ArrayList<String>) parseObject.get(Constants.GROUP_MEMBERS);
                    @SuppressWarnings("unchecked")
                    ArrayList<Integer> paid =
                            (ArrayList<Integer>) parseObject.get(Constants.TRANSACTION_PAID);
                    @SuppressWarnings("unchecked")
                    ArrayList<String> datePaid =
                            (ArrayList<String>) parseObject.get(Constants.TRANSACTION_DATE_PAID);

                    String currentUser = ParseUser.getCurrentUser().getEmail();
                    boolean complete = true;
                    for (int i = 0; i < members.size(); i++) {
                        if (members.get(i).equals(currentUser)) {
                            // Mark this person as paid and set their date paid
                            paid.set(i, 1);
                            String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
                            String date = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
                            datePaid.set(i,month + "/" + date);
                        }
                        if (paid.get(i) == 0) {
                            // Check if this transaction is complete or not
                            complete = false;
                        }
                    }

                    parseObject.put(Constants.TRANSACTION_PAID, paid);
                    parseObject.put(Constants.TRANSACTION_DATE_PAID, datePaid);
                    if (complete) {
                        parseObject.put(Constants.TRANSACTION_COMPLETE, true);
                        Log.v(TAG, "Charge completed");
                    }
                    parseObject.saveInBackground();
                    Log.v(TAG, "Mark charge as paid");
                }
            }
        });

    }

    public void venmoPaymentClick(View v) {
        // TODO: user's enter venmo id, email, or phone number preferred
        if(VenmoLibrary.isVenmoInstalled(this)) {
            // Direct the user to Venmo to make this payment
            Intent venmoIntent = VenmoLibrary.openVenmoPayment(
                    mResources.getString(R.string.VENMO_APP_ID),
                    mResources.getString(R.string.app_name), mPersonOwed, mSplitAmount,
                    mDescription, "pay");
            startActivityForResult(venmoIntent, REQUEST_CODE_VENMO_APP_SWITCH);
        } else {
            // User must download Venmo to their device
            new AlertDialog.Builder(this)
                    .setTitle(mResources.getString(R.string.venmo_not_installed))
                    .setMessage(mResources.getString(R.string.venmo_not_installed_message))
                    .setPositiveButton(mResources.getString(R.string.download_venmo),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                "market://details?id=com.venmo")));
                                    } catch (android.content.ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                "http://play.google.com/store/apps/details?id=com.venmo")));
                                    }
                                }
                            })
                    .setNegativeButton(mResources.getString(R.string.cancel), null)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto generated from Venmo Android SDK
        switch(requestCode) {
            case REQUEST_CODE_VENMO_APP_SWITCH: {
                if(resultCode == RESULT_OK) {
                    String signedrequest = data.getStringExtra("signedrequest");
                    if(signedrequest != null) {
                        VenmoResponse response = (new VenmoLibrary()).validateVenmoPaymentResponse(
                                signedrequest, mResources.getString(R.string.VENMO_APP_SECRET));
                        if(response.getSuccess().equals("1")) {
                            // Payment successful.
                            // Use data from response object to display a success message
                            String note = response.getNote();
                            String amount = response.getAmount();
                        }
                    }
                    else {
                        String error_message = data.getStringExtra("error_message");
                        // An error ocurred.  Make sure to display the error_message to the user
                    }
                }
                else if(resultCode == RESULT_CANCELED) {
                    //The user cancelled the payment
                }
                break;
            }
        }
    }
}
