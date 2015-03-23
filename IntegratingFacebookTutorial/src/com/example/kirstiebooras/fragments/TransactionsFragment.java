package com.example.kirstiebooras.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kirstiebooras.activities.HomeActivity;
import com.example.kirstiebooras.activities.PayChargeActivity;
import com.example.kirstiebooras.activities.ViewTransactionActivity;
import com.example.kirstiebooras.adapters.TransactionsAdapter;
import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment representing the transactions view.
 * Displays all group transactions, who has paid, and who still owes.
 * Created by kirstiebooras on 1/19/15.
 */
public class TransactionsFragment extends ListFragment {

    private static final String TAG = "TransactionsFragment";
    private ArrayList<ParseObject> mTransactions;
    private TransactionsAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTransactions = new ArrayList<ParseObject>();
        mAdapter = new TransactionsAdapter(getActivity().getBaseContext(), mTransactions);
        setListAdapter(mAdapter);

        // Workaround for problem where fragments are not available in the HomeActivity onCreate method
        // to initially set the data.
        HomeActivity homeActivity = (HomeActivity) getActivity();
        bindData(homeActivity.getTransactionsData());

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove lines between list views
        getListView().setDivider(null);
    }

    /*
     * Attach the data passed in from HomeActivity to the adapter
     */
    public void bindData(List<ParseObject> data) {
        Log.v(TAG, "bindData");
        if (data == null) {
            Log.e(TAG, "Data is null");
            return;
        }
        mTransactions.clear();
        ArrayList<ParseObject> a = new ArrayList<ParseObject>();
        a.addAll(data);
        mTransactions.addAll(a);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TextView transactionStatus = (TextView) v.findViewById(R.id.transactionStatus);
        ParseObject transaction = mAdapter.getItem(position);

        if (transactionStatus.getText().toString().equals(getString(R.string.pay_now))) {
            // Start PayChargeActivity
            Intent intent = new Intent(getActivity(), PayChargeActivity.class);
            intent.putExtra("parseObjectId", transaction.getObjectId());
            startActivity(intent);
        } else if (!transactionStatus.getText().equals(getString(R.string.paid))) {
            // TODO popup for remind or override
            // Display transaction
            Intent intent = new Intent(getActivity(), ViewTransactionActivity.class);
            intent.putExtra("parseObjectId", transaction.getObjectId());
            startActivity(intent);
        }
    }

}
