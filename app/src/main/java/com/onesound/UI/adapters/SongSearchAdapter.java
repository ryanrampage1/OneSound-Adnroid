/**
 *  SongSearchAdapter.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.onesound.R;
import com.onesound.UI.SongSearchActivity;
import com.onesound.managers.PartyManager;
import com.onesound.models.Song;
import com.onesound.models.SoundCloudSong;
import com.onesound.networking.APIService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SongSearchAdapter extends ArrayAdapter<SoundCloudSong>  {

    private SongSearchActivity mContext;
    private List<SoundCloudSong> mResults;

    static class SongSearchViewHolder {
        ImageView album;
        LinearLayout layout;
        TextView name;
        TextView artist;
        TextView duration;
    }

    public SongSearchAdapter(Context context, int resource, List<SoundCloudSong> results) {
        super(context, resource, results);
        this.mContext = (SongSearchActivity) context;
        this.mResults = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongSearchViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.song_search_item, parent, false);
            holder = new SongSearchViewHolder();

            holder.layout = (LinearLayout) convertView.findViewById(R.id.SongSearchItemLayout);
            holder.name = (TextView) convertView.findViewById(R.id.SongSearchName);
            holder.artist = (TextView) convertView.findViewById(R.id.SongSearchArtist);
            holder.duration = (TextView) convertView.findViewById(R.id.SongSearchDuration);
            holder.album = (ImageView) convertView.findViewById(R.id.albumArt);

            convertView.setTag(holder);
        } else {
            holder = (SongSearchViewHolder) convertView.getTag();
        }

        final SoundCloudSong song = mResults.get(position);

        // Statements to check whether to use album art URL, or use a default one (if the URL is null)
        try {
            if (song.getAlbum() != null && !song.getAlbum().isEmpty())
                Glide.with(this.mContext)
                        .load(song.getAlbum())
                        .placeholder(R.drawable.songcellimageplaceholder)
                        .transform(new com.onesound.Utility.RoundedCorner(this.mContext, 10, 0))
                        .into(holder.album);
            else
                Glide.with(this.mContext)
                        .load(R.drawable.songcellimageplaceholder)
                        .transform(new com.onesound.Utility.RoundedCorner(this.mContext, 10, 0))
                        .into(holder.album);
        }catch (Exception e){
            holder.album.setImageDrawable(mContext.getResources().getDrawable(R.drawable.songcellimageplaceholder));

        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the song to the playlist
                APIService.INSTANCE.addSongToParty(song.convertToSong(), AddSongCallback);

                // add the song to the local playlist
                List<Song> currPlaylist = PartyManager.INSTANCE.getPlaylist();
                if (currPlaylist == null) {
                    currPlaylist = new ArrayList<>();
                    currPlaylist.add(song.convertToSong());
                } else {
                    currPlaylist.add(song.convertToSong());
                }
                PartyManager.INSTANCE.setPlaylist(currPlaylist);
            }
        });
        holder.name.setText(song.getTitle());
        holder.artist.setText("Artist: " + song.getArtist().getUserName());
        int minutes = (int)TimeUnit.MILLISECONDS.toMinutes(song.getLength());
        int seconds = (song.getLength()/1000)%60;
        holder.duration.setText(String.format("%02d:%02d",  minutes, seconds));
        return convertView;

    }

    /**
     * CAllback for add song to playlist. will be either success or a failure
     */
    private final Callback<Object> AddSongCallback = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            mContext.onBackPressed();

            Toast.makeText(mContext, "Added song to party", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e("Add Song Fail", error.toString());
        }
    };

}
