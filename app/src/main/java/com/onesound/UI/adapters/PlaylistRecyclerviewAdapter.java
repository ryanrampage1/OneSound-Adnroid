/**
 *  PlaylistRecylerviewAdapter.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.onesound.Cache;
import com.onesound.R;
import com.onesound.UI.listeners.VoteClickListener;
import com.onesound.models.Song;
import com.onesound.networking.APIService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PlaylistRecyclerviewAdapter  extends RecyclerView.Adapter<PlaylistRecyclerviewAdapter.SongViewHolder> {

    private Context mContext;
    private List<Song> playlist;

    public PlaylistRecyclerviewAdapter(Context context, List<Song> playlist) {
        this.mContext = context;
        this.playlist = playlist;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.PlaylistAlbum)ImageView album;
        @Bind(R.id.ThumbsDownUnsel)ImageView thumbs_down;
        @Bind(R.id.ThumbsUpUnsel)ImageView thumbs_up;
        @Bind(R.id.PlaylistSongTitle)TextView song_title;
        @Bind(R.id.PlaylistArtist)TextView artist;
        @Bind(R.id.favorite)ImageView favorite;

        public SongViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public PlaylistRecyclerviewAdapter.SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_2, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SongViewHolder holder, int position) {
        final Song song = playlist.get(position);

        holder.song_title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        // Statements to check whether to use album art URL, or use a default one (if the URL is null)
        try {
            if (song.getAlbum() != null && !song.getAlbum().isEmpty())
                Glide.with(this.mContext)
                        .load(song.getAlbum())
                        .placeholder(R.drawable.songcellimageplaceholder)
                        .into(holder.album);

            else
                Glide.with(this.mContext)
                        .load(R.drawable.songcellimageplaceholder)
                        .into(holder.album);

        }catch (Exception e){
            holder.album.setImageDrawable(mContext.getResources().getDrawable(R.drawable.songcellimageplaceholder));
        }

        // Preloads the song cell with a downvote since the user has voted down on it
        if (song.getUserVote() == -1) {
            holder.thumbs_up.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_up_unsel));
            holder.thumbs_down.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_down_sel));

        }
        // Preloads the song cell with an upvote since the user has voted up on it
        else if (song.getUserVote() == 1) {
            holder.thumbs_up.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_up_sel));
            holder.thumbs_down.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_down_unsel));

        } else if (song.getUserVote() == 0) {
            holder.thumbs_up.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_up_unsel));
            holder.thumbs_down.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_thumb_down_unsel));
        }

        VoteClickListener voter = new VoteClickListener(mContext, song, holder.thumbs_up, holder.thumbs_down);
        holder.thumbs_up.setOnClickListener(voter);
        holder.thumbs_down.setOnClickListener(voter);

        if (song.isFavorited()) holder.favorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_full));
        else  holder.favorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star));

        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIService.INSTANCE.getRestAdapter().addFavorite(song.getSID())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() { }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e("Playlist adapter", e.toString());
                                    }

                                    @Override
                                    public void onNext(Object statusModel) {
                                        holder.favorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_full));
                                        Cache.INSTANCE.setRefreshFavorites(true);
                                    }
                                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public PlaylistRecyclerviewAdapter setPlaylist(List<Song> playlist) {
        this.playlist = playlist;
        return this;
    }
}
