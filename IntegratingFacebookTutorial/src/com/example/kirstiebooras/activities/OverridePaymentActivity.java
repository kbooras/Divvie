package com.example.kirstiebooras.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kirstiebooras.fragments.DatePickerFragment;
import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.ParseTools;
import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.integratingfacebooktutorial.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Activity to override a payment and mark as paid.
 * Created by kirstiebooras on 3/22/15.
 */
public class OverridePaymentActivity extends FragmentActivity {

    private static final String TAG = "OverridePaymentActivity";
    private Button mDatePaidButton;
    private String mTransactionId;
    private int mMemberIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.override_payment_activity);

        mDatePaidButton = (Button) findViewById(R.id.dateDropDown);

        Intent intent = getIntent();
        String name = intent.getStringExtra("memberName");
        String amount = intent.getStringExtra("splitAmount");
        String description = intent.getStringExtra("description");
        mTransactionId = intent.getStringExtra("transactionId");
        mMemberIndex = intent.getIntExtra("memberIndex", -1);

        setViewText(name, amount, description);
    }

    private void setViewText(String name, String amount, String description) {
        TextView member = (TextView) findViewById(R.id.member);
        TextView transactionAmount = (TextView) findViewById(R.id.transactionAmount);
        TextView transactionDescription = (TextView) findViewById(R.id.transactionDescription);

        member.setText(String.format(getString(R.string.person_paid_you), name));
        transactionAmount.setText(amount);
        transactionDescription
                .setText(String.format(getString(R.string.for_description), description));

        // Default set to today
        Button datePaid = (Button) findViewById(R.id.dateDropDown);
        Date date = new Date(System.currentTimeMillis());
        String today = new SimpleDateFormat("MM/dd/yy").format(date);
        datePaid.setText(today);
    }

    public void onSetDatePaidButtonClick(View view) {
        // Inflate view to select date
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setDatePaidButtonText(String datePaid) {
        mDatePaidButton.setText(datePaid);
    }

    @SuppressWarnings("unchecked")
    public void onConfirmClick(View view) {
        ParseTools parseTools = ((DivvieApplication) getApplication()).getParseTools();
        final ParseObject object =
                parseTools.findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION, mTransactionId);

        if(object != null) {
            ArrayList<Integer> paid = (ArrayList<Integer>) object.get(Constants.TRANSACTION_PAID);
            ArrayList<String> datePaid = (ArrayList<String>) object.get(Constants.TRANSACTION_DATE_PAID);

            if (mMemberIndex == -1) {
                return;
            }
            paid.set(mMemberIndex, 1);

            final String datePaidTxt = mDatePaidButton.getText().toString();
            datePaid.set(mMemberIndex, datePaidTxt);

            // Update in parse server
            object.put(Constants.TRANSACTION_PAID, paid);
            object.put(Constants.TRANSACTION_DATE_PAID, datePaid);
            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "Payment overridden successfully!");
                        // Update local object
                        Intent data = new Intent();
                        data.putExtra("memberIndex", mMemberIndex);
                        data.putExtra("datePaid", datePaidTxt);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    else {
                        Log.e(TAG, "Payment override error: " + e.getMessage());
                    }
                }
            });
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
                ParseTools parseTools = ((DivvieApplication) getApplication()).getParseTools();
                parseTools.unpinData(Constants.CLASSNAME_TRANSACTION);
                parseTools.unpinData(Constants.CLASSNAME_GROUP);
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
        intent.putExtra("updateTransaction", mTransactionId);
        startActivity(intent);
    }

}
