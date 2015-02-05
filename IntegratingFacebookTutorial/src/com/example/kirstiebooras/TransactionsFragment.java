package com.example.kirstiebooras;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Fragment representing the transactions view.
 * Displays all group transactions, who has paid, and who still owes.
 * Created by kirstiebooras on 1/19/15.
 */
public class TransactionsFragment extends ListFragment {

    private ArrayList<ParseObject> mTransactions;
    private TransactionsAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTransactions = new ArrayList<ParseObject>();
        mAdapter = new TransactionsAdapter(getActivity().getBaseContext(), mTransactions);
        setListAdapter(mAdapter);

        getDataFromParse();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDataFromParse();
    }

    private void getDataFromParse() {
        if (ParseUser.getCurrentUser() != null) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("currentUser", ParseUser.getCurrentUser().getEmail());
            ParseCloud.callFunctionInBackground("getTransactionsDescending", map,
                    new FunctionCallback<Object>() {
                        public void done(Object results, ParseException e) {
                            if (e == null) {
                                mTransactions.clear();
                                mTransactions.addAll((List<ParseObject>) results);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TextView transactionStatus = (TextView) v.findViewById(R.id.transactionStatus);
        Resources res = getResources();

        ParseObject transaction = mAdapter.getItem(position);

        if (transactionStatus.getText().toString().equals(res.getString(R.string.pay_now))) {
            // Start PayChargeActivity
            Intent intent = new Intent(getActivity(), PayChargeActivity.class);
            intent.putExtra("parseObjectId", transaction.getObjectId());
            startActivity(intent);
        } else if (!transactionStatus.getText().equals(res.getString(R.string.paid))) {
            // TODO popup for remind or override
            // Display transaction
            Intent intent = new Intent(getActivity(), ViewTransactionActivity.class);
            intent.putExtra("parseObjectId", transaction.getObjectId());
            intent.putExtra("transactionStatus", transactionStatus.getText().toString());
            startActivity(intent);
        }
    }

}
