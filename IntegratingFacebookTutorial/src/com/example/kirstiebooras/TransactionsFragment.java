package com.example.kirstiebooras;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.integratingfacebooktutorial.R;

/**
 * Fragment representing the transactions view.
 * Displays all group transactions, who has paid, and who still owes.
 * Created by kirstiebooras on 1/19/15.
 */
public class TransactionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transactions_fragment, container, false);
    }
}
