package com.example.kirstiebooras.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.ParseTools;
import com.example.kirstiebooras.venmo.VenmoLibrary;
import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import com.example.kirstiebooras.venmo.VenmoLibrary.VenmoResponse;

import java.util.Currency;
import java.util.Locale;

/**
 * Activity for a user to pay a charge.
 * Created by kirstiebooras on 2/2/15.
 */
public class PayChargeActivity extends Activity {

    private static final int REQUEST_CODE_VENMO_APP_SWITCH = 1;
    private ParseTools mParseTools;
    private String mTransactionObjectId;
    private String mDescription;
    private String mPersonOwed;
    private String mSplitAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pay_charge_activity);

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mTransactionObjectId = getIntent().getStringExtra("parseObjectId");
        ParseObject transaction = mParseTools.findLocalParseObjectById(
                Constants.CLASSNAME_TRANSACTION, mTransactionObjectId);
        mPersonOwed = transaction.getString(Constants.TRANSACTION_PERSON_OWED);
        mSplitAmount = transaction.getString(Constants.TRANSACTION_SPLIT_AMOUNT);
        mDescription = transaction.getString(Constants.TRANSACTION_DESCRIPTION);

        TextView payAmount = (TextView) findViewById(R.id.payAmount);
        TextView payPerson = (TextView) findViewById(R.id.payPerson);

        payAmount.setText(Currency.getInstance(Locale.getDefault()).getSymbol() + mSplitAmount);
        String displayName = mParseTools.getUserDisplayName(mPersonOwed);
        if (displayName != null) {
            payPerson.setText(String.format(getString(R.string.pay_person), displayName));
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

    public void cashPaymentClick(View v) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.record_cash_payment))
                .setMessage(getString(R.string.verify_record_cash_payment))
                .setPositiveButton(getString(R.string.record),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Mark charge as paid
                                mParseTools.markChargePaid(mTransactionObjectId, true);
                                finish();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    public void venmoPaymentClick(View v) {
        if(VenmoLibrary.isVenmoInstalled(this)) {
            // Direct the user to Venmo to make this payment
            Intent venmoIntent = VenmoLibrary.openVenmoPayment(
                    getString(R.string.VENMO_APP_ID),
                    getString(R.string.app_name), mPersonOwed, mSplitAmount,
                    mDescription, "pay");
            startActivityForResult(venmoIntent, REQUEST_CODE_VENMO_APP_SWITCH);
        } else {
            // User must download Venmo to their device
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.venmo_not_installed))
                    .setMessage(getString(R.string.venmo_not_installed_message))
                    .setPositiveButton(getString(R.string.download_venmo),
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
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    /*
     * Called after user returns from Venmo.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_VENMO_APP_SWITCH: {
                if(resultCode == RESULT_OK) {
                    String signedrequest = data.getStringExtra("signedrequest");
                    if(signedrequest != null) {
                        VenmoResponse response = (new VenmoLibrary()).validateVenmoPaymentResponse(
                                signedrequest, getString(R.string.VENMO_APP_SECRET));
                        if(response.getSuccess().equals("1")) {
                            // Payment successful.
                            Toast.makeText(this, getString(R.string.venmo_success),
                                    Toast.LENGTH_LONG).show();
                            mParseTools.markChargePaid(mTransactionObjectId, false);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                    else {
                        String errorMessage = data.getStringExtra("error_message");
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.venmo_error))
                                .setMessage(errorMessage)
                                .setPositiveButton(getString(R.string.ok), null)
                                .show();
                    }
                }
                break;
            }
        }
    }

}
