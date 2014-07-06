package com.Pull.pullapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.Pull.pullapp.R;
import com.Pull.pullapp.R.drawable;
import com.Pull.pullapp.fragment.SignInFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class SignInFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "SHARE CONVERSATIONS",};
    protected static final int[] ICONS = new int[] {
        R.drawable.conversations,
};

    private int mCount = CONTENT.length;

    public SignInFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return SignInFragment.newInstance(CONTENT[position % CONTENT.length], getIconResId(position));
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return SignInFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    @Override
    public int getIconResId(int index) {
      return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}