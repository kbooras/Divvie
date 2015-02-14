package com.example.kirstiebooras;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

/**
 * Custom ArrayAdapter to load a user's transaction info into the transactions fragment.
 * Created by kirstiebooras on 1/31/15.
 */
public class TransactionsAdapter extends ArrayAdapter<ParseObject> {

    private static final String TAG = "TransactionsAdapter";

    private final ArrayList<ParseObject> mTransactions;
    private Resources mResources;
    private String mSymbol;

    public TransactionsAdapter(Context context, ArrayList<ParseObject> transactions) {
        super(context, R.layout.transactions_fragment_row, transactions);
        mTransactions = transactions;
        mResources = context.getResources();
        mSymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.transactions_fragment_row, parent, false);
        } else {
            rowView = convertView;
        }

        TextView transactionGroup = (TextView) rowView.findViewById(R.id.transactionGroup);
        TextView transactionDescription = (TextView) rowView.findViewById(R.id.transactionDescription);
        TextView transactionAmount = (TextView) rowView.findViewById(R.id.transactionAmount);
        TextView transactionStatus = (TextView) rowView.findViewById(R.id.transactionStatus);

        // We retrieve the object from the list
        ParseObject transaction = mTransactions.get(position);
        if (transaction != null) {
            if (!transaction.getString(Constants.TRANSACTION_PERSON_OWED)
                    .equals(ParseUser.getCurrentUser().getEmail())) {
                // User owes the group
                setUserOwesGroupTexts(transaction, transactionGroup, transactionAmount);
                setPaidStatus(transactionAmount, transactionStatus, transaction);
                Log.v(TAG, "User owes the group");
            } else {
                // The group owes user
                setGroupOwesUserTexts(transaction, transactionGroup, transactionAmount);
                setAmountStillOwed(transactionStatus, transaction);
                Log.v(TAG, "The group owes the user");
            }

            transactionDescription.setText(String.format(
                    mResources.getString(R.string.transaction_description),
                    transaction.getString(Constants.TRANSACTION_DESCRIPTION)));

        }

        return rowView;
    }

    private void setUserOwesGroupTexts(ParseObject transaction, TextView transactionGroup,
                                       TextView transactionAmount) {
        transactionGroup.setText(String.format(
                mResources.getString(R.string.transaction_you_owe_group),
                transaction.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setText(mSymbol +
                transaction.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
    }

    private void setGroupOwesUserTexts(ParseObject transaction, TextView transactionGroup,
                                       TextView transactionAmount) {
        transactionGroup.setText(String.format(
                mResources.getString(R.string.transaction_group_owes_you),
                transaction.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setTextColor(mResources.getColor(R.color.green));
        transactionAmount.setText(mSymbol +
                transaction.getString(Constants.TRANSACTION_TOTAL_AMOUNT));
    }

    // Return string based on if user has paid or not
    private void setPaidStatus(TextView amountText, TextView statusText, ParseObject group) {
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) group.get(Constants.GROUP_MEMBERS);
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) group.get(Constants.TRANSACTION_PAID);

        int i;
        for (i = 0; i < members.size(); i++) {
            if (members.get(i).equals(ParseUser.getCurrentUser().getEmail())) {
                break;
            }
        }

        if (paid.get(i) == 1) {
            statusText.setText(mResources.getString(R.string.paid));
            statusText.setTextColor(mResources.getColor(R.color.light_grey));
            amountText.setTextColor(mResources.getColor(R.color.dark_grey));
        } else {
            statusText.setText(mResources.getString(R.string.pay_now));
            statusText.setTextColor(mResources.getColor(R.color.light_grey));
            amountText.setTextColor(mResources.getColor(R.color.pink));
        }
    }

    // Return string based on if transaction is complete or not
    private void setAmountStillOwed(TextView statusText, ParseObject group) {
        double splitAmount = Double.valueOf(group.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) group.get(Constants.TRANSACTION_PAID);
        int notPaid = 0;

        for (Integer p : paid) {
            if (p == 0) {
                notPaid++;
            }
        }

        if (notPaid == 0) {
            statusText.setText(mResources.getString(R.string.complete));
            statusText.setTextColor(mResources.getColor(R.color.light_grey));
        } else {
            statusText.setText(String.format(mResources.getString(R.string.transaction_amount_owed),
                    mSymbol, String.format("%.2f", splitAmount * notPaid)));
            statusText.setTextColor(mResources.getColor(R.color.pink));
        }
    }

}
