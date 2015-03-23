package com.example.kirstiebooras.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kirstiebooras.helpers.Constants;
import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;

/**
 * Adapter for the spinner in CreateTransactionActivity.
 * Holds an array of ParseObjects and displays the value corresponding to "name".
 * Created by kirstiebooras on 1/30/15.
 */
public class GroupsSpinnerAdapter extends ArrayAdapter<ParseObject> {

    private Context mContext;
    private ArrayList<ParseObject> mGroups;

    public GroupsSpinnerAdapter(Context context, ArrayList<ParseObject> groups) {
        super(context, R.layout.groups_spinner, groups);
        mContext = context;
        mGroups = groups;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.groups_spinner, parent, false);
        }

        TextView group = (TextView) convertView.findViewById(R.id.groupTextView);
        group.setText(mGroups.get(position).getString(Constants.GROUP_NAME));

        return convertView;
    }

}
