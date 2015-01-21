package com.example.kirstiebooras;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;

/**
 * Created by kirstiebooras on 1/20/15.
 */
public class GroupsAdapter extends ArrayAdapter<ParseObject> {

    private final Context context;
    private final ArrayList<ParseObject> groups;


    public GroupsAdapter(Context context, ArrayList<ParseObject> groups) {
        super(context, R.layout.groups_list_row, groups);
        this.context = context;
        this.groups = groups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.groups_list_row, parent, false);
        } else {
            rowView = convertView;
        }

        LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.layout);
        TextView textView = (TextView) rowView.findViewById(R.id.groupName);

        // We retrieve the object from the list
        ParseObject group = groups.get(position);
        if (group != null) {
            textView.setText(group.getString("name"));
            ArrayList<String> members = (ArrayList<String>) group.get("users");
            // Create a TextView for each member
            for (int i = 0; i < members.size(); i++) {
               ll.addView(createTextView(members.get(i), i));
            }
        }

        return rowView;
    }

    private TextView createTextView(String member, int textViewNumber) {
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final TextView textView = new TextView(context);

        textView.setId(textViewNumber);
        textView.setLayoutParams(lparams);
        textView.setText(member);

        return textView;
    }
}
