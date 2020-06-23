package com.example.send.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    ViewPager viewPager;
    SendFragment sendFragment;
    ReceiveFragment receiveFragment;

    public ViewPagerAdapter(@NonNull FragmentManager fm, ViewPager viewPager) {
        super(fm);
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position==0){
            receiveFragment = new ReceiveFragment();
            return receiveFragment;
        }
        sendFragment = new SendFragment();
        return sendFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position==0)
            return "receive";
        return "send";
    }
}
