package com.freevar.dswami.hw9;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.HashSet;
import java.util.Set;

public class FavoriteAdapter extends FragmentPagerAdapter {

    private long baseId = 0;
    private Context context;
    private Set<String> set;

    public FavoriteAdapter(Context context, FragmentManager manager) {

        super(manager);
        this.context=context;

        SharedPreferences sharedPreferences = this.context.getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        this.set = sharedPreferences.getStringSet("favorite_set", null);
        if(this.set==null){ this.set = new HashSet<>(); }
    }

    @Override
    // Position 0 returns current location
    public Fragment getItem(int position) {
        return FavoriteFragment.newInstance(position-1);
    }

    @Override
    public int getCount() {
        return this.set.size()+1;
    }


    //this is called when notifyDataSetChanged() is called
    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PagerAdapter.POSITION_NONE;
    }


    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }


    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {

        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getCount() + n;

        // Refresh favorites from Shared Preferences
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        this.set = sharedPreferences.getStringSet("favorite_set", null);
        if(this.set==null){ this.set = new HashSet<>(); }
    }

    @Override
    public void notifyDataSetChanged() {
        this.notifyChangeInPosition(1);
        super.notifyDataSetChanged();
    }
}
