package com.Pull.pullapp;

import java.util.Random;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.Pull.pullapp.adapter.SignInFragmentAdapter;
import com.viewpagerindicator.PageIndicator;

public abstract class BaseActivity extends FragmentActivity {
    private static final Random RANDOM = new Random();

    SignInFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;


}