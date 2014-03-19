package com.siteknows.museum.adapter;

import com.siteknows.museum.InfoFragment;
import com.siteknows.museum.OnSiteFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
	Bundle savedInstanceState;

	public TabsPagerAdapter(FragmentManager fm, Bundle savedInstanceState) {
		super(fm);
		this.savedInstanceState = savedInstanceState;
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
		// Top Rated fragment activity
		{
			Fragment fragment = new OnSiteFragment();
			fragment.setArguments(savedInstanceState);
			return fragment;
		}
		case 1:
			// Games fragment activity
			return new InfoFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}

}
