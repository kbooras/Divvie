package com.example.kirstiebooras;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ArrayAdapter to load a user's group info into the groups fragment.
 * Created by kirstiebooras on 1/20/15.
 */
public class GroupsAdapter extends ArrayAdapter<ParseObject> {

    private final Context mContext;
    private final ArrayList<ParseObject> mGroups;


    public GroupsAdapter(Context context, ArrayList<ParseObject> groups) {
        super(context, R.layout.groups_list_row, groups);
        mContext = context;
        mGroups = groups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            rowView = inflater.inflate(R.layout.groups_list_row, parent, false);
        } else {
            rowView = convertView;
        }

        TextView textView = (TextView) rowView.findViewById(R.id.groupName);
        LinearLayout membersLayout = (LinearLayout) rowView.findViewById(R.id.membersLayout);
        membersLayout.removeAllViews();

        // We retrieve the object from the list
        ParseObject group = mGroups.get(position);
        if (group != null) {
            textView.setText(group.getString("name"));
            ArrayList<String> members = (ArrayList<String>) group.get("users");
            // Create a TextView for each member
            for (int i = 0; i < members.size(); i++) {
               membersLayout.addView(createMemberView(members.get(i), i));
            }
        }

        return rowView;
    }

    private LinearLayout createMemberView(final String memberEmail, int textViewNumber) {
        LinearLayout memberView = (LinearLayout) View.inflate(mContext, R.layout.group_member_row,
                null);
        final TextView textView = (TextView) memberView.findViewById(R.id.member);
        textView.setId(textViewNumber);

        // Display name if they have an account, email otherwise
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("email", memberEmail);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                if (results.size() == 0) {
                    textView.setText(memberEmail);
                } else {
                    textView.setText(results.get(0).getString("fullName"));
                }

            }
        });

        return memberView;
    }
}
