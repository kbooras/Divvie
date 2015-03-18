package com.example.kirstiebooras;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.lang.reflect.Method;

/**
 * Shows the pending group transactions the user has.
 * Shows who has paid them, who still owes them, and completed group charges.
 * Created by kirstiebooras on 1/16/15.
 */
public class HomeActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final String TAG = "HomeActivity";
    private ActionBar mActionBar;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        checkForCurrentUser();

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

}
