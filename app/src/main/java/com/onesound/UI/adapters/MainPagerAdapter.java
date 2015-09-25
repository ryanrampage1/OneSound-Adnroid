package com.onesound.UI.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.onesound.UI.ProfileFragment;
import com.onesound.UI.party.PartyMemberFragment;
import com.onesound.UI.party.PartyPlaylistFragment;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ryan on 8/18/15.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private String[] mTabs = {"Playlist", "Members", "Profile"};

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        fragments.add(new PartyPlaylistFragment());
        fragments.add(new PartyMemberFragment());
        fragments.add(new ProfileFragment());
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale.getDefault();
        switch (position) {
            case 0:
                return mTabs[0];
            case 1:
                return mTabs[1];
            case 2:
                return mTabs[2];
        }
        return null;    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public ArrayList<Fragment> getFragments() {return fragments;}
}
