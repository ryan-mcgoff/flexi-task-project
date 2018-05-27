package com.example.android.flexitask;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 */

public class TaskPageAdaptor extends FragmentPagerAdapter {

    /**
     * App Context
     */
    private Context mContext;


    public TaskPageAdaptor(Context context, FragmentManager fm) {

        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new FixedTaskTimeLine();
        } else {
            return new FlexiTaskTimeLine();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.fixed_task);
        } else {
            return mContext.getString(R.string.flexi_task);
        }
    }

    /*how many pages our viewpager will have*/
    @Override
    public int getCount() {
        return 2;
    }
}
