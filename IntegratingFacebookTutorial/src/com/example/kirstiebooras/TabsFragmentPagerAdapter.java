package com.example.kirstiebooras;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * FragmentPagerAdapter which loads the fragments into the ViewPager.
 * Created by kirstiebooras on 1/19/15.
 */
public class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "TabsFragmentPagerAdapter";
    private Map<Integer, Fragment> mPageReferenceMap;

    public TabsFragmentPagerAdapter(FragmentManager manager) {
        super(manager);
        mPageReferenceMap = new HashMap<Integer, Fragment>();
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                Fragment transactions = new TransactionsFragment();
                mPageReferenceMap.put(index, transactions);
                Log.v(TAG, "case 0 " + mPageReferenceMap.size());
                return transactions;
            case 1:

                Fragment groups = new GroupsFragment();
                mPageReferenceMap.put(index, groups);
                Log.v(TAG, "case 1 " + mPageReferenceMap.size());
                return groups;
            default:
                return null;
        }
    }

    public TransactionsFragment getTransactionsFragment() {
        if (mPageReferenceMap.isEmpty()) {
            Log.e(TAG, "mPageReferenceMap is empty.");
            return null;
        }
        else {
            Fragment fragment = mPageReferenceMap.get(0);
            if (!(fragment instanceof TransactionsFragment)) {
                Log.wtf(TAG, "Value returned from getGroupsFragment not instance of TransactionsFragment");
                return null;
            } else {
                Log.v(TAG, "returned fragment");
                return (TransactionsFragment) fragment;
            }
        }
    }

    public GroupsFragment getGroupsFragment() {
        if (mPageReferenceMap.size() < 2) {
            Log.e(TAG, "mPageReferenceMap has a size less than 2.");
            return null;
        }
        else {
            Fragment fragment = mPageReferenceMap.get(1);
            if (!(fragment instanceof GroupsFragment)) {
                Log.wtf(TAG, "Value returned from getGroupsFragment not instance of GroupsFragment");
                return null;
            } else {
                return (GroupsFragment) fragment;
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
