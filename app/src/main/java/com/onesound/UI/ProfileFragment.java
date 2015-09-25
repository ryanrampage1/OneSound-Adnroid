/**
 *  ProfileFragment.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 8/10/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  Class for the profile fragment
 */

package com.onesound.UI;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.onesound.BaseFragment;
import com.onesound.BuildConfig;
import com.onesound.Cache;
import com.onesound.MyApplication;
import com.onesound.R;
import com.onesound.UI.adapters.FavoritesAdapter;
import com.onesound.managers.UserManager;
import com.onesound.models.Favorite;
import com.onesound.models.RootFavoriteModel;
import com.onesound.networking.APIService;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    private ImageView profilePicture;
    private ImageView background;
    private TextView nameTV;
    private TextView hotness;
    private TextView upvotes;
    private TextView songs;

    private SwipeRefreshLayout mRefreshLayout;
    private View rootView;
    private ListView listView;
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!BuildConfig.DEBUG) {
            Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);
            t.setScreenName("Profile");
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTV = (TextView) rootView.findViewById(R.id.UserName);
        hotness = (TextView) rootView.findViewById(R.id.HotnessScore);
        upvotes = (TextView) rootView.findViewById(R.id.UpVoteScore);
        songs = (TextView) rootView.findViewById(R.id.SongsScore);
        profilePicture = (ImageView) rootView.findViewById(R.id.ProfilePic);
        background = (ImageView) rootView.findViewById(R.id.background);

        setHasOptionsMenu(true);

        listView = (ListView) rootView.findViewById(R.id.favorites_list);
        textView = (TextView) rootView.findViewById(R.id.noFavorites);

        // Set up the refreshing layout
        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.favorites_swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light);

        MainActivity activity = ((MainActivity) getActivity());

        FavoritesAdapter adapter = new FavoritesAdapter(activity, R.layout.favorite_item, new ArrayList<Favorite>());
        listView.setAdapter(adapter);

        onRefresh(false);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    private void updateUI(){

        // set user name
        nameTV.setText(UserManager.INSTANCE.getName());
        hotness.setText(String.valueOf(UserManager.INSTANCE.getHotnessPercent()));
        upvotes.setText(Integer.toString(UserManager.INSTANCE.getVoteCount()));
        songs.setText(String.valueOf(UserManager.INSTANCE.getSongCount()));


        if(UserManager.INSTANCE.isGuest()){
            profilePicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            background.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.gradient_dark));
        }
        else{
            try {
                Glide.with(getActivity())
                        .load(UserManager.INSTANCE.getPhotoURL())
                        .placeholder(R.drawable.defaultuserimageformainparty)
                        .centerCrop()
                        .bitmapTransform(new com.onesound.Utility.RoundedCorner(getActivity(), 90, 0))    // rounded corners
                        .into(profilePicture);
            }catch (Exception e){
                profilePicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            }
            try {
                Glide.with(getActivity())
                        .load(UserManager.INSTANCE.getPhotoURL())
                        .centerCrop()
                        .bitmapTransform(new com.onesound.Utility.Blur(getActivity()))
                        .into(background);
            }catch (Exception e){
                profilePicture.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.gradient_dark));
            }

        }

    }

    @Override
    public void onRefresh() {
        onRefresh(true);
    }

    private void onRefresh(boolean forceRefresh){
        if (!mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(true);
        }

        if(Cache.INSTANCE.getFavorites() == null || forceRefresh || Cache.INSTANCE.isRefreshFavorites()) {
            if (!mRefreshLayout.isRefreshing()) mRefreshLayout.setRefreshing(true);
            APIService.INSTANCE.getFavorites(UserManager.INSTANCE.getID(), GetFavoritesCallback);
            Cache.INSTANCE.setRefreshFavorites(false);
        }
        else {
            ListView lv = (ListView) rootView.findViewById(R.id.favorites_list);
            FavoritesAdapter adapter = (FavoritesAdapter) lv.getAdapter();
            adapter.updateMembers(Cache.INSTANCE.getFavorites());
        }
    }

    private void ToggleFavList(boolean b){
        if (b){
            listView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
        else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    private final Callback<RootFavoriteModel> GetFavoritesCallback = new Callback<RootFavoriteModel>() {
        @Override
        public void success(RootFavoriteModel obj, Response response) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(false);
            }

            if (obj != null && obj.getResults().size() > 0) {
                Cache.INSTANCE.setFavorites((ArrayList<Favorite>) obj.getResults());

                ToggleFavList(true);
                ListView lv = (ListView) rootView.findViewById(R.id.favorites_list);
                FavoritesAdapter adapter = (FavoritesAdapter) lv.getAdapter();
                adapter.updateMembers(obj.getResults());
            }
            else{
                ToggleFavList(false);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.i("Favorites failure", error.toString());
        }
    };
}

