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
                return transactions;
            case 1:
                Fragment groups = new GroupsFragment();
                mPageReferenceMap.put(index, groups);
                return groups;
            default:
                return null;
        }
    }

    public Fragment getCurrentFragment(int index) {
        return  mPageReferenceMap.get(index);
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
