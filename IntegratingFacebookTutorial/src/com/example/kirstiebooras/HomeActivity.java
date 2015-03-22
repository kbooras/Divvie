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

import com.parse.ParseObject;
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
    private static final int CREATE_TRANSACTION_REQUEST_CODE = 1;
    private static final int CREATE_GROUP_REQUEST_CODE = 2;
    private ParseTools mParseTools;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private List<ParseObject> mTransactionsData;
    private List<ParseObject> mGroupsData;
    private TabsFragmentPagerAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.home_activity);

        checkForCurrentUser();

        mParseTools = ((DivvieApplication) getApplication()).getParseTools();
        mParseTools.setGetParseDataListener(new ParseTools.GetParseDataListener() {
            @Override
            public void onGetParseDataComplete(String className) {
                // When Local Datastore is updated, update values of Lists
                if (className.equals(Constants.CLASSNAME_TRANSACTION)) {
                    mTransactionsData = mParseTools.getLocalData(className);
                }
                else if (className.equals(Constants.CLASSNAME_GROUP)) {
                    mGroupsData = mParseTools.getLocalData(className);
                }
            }
        });

        mTabsAdapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());

        // Home button should not be enabled, since there is no hierarchical parent.
        mActionBar = getActionBar();
        mActionBar.setHomeButtonEnabled(false);

        // Set up the ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mTabsAdapter);
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

        // Load data into the Lists
        if(mTransactionsData == null) {
            mTransactionsData = getArrayFromLocalDataStore(Constants.CLASSNAME_TRANSACTION);
        }
        if (mGroupsData == null) {
            mGroupsData = getArrayFromLocalDataStore(Constants.CLASSNAME_GROUP);
        }

        // Update the data in the Local Datastore
        updateLocalDatastore(Constants.CLASSNAME_TRANSACTION);
        updateLocalDatastore(Constants.CLASSNAME_GROUP);
    }

    private void checkForCurrentUser() {
        Log.d(TAG, "checkForCurrentUser");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If the current user is null, send to sign in or register
            startSigninRegisterActivity();
            finish();
        }
    }

    private void startSigninRegisterActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*
     * Update objects of the specified type in the Local Datastore with data from Parse.
     */
    private void updateLocalDatastore(String className) {
        if (isNetworkConnected()) {
            mParseTools.getParseData(className);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /*
     * Get array of specified type of ParseObjects from Local Datastore
     */
    private List<ParseObject> getArrayFromLocalDataStore(String className) {
        Log.d(TAG, className + ": getArrayFromLocalDataStore");
        return mParseTools.getLocalData(className);
    }

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
                mParseTools.unpinData(Constants.CLASSNAME_TRANSACTION);
                mParseTools.unpinData(Constants.CLASSNAME_GROUP);
                ParseUser.logOut();
                Log.i(TAG, "User signed out!");
                startSigninRegisterActivity();
                return true;

            case R.id.add:
                int currentFragment = mViewPager.getCurrentItem();
                if (currentFragment == 0) {
                    if (userHasGroups()) {
                        // User must have at least one group to make a transaction
                        Log.v(TAG, "Start create transaction");
                        Intent intent = new Intent(this, CreateTransactionActivity.class);
                        startActivityForResult(intent, CREATE_TRANSACTION_REQUEST_CODE);
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.user_no_groups_toast),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.v(TAG, "Start create transaction");
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    startActivityForResult(intent, CREATE_GROUP_REQUEST_CODE);
                }
                return true;
            default:
                return false;
        }
    }

    private boolean userHasGroups() {
        if (mGroupsData != null) {
            return mGroupsData.size() != 0;
        }
        else {
            Log.wtf(TAG, "mGroupsData is null");
            return false;
        }
    }

    /*
     * Result from CreateTransactionActivity or CreateGroupActivity letting HomeActivity know a new
     * ParseObject was created and the List data should be reloaded.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == CREATE_TRANSACTION_REQUEST_CODE) {
                mTransactionsData = getArrayFromLocalDataStore(Constants.CLASSNAME_TRANSACTION);
                mTabsAdapter.getTransactionsFragment().bindData(mTransactionsData);
            }
            else if (requestCode == CREATE_GROUP_REQUEST_CODE) {
                mGroupsData = getArrayFromLocalDataStore(Constants.CLASSNAME_GROUP);
                mTabsAdapter.getGroupsFragment().bindData(mGroupsData);
            }
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
