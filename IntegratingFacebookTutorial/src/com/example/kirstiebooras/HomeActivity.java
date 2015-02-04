package com.example.kirstiebooras;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.integratingfacebooktutorial.R;

import java.lang.reflect.Method;

/**
 * Shows the pending group transactions the user has.
 * Shows who has paid them, who still owes them, and completed group charges.
 * Created by kirstiebooras on 1/16/15.
 */
public class HomeActivity extends FragmentActivity implements ActionBar.TabListener {

    private ActionBar mActionBar;
    private ViewPager mViewPager;
    public static float sScale;
    private static final String TAG = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_activity);

        // Used to set font dp in fragments
        sScale = getResources().getDisplayMetrics().density;

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        TabsFragmentPagerAdapter tabsAdapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(tabsAdapter);

        mActionBar = getActionBar();

        // Home button should not be enabled, since there is no hierarchical parent.
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(false);
        }

        // Specify there will be tabs
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mActionBar.addTab(mActionBar.newTab().setText(
                getResources().getString(R.string.transactions)).setTabListener(this));
        mActionBar.addTab(mActionBar.newTab().setText(
                getResources().getString(R.string.groups)).setTabListener(this));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                mActionBar.setSelectedNavigationItem(i);
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // TODO Auto-generated method stub
            }
        });

        setTabsBelowActionBar();

        checkForCurrentUser();

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
                ParseUser.logOut();
                Log.v(TAG, "User signed out!");
                startSigninRegisterActivity();
                return true;

            case R.id.add:
                int currentFragment = mViewPager.getCurrentItem();
                if (currentFragment == 0) {
                    Intent intent = new Intent(this, CreateTransactionActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, CreateGroupActivity.class);
                    startActivity(intent);
                }
                return true;
            default:
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

    // Adds the tabs below the action bar
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

    // Check if there is a currently logged in user
    private void checkForCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            // If the current user is null, send to sign in or register
            startSigninRegisterActivity();
        }
        else if (ParseFacebookUtils.isLinked(currentUser)) {
            // If the current user is not null and is linked to a Facebook account
            // send to user details activity
            Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
            startActivity(intent);
        }
    }

    private void startSigninRegisterActivity() {
        Intent intent = new Intent(this, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
