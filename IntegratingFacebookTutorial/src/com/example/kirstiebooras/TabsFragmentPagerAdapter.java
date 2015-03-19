package com.example.kirstiebooras;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * FragmentPagerAdapter which loads the fragments into the ViewPager.
 * Created by kirstiebooras on 1/19/15.
 */
public class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

    private TransactionsFragment mTransactionsFragment;
    private GroupsFragment mGroupsFragment;

    public TabsFragmentPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                mTransactionsFragment = new TransactionsFragment();
                return mTransactionsFragment;
            case 1:
                mGroupsFragment = new GroupsFragment();
                return mGroupsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public GroupsFragment getGroupsFragment() {
        return mGroupsFragment;
    }

    public TransactionsFragment getTransactionsFragment() {
        return mTransactionsFragment;
    }
}
