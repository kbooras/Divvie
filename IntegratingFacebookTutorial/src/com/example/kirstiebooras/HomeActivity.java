package com.example.kirstiebooras;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Shows the pending group transactions the user has.
 * Shows who has paid them, who still owes them, and completed group charges.
 * Created by kirstiebooras on 1/16/15.
 */
public class HomeActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG = "HomeActivity";
    private static final String CLASSNAME_GROUP = "Group";
    private static final String CLASSNAME_TRANSACTION = "Transaction";
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private List<ParseObject> mTransactionsData;
    private List<ParseObject> mGroupsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.home_activity);

        checkForCurrentUser();

        if (isNetworkConnected()) {
            // If there is a network connection, get data from Parse
            Log.v(TAG, "connected to network");
            getParseData(CLASSNAME_TRANSACTION);
            getParseData(CLASSNAME_GROUP);
        }
        else {
            // Otherwise get data from local datastore
            Log.v(TAG, "no network connection");
            getPinnedData(CLASSNAME_TRANSACTION);
            getPinnedData(CLASSNAME_GROUP);
        }

        TabsFragmentPagerAdapter tabsAdapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());

        // Home button should not be enabled, since there is no hierarchical parent.
        mActionBar = getActionBar();
        mActionBar.setHomeButtonEnabled(false);

        // Set up the ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(tabsAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int i) {
                mActionBar.setSelectedNavigationItem(i);
            }
        });

        // Add the tabs to the action bar
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.addTab(mActionBar.newTab().setText(
                getResources().getString(R.string.transactions)).setTabListener(this));
        mActionBar.addTab(mActionBar.newTab().setText(
                getResources().getString(R.string.groups)).setTabListener(this));
        setTabsBelowActionBar();

    }

    /**
     * Checks for network connection
     * @return true if connected
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * Adds the tabs below the action bar
     */
    public void setTabsBelowActionBar() {
        try {
            final Method setHasEmbeddedTabsMethod = mActionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(mActionBar, false);
        }
        catch(final Exception e) {
            // Handle issues as needed: log, warn user, fallback etc
            // This error is safe to ignore, standard tabs will appear.
        }
    }

    /**
     * Get data from Parse.
     * @param className: The type of objects the ParseQuery will be searching for.
     */
    private void getParseData(final String className) {
        Log.v(TAG, "getParseData");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
        query.orderByDescending("createdAt");
        try {
            final List<ParseObject> parseObjects = query.find();

            if (className.equals(CLASSNAME_TRANSACTION)) {
                mTransactionsData = parseObjects;
            }
            else if (className.equals(CLASSNAME_GROUP)) {
                mGroupsData = parseObjects;
            }

            // Release any objects previously pinned for this query.
            Log.v(TAG, className + ": Found " + parseObjects.size());
            ParseObject.unpinAllInBackground(className, parseObjects, new DeleteCallback() {
                public void done(ParseException e) {
                    if (e != null) {
                        // There was some error.
                        Log.v(TAG, "Unpin error: " + e.getMessage());
                        return;
                    }

                    // Add the latest results for this query to the cache.
                    Log.v(TAG, className + ": Pinned " + parseObjects.size());
                    ParseObject.pinAllInBackground(className, parseObjects);
                }
            });
        } catch (ParseException e) {
            Log.v(TAG, "Query error: " + e.getMessage());
        }
    }

    /**
     * Get data from the local datastore.
     * @param className: The type of objects the ParseQuery will be searching for.
     */
    private void getPinnedData(String className) {
        Log.v(TAG, "getPinnedData");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(className);
        query.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
        query.fromLocalDatastore();
        query.orderByDescending("createdAt");
        try {
            List<ParseObject> parseObjects = query.find();
            Log.v(TAG, "Found " + parseObjects.size() + " objects in local datastore");

            if (className.equals(CLASSNAME_TRANSACTION)) {
                mTransactionsData = parseObjects;
            }
            else if (className.equals(CLASSNAME_GROUP)) {
                mGroupsData = parseObjects;
            }
        } catch (ParseException e) {
            Log.v(TAG, "Query error: " + e.getMessage());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.logout:
//                // Logout Facebook user
//                if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
//                    Session session = ParseFacebookUtils.getSession();
//                    if (session != null && session.isOpened()) {
//                        session.closeAndClearTokenInformation();
//                    }
//                }
                // TODO: unpin data
                ParseUser.logOut();
                Log.v(TAG, "User signed out!");
                startSigninRegisterActivity();
                return true;

            case R.id.add:
                int currentFragment = mViewPager.getCurrentItem();
                if (currentFragment == 0) {
                    if (userHasGroups()) {
                        // User must have at least one group to make a transaction
                        Intent intent = new Intent(this, CreateTransactionActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.user_no_groups_toast),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    startActivity(intent);
                }
                return true;
            default:
                return false;
        }
    }

    private boolean userHasGroups() {
        ParseQuery<ParseObject> groupQuery = ParseQuery.getQuery("Group");
        groupQuery.whereEqualTo(Constants.GROUP_MEMBERS, ParseUser.getCurrentUser().getEmail());
        try {
            groupQuery.getFirst();
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction arg1) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabUnselected(ActionBar.Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForCurrentUser();
    }

    /**
     * Check if there is a currently logged in user
     */
    private void checkForCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If the current user is null, send to sign in or register
            startSigninRegisterActivity();
        }
    }

    private void startSigninRegisterActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public List<ParseObject> getGroupsData() {
        return mGroupsData;
    }

    public List<ParseObject> getTransactionsData() {
        return mTransactionsData;
    }

}
