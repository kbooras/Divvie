package com.example.kirstiebooras.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kirstiebooras.helpers.Constants;
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

    private ArrayList<ParseObject> mTransactions;
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
            } else {
                // The group owes user
                setGroupOwesUserTexts(transaction, transactionGroup, transactionAmount);
                setAmountStillOwed(transactionStatus, transaction);
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
                mResources.getString(R.string.transaction_you_owe),
                transaction.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setTextColor(mResources.getColor(R.color.pink));
        transactionAmount.setText(mSymbol +
                transaction.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
    }

    private void setGroupOwesUserTexts(ParseObject transaction, TextView transactionGroup,
                                       TextView transactionAmount) {
        transactionGroup.setText(String.format(
                mResources.getString(R.string.transaction_owes_you),
                transaction.getString(Constants.TRANSACTION_GROUP_NAME)));
        transactionAmount.setTextColor(mResources.getColor(R.color.green));
        transactionAmount.setText(mSymbol +
                transaction.getString(Constants.TRANSACTION_TOTAL_AMOUNT));
    }

    // Return string based on if user has paid or not. This happens when the user owes a group.
    private void setPaidStatus(TextView amountText, TextView statusText, ParseObject group) {
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) group.get(Constants.GROUP_MEMBERS);
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) group.get(Constants.TRANSACTION_DATE_PAID);

        int index = getIndexForCurrentUser(members);
        if (index == -1) {
            return;
        }
        if (datePaid.get(index).equals("")) {
            statusText.setText(mResources.getString(R.string.pay_now));
        } else if (datePaid.get(index).charAt(0) == 'p') {
            statusText.setText(mResources.getString(R.string.transaction_pending));
            statusText.setTypeface(null, Typeface.ITALIC);
        } else {
            statusText.setText(mResources.getString(R.string.paid));
        }
        statusText.setTextColor(mResources.getColor(R.color.dark_grey));
        amountText.setTextColor(mResources.getColor(R.color.pink));

    }

    private int getIndexForCurrentUser(ArrayList<String> members) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(ParseUser.getCurrentUser().getEmail())) {
                return i;
            }
        }
        return -1;
    }

    // Return string based on if transaction is complete or not. This happens when a user is owed.
    private void setAmountStillOwed(TextView statusText, ParseObject group) {
        double splitAmount = Double.valueOf(group.getString(Constants.TRANSACTION_SPLIT_AMOUNT));
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) group.get(Constants.TRANSACTION_DATE_PAID);
        int notPaid = 0;

        for (String p : datePaid) {
            // If not paid or pending
            if (p.equals("") || p.charAt(0) == 'p') {
                notPaid++;
            }
        }

        if (notPaid == 0) {
            statusText.setText(mResources.getString(R.string.complete));
            statusText.setTextColor(mResources.getColor(R.color.dark_grey));
        } else {
            statusText.setText(String.format(mResources.getString(R.string.transaction_amount_owed),
                    mSymbol, String.format("%.2f", splitAmount * notPaid)));
            statusText.setTextColor(mResources.getColor(R.color.pink));
        }
    }

}
