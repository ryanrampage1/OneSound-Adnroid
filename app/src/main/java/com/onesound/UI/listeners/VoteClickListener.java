/**
 *  VoteClickListener.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 * This class defines a VoteClick listener.
 * Handles both thumbs up and thumbs down clicks
 *
 */
package com.onesound.UI.listeners;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.onesound.R;
import com.onesound.models.Song;
import com.onesound.networking.APIService;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class VoteClickListener implements View.OnClickListener {

    /**
     * The Song that is being clicked
     */
    private Song song;
    /**
     * Reference to main activity
     */
    private Context mainActivity;
    /**
     * Thumbs up image
     */
    private ImageView thumbsUp;
    /**
     * Thumbs down image
     */
    private ImageView thumbsDown;
    /**
     * Flag for thubs up selected
     */
    private boolean thumbsUpSelected;
    /**
     * Flag for thubs down selected
     */
    private boolean thumbsDownSelected;

    private static final String TAG = "VoteClickListener";

    /**
     * Constructor for a vote click listener
     * @param activity reference to the main activity
     * @param song that is being clicked
     * @param tu thumbs up imageview
     * @param td thumbs down imageview
     */
    public VoteClickListener(Context activity, Song song, ImageView tu, ImageView td) {
        this.song = song;
        mainActivity = activity;
        thumbsUp = tu;
        thumbsDown = td;
        if (song != null) {
            thumbsUpSelected = song.getUserVote() == 1;
            thumbsDownSelected = song.getUserVote() == -1;
        }
    }

    /**
     * onClick function for both buttons
     *
     * This function will toggle the selected icon and make any network calls necessary
     * @param v the type of view that was clicked
     */
    @Override
    public void onClick(View v) {
        if (v.equals(thumbsUp)) {
            if (thumbsDownSelected || (!thumbsUpSelected)) {
                // Thumbsdown should be toggled off since thumbs up was just pressed
                thumbsDownSelected = false;
                thumbsDown.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_down_unsel));

                // Thumbs up has now been selected
                thumbsUpSelected = true;
                thumbsUp.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_sel));
                if(song != null) {
                    upVote(song.getSID());
                }
            } else {
                // If thumbs up had been selected before, then toggle it off
                thumbsUpSelected = false;
                thumbsUp.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_unsel));
                if(song != null) APIService.INSTANCE.removeVote(RemoveUpVoteCallback, song.getSID());
            }


        } else if (v.equals(thumbsDown) ) {
            if (thumbsUpSelected || (!thumbsDownSelected)) {
                thumbsUpSelected = false;
                thumbsUp.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_unsel));

                thumbsDownSelected = true;
                thumbsDown.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_down_sel));
                if(song != null) downVote(song.getSID());
            } else {
                // If thumbs up had been selected before, then toggle it off
                thumbsDownSelected = false;
                thumbsDown.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_down_unsel));
                if(song != null) APIService.INSTANCE.removeVote(RemoveDownVoteCallback, song.getSID());
            }


        }
    }

    private void upVote(int sid){
        APIService.INSTANCE.getRestAdapter().upVoteSong(sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        // load the not selected up vote icon
                        thumbsUp.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_unsel));
                        Log.i(TAG, e.toString());
                    }

                    @Override
                    public void onNext(Object p) {
                        song.setUserVote(1);
                    }
                });
    }

    private void downVote(int sid){
        APIService.INSTANCE.getRestAdapter().upVoteSong(sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        song.setUserVote(-1);
                    }
                });
    }

    /**
     * Callback for removing a up vote
     */
    private final Callback<Object> RemoveUpVoteCallback = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            // reset the user vote
            song.setUserVote(0);
        }

        @Override
        public void failure(RetrofitError error) {
            // removing the vote failed so set the thumbs up image
            thumbsUp.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_up_sel));
            Log.i("VoteClickListener", error.toString());
        }
    };

    /**
     * Callback for removing a down vote
     */
    private final Callback<Object> RemoveDownVoteCallback = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            // reset the user vote
            song.setUserVote(0);
        }

        @Override
        public void failure(RetrofitError error) {
            // removing the vote failed set the image back to down
            thumbsDown.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_thumb_down_sel));
            Log.i("VoteClickListener", error.toString());
        }
    };
}
