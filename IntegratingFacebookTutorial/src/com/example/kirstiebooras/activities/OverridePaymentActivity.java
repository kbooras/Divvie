package com.example.kirstiebooras.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.kirstiebooras.fragments.DatePickerFragment;
import com.example.kirstiebooras.DivvieApplication;
import com.example.kirstiebooras.helpers.ParseTools;
import com.parse.integratingfacebooktutorial.R;

import java.text.SimpleDateFormat;
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

        getActionBar().setDisplayHomeAsUpEnabled(true);

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
        String today = new SimpleDateFormat("M/d/yy").format(date);
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
        String datePaid = mDatePaidButton.getText().toString();
        parseTools.markChargePaid(mTransactionId, false, mMemberIndex, datePaid);

        // Notify ViewTransactionActivity that it should update
        Intent data = new Intent();
        data.putExtra("memberIndex", mMemberIndex);
        data.putExtra("datePaid", datePaid);
        setResult(RESULT_OK, data);
        finish();
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
}
