package com.example.kirstiebooras;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Utility class to handle calls to the parse server
 * Created by kirstiebooras on 3/20/15.
 */
public final class ParseMethods {

    private static final String TAG = "ParseQueries";

    /**
     * Get data from the Local Datastore.
     * @param className: The type of objects the ParseQuery will be searching for.
     */
    public List<ParseObject> getLocalData(final String className) {
        Log.d(TAG, "getLocalData");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.wtf(TAG, "Current user is null.");
            return null;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.GROUP_MEMBERS, currentUser.getEmail());
        query.fromLocalDatastore();
        query.orderByDescending("createdAt");
        try {
            List<ParseObject> parseObjects = query.find();
            Log.i(TAG, "Found " + parseObjects.size() + " objects in Local Datastore.");
            return parseObjects;
        }
        catch (ParseException e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get data from the Parse server and update the Local Datastore.
     * @param className: The ParseObject type to query for.
     */
    public void getParseData(final String className) {
        Log.d(TAG, "getParseData");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.GROUP_MEMBERS, currentUser.getEmail());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Query error: " + e.getMessage());
                }
                else {
                    // Release any objects previously pinned for this query.
                    Log.i(TAG, className + ": Found " + parseObjects.size());
                    ParseObject.unpinAllInBackground(className, parseObjects, new DeleteCallback() {
                        public void done(ParseException e) {
                            if (e != null) {
                                // There was some error.
                                Log.e(TAG, "Unpin error: " + e.getMessage());
                                return;
                            }

                            // Add the latest results for this query to the cache.
                            Log.i(TAG, className + ": Pinned " + parseObjects.size());
                            ParseObject.pinAllInBackground(className, parseObjects);
                        }
                    });
                }
            }
        });
    }

    public void createTransactionParseObject(String groupId, String groupName,
                                              String personOwed, String descriptionTxt,
                                              String totalAmount, ArrayList<String> members,
                                              ArrayList<String> displayNames, String splitAmount) {
        ParseObject newTransaction = new ParseObject("Transaction");

        newTransaction.put(Constants.TRANSACTION_GROUP_ID, groupId);
        newTransaction.put(Constants.TRANSACTION_GROUP_NAME, groupName);
        newTransaction.put(Constants.TRANSACTION_PERSON_OWED, personOwed);
        newTransaction.put(Constants.TRANSACTION_DESCRIPTION, descriptionTxt);
        newTransaction.put(Constants.TRANSACTION_TOTAL_AMOUNT, totalAmount);
        newTransaction.put(Constants.TRANSACTION_SPLIT_AMOUNT, splitAmount);
        newTransaction.put(Constants.GROUP_MEMBERS, members);
        newTransaction.put(Constants.GROUP_DISPLAY_NAMES, displayNames);

        // Set paid values and date paid values. PersonOwed is set as paid.
        ArrayList<Integer> paid = new ArrayList<Integer>(members.size());
        ArrayList<String> datePaid = new ArrayList<String>(members.size());
        for (String user : members) {
            if (user.equals(personOwed)) {
                paid.add(1);
                String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
                String date = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
                datePaid.add(month + "/" + date);
            } else {
                paid.add(0);
                datePaid.add("");
            }
        }

        newTransaction.put(Constants.TRANSACTION_PAID, paid);
        newTransaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);
        newTransaction.put(Constants.TRANSACTION_COMPLETE, false);
        newTransaction.saveEventually();
        //TODO Extend ParseObject and use UUID and write to Local Datastore
        Log.v(TAG, "Saved new transaction");

    }

}
