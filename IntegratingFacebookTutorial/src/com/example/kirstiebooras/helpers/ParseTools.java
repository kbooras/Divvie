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
import java.util.Calendar;
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
        public void onReminderEmailSent();
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
                    if (mGetParseDataListener != null) {
                        mGetParseDataListener.onGetParseDataComplete(className);
                    }
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
            Log.i(TAG, "Found " + parseObjects.size() + " objects in Local Datastore.");
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
            Log.v(TAG, "Found object");
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
                if (e != null) {
                    // There was some error.
                    Log.e(TAG, "Unpin error: " + e.getMessage());
                } else {
                    Log.i(TAG, "Unpinned " + className + " successfully.");
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
                }
                else {
                    Log.i(TAG, "Saved new transaction successfully!");
                }
            }
        });
        //TODO Extend ParseObject and use UUID and SaveEventually and write to Local Datastore
        Log.i(TAG, "Saved new transaction successfully!");

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
     * Mark user as having paid charge. Check if the transaction is now complete.
     */
    public void markChargePaid(String transactionId) {
        Log.d(TAG, "Mark charge as paid");
        ParseObject transaction =
                findLocalParseObjectById(Constants.CLASSNAME_TRANSACTION, transactionId);
        if (transaction == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> members = (ArrayList<String>) transaction.get(Constants.GROUP_MEMBERS);
        @SuppressWarnings("unchecked")
        ArrayList<Integer> paid = (ArrayList<Integer>) transaction.get(Constants.TRANSACTION_PAID);
        @SuppressWarnings("unchecked")
        ArrayList<String> datePaid = (ArrayList<String>) transaction.get(Constants.TRANSACTION_DATE_PAID);

        String currentUser = ParseUser.getCurrentUser().getEmail();
        boolean complete = true;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equals(currentUser)) {
                // Mark this person as paid and set their date paid
                paid.set(i, 1);
                Date date = new Date(System.currentTimeMillis());
                String today = new SimpleDateFormat("MM/dd/yy").format(date);
                datePaid.set(i,today);
            }
            if (paid.get(i) == 0) {
                // Check if this transaction is complete or not
                complete = false;
            }
        }

        transaction.put(Constants.TRANSACTION_PAID, paid);
        transaction.put(Constants.TRANSACTION_DATE_PAID, datePaid);
        if (complete) {
            transaction.put(Constants.TRANSACTION_COMPLETE, true);
            Log.i(TAG, "Transaction completed!");
        }
        transaction.saveInBackground(new SaveCallback() {
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

    }

    /*
     * Create a Group object and save to the Parse server.
     */
    public void createParseGroupObject(String groupName, ArrayList<String> memberEmails) {
        ArrayList<String> memberDisplayNames = getMemberDisplayNames(memberEmails);
        // Todo: Send invite emails to new users and notification email to existing users
        ParseObject newGroup = new ParseObject(Constants.CLASSNAME_GROUP);
        newGroup.put(Constants.GROUP_NAME, groupName);
        newGroup.put(Constants.GROUP_MEMBERS, memberEmails);
        newGroup.put(Constants.GROUP_DISPLAY_NAMES, memberDisplayNames);
        newGroup.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Create Group object error: " + e.getMessage());
                }
                else {
                    Log.i(TAG, "Saved new group successfully!");
                }
            }
        });
        //TODO Extend ParseObject and use UUID and write to Local Datastore
    }

    /*
     * Get display names for members of a group.
     * Add the corresponding name, or email if there is no existing user.
     */
    private ArrayList<String> getMemberDisplayNames(ArrayList<String> memberEmails) {
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
                    mSendReminderEmailListener.onReminderEmailSent();
                }
                else {
                    Log.e(TAG, "Send reminder email error: " + e.toString());
                }
            }
        });

    }

}
