package com.onesound.UI.party;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onesound.BaseFragment;
import com.onesound.R;
import com.onesound.UI.MainActivity;
import com.onesound.UI.SongSearchActivity;
import com.onesound.UI.adapters.PlaylistRecyclerviewAdapter;
import com.onesound.managers.PartyManager;
import com.onesound.managers.UserManager;
import com.onesound.models.RootPlaylistModel;
import com.onesound.models.Song;
import com.onesound.networking.APIService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by tanaysalpekar on 12/22/14.
 */
public class PartyPlaylistFragment extends BaseFragment {

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "PartyPlaylistFragment";
    
    private final Callback<Object> DeleteSongCB = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            // tell the user that the song was succssfully deleted
            Toast.makeText(getActivity(), "Song Deleted", Toast.LENGTH_SHORT).show();

            // update the playlist
            subscribeToPlaylistUpdates();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, error.toString());
        }
    };
    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout full;
    private RelativeLayout empty;
    private RelativeLayout noParty;
    private boolean mFull = false;
    private Subscription playlistSubscription;

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playlistSubscription != null) playlistSubscription.unsubscribe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MainActivity mActivity = ((MainActivity) getActivity());

        rootView = inflater.inflate(R.layout.party_playlist, container, false);

        full = (RelativeLayout) rootView.findViewById(R.id.playlist_full);
        empty = (RelativeLayout) rootView.findViewById(R.id.empty_playlist);
        noParty = (RelativeLayout) rootView.findViewById(R.id.noParty);

        TextView noSongs = (TextView) rootView.findViewById(R.id.searchbutton);
        noSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SongSearchActivity.class));
            }
        });

        final RecyclerView lv = (RecyclerView) rootView.findViewById(R.id.playlist);

        lv.setLongClickable(true);
                // use a linear layout manager
        lv.setLayoutManager(new LinearLayoutManager(getActivity()));
        lv.setItemAnimator(new ScaleInLeftAnimator());

        rootView.findViewById(R.id.fabBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SongSearchActivity.class));
            }
        });


        // Set up the refreshing layout
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.playlist_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light);


        // User is in a party, but the playlist is empty
        if (!UserManager.INSTANCE.inParty()) {
            if (full != null) full.setVisibility(View.GONE);
            if (empty != null) empty.setVisibility(View.GONE);
            if (noParty!=null) noParty.setVisibility(View.VISIBLE);

        }else if (PartyManager.INSTANCE.getPlaylist() == null) {
            ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
            progress.setVisibility(View.VISIBLE);
            if (noParty!=null) noParty.setVisibility(View.GONE);
            PlaylistRecyclerviewAdapter adapter = new PlaylistRecyclerviewAdapter(mActivity, new ArrayList<Song>());
            lv.setAdapter(adapter);

        } else if (PartyManager.INSTANCE.getPlaylist().size() == 0 && !mFull) {
            if (full != null) full.setVisibility(View.GONE);
            if (empty != null) empty.setVisibility(View.VISIBLE);
            if (noParty!=null) noParty.setVisibility(View.GONE);

        } else {
            // User is in a party and there is a playlist with songs in it
            mFull = true;
            if (full != null) full.setVisibility(View.VISIBLE);
            if (empty != null) empty.setVisibility(View.GONE);
            if (noParty!=null) noParty.setVisibility(View.GONE);

            PlaylistRecyclerviewAdapter adapter = new PlaylistRecyclerviewAdapter(mActivity, PartyManager.INSTANCE.getPlaylist());
            lv.setAdapter(adapter);
        }


        return rootView;
    }

    @Override
    public void onRefresh() {
        // Set the refreshing animation since the event was called
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        if (full != null) full.setVisibility(View.VISIBLE);
        if (UserManager.INSTANCE.inParty()) subscribeToPlaylistUpdates();
    }

    private void subscribeToPlaylistUpdates() {
        if (playlistSubscription != null && !playlistSubscription.isUnsubscribed()) playlistSubscription.unsubscribe();

            playlistSubscription = Observable.interval(0, 15, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<RootPlaylistModel>>() {
                    @Override
                    public Observable<RootPlaylistModel> call(Long aLong) {
                        return APIService.INSTANCE.getRestAdapter().getPartyPlaylist(PartyManager.INSTANCE.getPID());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<RootPlaylistModel>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.toString());
                            }

                            @Override
                            public void onNext(RootPlaylistModel playlistModel) {
                                handlePlaylistCallback(playlistModel);
                            }
                        });

    }


    private void handlePlaylistCallback(RootPlaylistModel rootPlaylistModel){
        if (rootPlaylistModel != null) {

            // update model
            PartyManager.INSTANCE.setPlaylist(rootPlaylistModel.getResults());
            try {
                final RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.playlist);
                PlaylistRecyclerviewAdapter adapter = (PlaylistRecyclerviewAdapter) rv.getAdapter();
                adapter.setPlaylist(rootPlaylistModel.getResults());
                adapter.notifyDataSetChanged();
            } catch (NullPointerException e) {
                Log.d("Set playlist adapter", e.toString());
            }

            // if the returned object has no elements, set the screen to no songs
            // if only one object was returned and the next song is null it should
            // also display no songs in the playlist
            // || (obj.getResults().size() == 1 && PartyManager.INSTANCE.getCurrentSong() == null)
            if (rootPlaylistModel.getResults().size() == 0) {
                if (full != null) full.setVisibility(View.GONE);
                if (empty != null) empty.setVisibility(View.VISIBLE);
                if (noParty!=null) noParty.setVisibility(View.GONE);
                mFull = false;
            } else {
                if (full != null) full.setVisibility(View.VISIBLE);
                if (empty != null) empty.setVisibility(View.GONE);
                if (noParty!=null) noParty.setVisibility(View.GONE);
                mFull = true;
            }
        }

        try {
            // hide the progress bar
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        } catch (NullPointerException e) {
            Log.d("hide progress bar", e.toString());
        }

        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
