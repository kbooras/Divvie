package com.example.kirstiebooras;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;

/**
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

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.groups_spinner, parent, false);
        }

        TextView group = (TextView) convertView.findViewById(R.id.groupTextView);
        group.setText(mGroups.get(position).getString("name"));

        return convertView;
    }

}
