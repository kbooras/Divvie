package com.example.kirstiebooras;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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

        // Get data from HomeActivity member variables
        HomeActivity homeActivity = (HomeActivity) getActivity();
        bindData(homeActivity.getTransactionsData());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove lines between list views
        getListView().setDivider(null);
    }

    /**
     * Attach the data passed in from HomeActivity to the adapter
     * @param data: The data from HomeActivity
     */
    private void bindData(List<ParseObject> data) {
        Log.v(TAG, "bindData");
        mTransactions.clear();
        mTransactions.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Display appropriate action when a list item is clicked depending on the type of transaction
     * @param l: The ListView
     * @param v: The item clicked in the ListView
     * @param position: The index clicked in the ListView
     * @param id:
     */
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
            intent.putExtra("transactionStatus", transactionStatus.getText().toString());
            startActivity(intent);
        }
    }

}
