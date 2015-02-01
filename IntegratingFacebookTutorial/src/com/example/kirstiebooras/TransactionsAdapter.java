package com.example.kirstiebooras;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
    private Resources res;
    private String mSymbol;

    public TransactionsAdapter(Context context, ArrayList<ParseObject> transactions) {
        super(context, R.layout.transactions_fragment_row, transactions);
        mTransactions = transactions;
        res = context.getResources();
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
        TextView amountStatus = (TextView) rowView.findViewById(R.id.amountStatus);

        // We retrieve the object from the list
        ParseObject group = mTransactions.get(position);
        if (group != null) {
            if (!group.get("personOwed").toString().equals(ParseUser.getCurrentUser().getEmail())) {
                // User owes the group
                setUserOwesGroupTexts(group, transactionGroup, transactionAmount, amountStatus);
                Log.v(TAG, "User owes the group");
            } else {
                // The group owes user
                setGroupOwesUserTexts(group, transactionGroup, transactionAmount, amountStatus);
                Log.v(TAG, "The group owes the user");
            }

            transactionDescription.setText(String.format(
                    res.getString(R.string.transaction_description),
                    group.getString("description")));

        }

        return rowView;
    }

    private void setUserOwesGroupTexts(ParseObject group, TextView transactionGroup,
                                       TextView transactionAmount, TextView amountStatus) {
        transactionGroup.setText(String.format(
                res.getString(R.string.transaction_you_owe_group),
                group.getString("groupName")));
        transactionAmount.setTextColor(Color.parseColor("#F2447E"));
        transactionAmount.setText(group.getNumber("splitAmount").toString());
        setPaidStatus(amountStatus, group);
    }

    private void setGroupOwesUserTexts(ParseObject group, TextView transactionGroup,
                                       TextView transactionAmount, TextView amountStatus) {
        transactionGroup.setText(String.format(
                res.getString(R.string.transaction_group_owes_you),
                group.getString("groupName")));
        transactionAmount.setTextColor(Color.parseColor("#83CD6E"));
        transactionAmount.setText(mSymbol + group.getNumber("totalAmount").toString());
        setAmountStillOwed(amountStatus, group);
    }

    // Return string based on if user has paid or not
    private void setPaidStatus(TextView text, ParseObject group) {
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) group.get("members");
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) group.get("paid");

        int i;
        for (i = 0; i < members.size(); i++) {
            if (members.get(i).equals(ParseUser.getCurrentUser().getEmail())) {
                break;
            }
        }

        if (paid.get(i) == 1) {
            text.setText(res.getString(R.string.paid));
            text.setTextColor(Color.parseColor("#5C5C5C"));
        } else {
            text.setText(res.getString(R.string.pay_now));
            text.setTextColor(Color.parseColor("#5C5C5C"));
        }
    }

    // Return string based on if transaction is complete or not
    private void setAmountStillOwed(TextView text, ParseObject group) {
        double splitAmount = group.getNumber("splitAmount").doubleValue();
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) group.get("paid");
        int notPaid = 0;

        for (Integer p : paid) {
            if (p == 0) {
                notPaid++;
            }
        }

        if (notPaid == 0) {
            text.setText(res.getString(R.string.complete));
            text.setTextColor(Color.parseColor("#5C5C5C"));
        } else {
            text.setText(String.format(res.getString(R.string.transaction_amount_owed),
                    mSymbol, String.valueOf(splitAmount * notPaid)));
            text.setTextColor(Color.parseColor("#F2447E"));
        }
    }

}
