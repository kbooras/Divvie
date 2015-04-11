package com.example.kirstiebooras.helpers;

import android.content.Context;
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
import com.parse.integratingfacebooktutorial.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to handle queries to the Parse server and Local Datastore
 * Created by kirstiebooras on 3/20/15.
 */
public class ParseTools {

    private static final String TAG = "ParseTools";
    private GetParseDataListener mGetParseDataListener;
    private SendReminderEmailListener mSendReminderEmailListener;
    private Context mContext;

    public ParseTools(Context applicationContext) {
        mContext = applicationContext;
    }

    public interface GetParseDataListener {
        public void onGetParseDataComplete(String className);
    }

    public void setGetParseDataListener(GetParseDataListener listener) {
        mGetParseDataListener = listener;
    }

    public interface SendReminderEmailListener {
        public void onReminderEmailSent(boolean sent);
    }

    public void setSendReminderEmailListener(SendReminderEmailListener listener) {
        mSendReminderEmailListener = listener;
    }

    /*
     * Get data for the specified object type from the Parse server and update the Local Datastore.
     */
    public void getParseData(final String className) {
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
                } else {
                    // Release any objects previously pinned for this query.
                    ParseObject.unpinAllInBackground(className, parseObjects, new DeleteCallback() {
                        public void done(ParseException e) {
                            if (e != null) {
                                // There was some error.
                                Log.e(TAG, "Unpin error: " + e.getMessage());
                                return;
                            }

                            // Add the latest results for this query to the cache.
                            ParseObject.pinAllInBackground(className, parseObjects, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.i(TAG, className + ": Pinned " + parseObjects.size());
                                    if (mGetParseDataListener != null) {
                                        mGetParseDataListener.onGetParseDataComplete(className);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /*
     * Get data for the specified object type from the Local Datastore.
     */
    public List<ParseObject> getLocalData(final String className) {
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
            Log.i(TAG, "Found " + parseObjects.size() + " " + className + " objects in Local Datastore.");
            return parseObjects;
        }
        catch (ParseException e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            return null;
        }
    }

    /*
     * Find a ParseObject in the Local Datastore given the object type and objectId
     */
    public ParseObject findLocalParseObjectById(String className, String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.OBJECT_ID, objectId);
        query.fromLocalDatastore();
        try {
            return query.getFirst();
        }
        catch (ParseException e) {
            Log.e(TAG, "findLocalParseObjectById query error: " + e.getMessage());
            return  null;
        }
    }

    /*
     * Remove ParseObjects of the given type from the Local Datastore.
     */
    public void unpinData(final String className) {
        Log.d(TAG, "unpinData");
        ParseObject.unpinAllInBackground(className, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "Unpinned " + className + " successfully.");
                }
                else {
                    Log.e(TAG, "Unpin error: " + e.getMessage());
                }
            }
        });
    }

    /*
     * Get a user's full name given their email.
     */
    public String getUserDisplayName(String email) {
        Log.d(TAG, "getUserDisplayName");
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Constants.USER_EMAIL, email);
        try {
            return query.getFirst().getString(Constants.USER_FULL_NAME);
        }
        catch (ParseException e) {
            Log.e(TAG, "getUserDisplayName query error: " + e.getMessage());
            return null;
        }
    }

    /*
     * Create a Transaction object and save to the Parse server.
     */
    public void createTransactionParseObject(String groupId, String personOwed, String descriptionTxt,
                                              Double totalAmount) {
        Log.d(TAG, "createTransactionParseObject");
        ParseObject group = findLocalParseObjectById(Constants.CLASSNAME_GROUP, groupId);
        if (group == null) {
            Log.wtf(TAG, "The group for the new transaction is not found on the device.");
            return;
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) group.get(Constants.GROUP_MEMBERS);

        String totalAmountString = String.format("%.2f", totalAmount);
        String splitAmount = getSplitAmount(totalAmount, members.size());

        ParseObject newTransaction = new ParseObject(Constants.CLASSNAME_TRANSACTION);
        newTransaction.put(Constants.TRANSACTION_GROUP_ID, groupId);
        newTransaction.put(Constants.TRANSACTION_GROUP_NAME, group.getString(Constants.GROUP_NAME));
        newTransaction.put(Constants.GROUP_MEMBERS, members);
        newTransaction.put(Constants.TRANSACTION_PERSON_OWED, personOwed);
        newTransaction.put(Constants.TRANSACTION_DESCRIPTION, descriptionTxt);
        newTransaction.put(Constants.TRANSACTION_TOTAL_AMOUNT, totalAmountString);
        newTransaction.put(Constants.TRANSACTION_SPLIT_AMOUNT, splitAmount);

        // Set date paid values. PersonOwed is set as paid on today's date.
        ArrayList<String> datePaid = new ArrayList<String>(members.size());
        for (String user : members) {
            if (user.equals(personOwed)) {
                Date date = new Date(System.currentTimeMillis());
                String today = new SimpleDateFormat("M/d/yy").format(date);
                datePaid.add(today);
            } else {
                datePaid.add("");
            }
        }

        newTransaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);
        newTransaction.put(Constants.TRANSACTION_COMPLETE, false);
        newTransaction.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Create Transaction object error: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Saved new transaction successfully!");
                    getParseData(Constants.CLASSNAME_TRANSACTION);
                }
            }
        });
    }

    /*
     * Determine amount to be paid by each member of the group.
     */
    private String getSplitAmount(double amountValue, double numMembers) {
        double dividedAmount = amountValue / numMembers;
        BigDecimal bd = new BigDecimal(dividedAmount);
        return bd.setScale(2, BigDecimal.ROUND_FLOOR).toString();
    }

    /*
     * Send notification email to every one splitting the transaction.
     */
    public void sendNewTransactionEmails(HashMap<String, Object> map, String[] members) {
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

    /*
     * Mark member as having paid charge on the date 'datePaid'. Since we are linearly
     * traversing the array, simultaneously check if the transaction is now complete.
     * @param pending: True if the user's payment should be flagged as pending
     * @param memberIndex: The index in the members array of the user being marked as paid. If null,
     * this defaults to the current user.
     * @param date: If null, this defaults to today's date.
     */
    @SuppressWarnings("unchecked")
    public void markChargePaid(String transactionId, boolean pending, int memberIndex, String date) {
        Log.d(TAG, "markChargePaid");
        ParseObject transaction =
                findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION, transactionId);
        if (transaction == null) {
            return;
        }

        ArrayList<String> members = (ArrayList<String>) transaction.get(Constants.GROUP_MEMBERS);
        ArrayList<String> datePaid = (ArrayList<String>) transaction.get(Constants.TRANSACTION_DATE_PAID);

        String member;
        if (memberIndex == -1) {
            member = ParseUser.getCurrentUser().getEmail();
        } else {
            member = members.get(memberIndex);
        }

        boolean complete = true;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(member)) {
                // Set this person's date paid as today if the parameter is null
                if (date == null) {
                    Date today = new Date(System.currentTimeMillis());
                    date = new SimpleDateFormat("M/d/yy").format(today);
                }
                // Flag if pending
                if (pending) {
                    date = "p" + date;
                    // TODO Send notification to the person owed to verify
                }
                datePaid.set(i, date);
            }
            if (datePaid.get(i).equals("")) {
                complete = false;
            }
        }

        transaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);

        if (complete) {
            transaction.put(Constants.TRANSACTION_COMPLETE, true);
            Log.i(TAG, "Transaction completed!");
        }
        transaction.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error marking charge as paid: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Marked charge as paid successfully!");
                }
            }
        });
        //getParseData(Constants.CLASSNAME_TRANSACTION);
        mGetParseDataListener.onGetParseDataComplete(Constants.CLASSNAME_TRANSACTION);
    }

    /*
     * Accept a pending payment as valid or reject it.
     */
    public void updatePendingPayment(String transactionId, int memberIndex, boolean paid) {
        Log.d(TAG, "updatePendingPayment");
        ParseObject transaction =
                findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION, transactionId);
        if (transaction == null) {
            return;
        }

        boolean complete = true;
        ArrayList<String> datePaid = (ArrayList<String>) transaction.get(Constants.TRANSACTION_DATE_PAID);

        if (paid) {
            // Mark as paid by removing pending flag.
            datePaid.set(memberIndex, datePaid.get(memberIndex).substring(1));

            //Check if complete
            for (int i = 0; i < datePaid.size(); i++) {
                if (datePaid.get(i).equals("")) {
                    complete = false;
                }
            }
        }
        else {
            // Mark as not paid
            datePaid.set(memberIndex, "");
            complete = false;
        }
        transaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);

        if (complete) {
            transaction.put(Constants.TRANSACTION_COMPLETE, true);
            Log.i(TAG, "Transaction completed!");
        }

        // Updates the object in local datastore immediately, then pushes to server
        transaction.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error updating pending charge: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Updated pending charge successfully!");
                }
            }
        });
        mGetParseDataListener.onGetParseDataComplete(Constants.CLASSNAME_TRANSACTION);
    }

    /*
     * Create a Group object and save to the Parse server.
     */
    public void createGroupParseObject(String groupName, ArrayList<String> members,
                                       ArrayList<String> memberDisplayNames) {
        ParseObject newGroup = new ParseObject(Constants.CLASSNAME_GROUP);
        newGroup.put(Constants.GROUP_NAME, groupName);
        newGroup.put(Constants.GROUP_MEMBERS, members);
        newGroup.put(Constants.GROUP_DISPLAY_NAMES, memberDisplayNames);
        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Create Group object error: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Saved new group successfully!");
                    getParseData(Constants.CLASSNAME_GROUP);
                    // Todo: Send invite emails to new users and notification email to existing users
                }
            }
        });
    }

    /*
     * Get member username for Divvie user from the passed in Facebook ID.
     */
    public String getDivvieMemberFromFBId(String id) {
        Log.d(TAG, "getDivvieMemberFromFBId");
        // Check if there is a Divvie account with this id
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(Constants.FACEBOOK_ID, id);
        try {
            // If there is an account linked, the id is replaced with the email
            ParseUser user = query.getFirst();
            return user.getEmail();
        } catch (ParseException e) {
            // If there is no account linked, the id is left in the array
            Log.i(TAG, "No user found with given Facebook ID.");
            return id;
        }


    }

    /*
     * Get display names for members of a group.
     * Add the corresponding name, or email if there is no existing user.
     */
    public ArrayList<String> getMemberDisplayNames(ArrayList<String> memberEmails) {
        ArrayList<String> memberNames = new ArrayList<String>(memberEmails.size());
        for (String email : memberEmails) {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(Constants.USER_EMAIL, email);
            try {
                ParseUser user = query.getFirst();
                memberNames.add(user.getString(Constants.USER_FULL_NAME));
            } catch (ParseException e) {
                memberNames.add(email);
            }
        }
        return memberNames;
    }

    /*
     * Send email to person added to a group who does not yet have a Divvie account.
     */
    public void sendInviteEmails(ArrayList<String> noDivvieAccount, String fromName, String groupName) {
        // TODO Check for ids when sending emails!
        // TODO invite facebook friends
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("key", mContext.getString(R.string.MANDRILL_API_KEY));
        map.put("fromName", fromName);
        map.put("groupName", groupName);
        for (String email : noDivvieAccount) {
            map.remove("toEmail");
            map.put("toEmail", email);
            ParseCloud.callFunctionInBackground("sendInviteEmails", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "Send new user email sent successfully!");
                    }
                    else {
                        Log.e(TAG, "Send new user email error: " + e.toString());
                    }
                }
            });
        }
    }

    public void sendReminderEmail(String toEmail, String toName, String description, String fromName) {
        // TODO Check for ids when sending emails!
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("key", mContext.getString(R.string.MANDRILL_API_KEY));
        map.put("toEmail", toEmail);
        map.put("toName", toName);
        map.put("fromName", fromName);
        map.put("chargeDescription", description);
        ParseCloud.callFunctionInBackground("sendReminderEmail", map, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "Send reminder email sent successfully!");
                    mSendReminderEmailListener.onReminderEmailSent(true);
                }
                else {
                    Log.e(TAG, "Send reminder email error: " + e.toString());
                    mSendReminderEmailListener.onReminderEmailSent(false);
                }
            }
        });

    }

    /*
     * Called when a user links their FB account or signs up for Divvie with their FB account.
     * Finds any groups or transactions their FB ID is a part of and updates the member info.
     */
    public void updateDataForLinkedFacebookAccount(final String className, final ParseUser user) {
        final String fbID = user.getString(Constants.FACEBOOK_ID);
        final String name = user.getString(Constants.USER_FULL_NAME);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(className);
        query.whereEqualTo(Constants.GROUP_MEMBERS, fbID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error finding " + className + " for FB account: " + e.toString());
                    return;
                }
                for (ParseObject object : parseObjects) {
                    // Update the members array of each object
                    ArrayList<String> members = (ArrayList<String>) object.get(Constants.GROUP_MEMBERS);
                    ArrayList<String> displayNames = (ArrayList<String>) object.get(Constants.GROUP_DISPLAY_NAMES);
                    for (int i = 0; i < members.size(); i++) {
                        if (members.get(i).equals(fbID)) {
                            members.set(i, user.getEmail());
                            displayNames.set(i, name);
                            object.put(Constants.GROUP_MEMBERS, members);
                            object.put(Constants.GROUP_DISPLAY_NAMES, displayNames);
                            object.saveInBackground();
                            break;
                        }
                    }
                }
            }
        });
    }
}
