package com.example.kirstiebooras;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to handle calls to the parse server
 * Created by kirstiebooras on 3/20/15.
 */
public final class ParseMethods {

    private static final String TAG = "ParseQueries";

//    private ParseMethods() {
//
//    }
//
//    public interface GetParseDataListener {
//        public void onGetParseDataComplete(String className);
//    }
//
    /**
     * Get data from the Local Datastore.
     * @param className: The type of objects the ParseQuery will be searching for.
     */
    public static List<ParseObject> getLocalData(final String className) {
        Log.d(TAG, className + ": getLocalData");
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
    public static void getParseData(final String className) {
        Log.d(TAG, className + ": getParseData");
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

    /**
     * Create a Transaction object
     * @param groupId: The ID for the group this transaction belongs to
     * @param groupName: The name of the group this transaction belongs to
     * @param personOwed: The username for the person owed
     * @param descriptionTxt: A description for the transaction
     * @param totalAmount: The total bill
     * @param members: The members of the group
     * @param displayNames: The display names for the members of the group
     * @param splitAmount: The amount each member owes
     */
    public static void createTransactionParseObject(String groupId, String groupName,
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
        newTransaction.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Create Transaction object error: " + e.getMessage());
                    // TODO notify the user
                }
                else {
                    Log.i(TAG, "Saved new transaction successfully!");
                }
            }
        });
        //TODO Extend ParseObject and use UUID and SaveEventually and write to Local Datastore
        Log.i(TAG, "Saved new transaction successfully!");

    }

    /**
     * Send email to every one splitting the transaction.
     * @param map: Map containing all data needed to create the email.
     * @param members: An array of all persons splitting the bills
     */
    public static void sendNewTransactionEmails(HashMap<String, Object> map, String[] members) {
        for(String email : members) {
            map.put("toEmail", email);
            // TODO: move this loop to the cloud
            ParseCloud.callFunctionInBackground("sendChargeEmail", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Send email error: " + e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Create a Group object.
     * @param name: Name of the group
     * @param memberEmails: Array holding the emails for the members
     * @param memberNames: Array holding the names of the members
     */
    public static void createParseGroupObject(String name, ArrayList<String> memberEmails,
                                       ArrayList<String> memberNames){
        ParseObject newGroup = new ParseObject(Constants.CLASSNAME_GROUP);
        newGroup.put(Constants.GROUP_NAME, name);
        newGroup.put(Constants.GROUP_MEMBERS, memberEmails);
        newGroup.put(Constants.GROUP_DISPLAY_NAMES, memberNames);
        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Create Group object error: " + e.getMessage());
                    // TODO notify the user
                }
                else {
                    Log.i(TAG, "Saved new group successfully!");
                }
            }
        });
        //TODO Extend ParseObject and use UUID and write to Local Datastore
    }

    /**
     * Send email to person added to a group who does not yet have a Divvie account.
     * @param map: Map containing all data needed to create the email.
     */
    public static void sendNewUserEmail(HashMap<String, Object> map) {
        ParseCloud.callFunctionInBackground("sendNewUserEmail", map, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Send new user email error: " + e.toString());
                }
            }
        });
    }

    /**
     * Find a ParseObject in the Local Datastore
     * @param className: The type of ParseObject
     * @param objectId: The id for the object
     * @return The ParseObject or null if one is not found.
     */
    public static ParseObject findLocalParseObjectById(String className, String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.OBJECT_ID, objectId);
        query.fromLocalDatastore();
        try {
            Log.v(TAG, "Found object");
            return query.getFirst();
        }
        catch (ParseException e) {
            Log.e(TAG, "findLocalParseObjectById query error: " + e.getMessage());
            return  null;
        }
    }

    /**
     * Remove ParseObjects from the Local Datastore.
     * @param className: The type of ParseObjects to remove.
     */
    public static void unpinData(final String className) {
        Log.d(TAG, "unpinData");
        ParseObject.unpinAllInBackground(className, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    // There was some error.
                    Log.e(TAG, "Unpin error: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Unpinned " + className + " successfully.");
                }
            }
        });
    }

}
