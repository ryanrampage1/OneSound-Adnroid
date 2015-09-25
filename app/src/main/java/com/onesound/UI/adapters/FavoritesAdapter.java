package com.onesound.UI.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.onesound.R;
import com.onesound.UI.MainActivity;
import com.onesound.managers.UserManager;
import com.onesound.models.Favorite;
import com.onesound.models.SoundCloudSong;
import com.onesound.networking.APIService;
import com.onesound.networking.SoundCloudService;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by ryan on 4/25/15.
 * <p/>
 * Listview adapter for the a list of favorite objects
 */
public class FavoritesAdapter extends BaseAdapter {

    /**
     * Reference to the main activity for context to get images as well as get the layout inflater
     */
    private MainActivity context;
    private final Callback<Object> AddSongCallback = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            Toast.makeText(context, "Song added to party", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.i("Favorites Adapter", error.toString());
        }
    };
    /**
     * The list of favorites that is being displayed
     */
    private List<Favorite> favorites;

    public FavoritesAdapter(MainActivity context, @SuppressWarnings("SameParameterValue") int resource, List<Favorite> favorites) {
        //super(context, resource, members);
        this.context = context;
        this.favorites = favorites;
    }

    /**
     * Update the list in the adapter
     *
     * @param favorites the new list of favorites
     */
    public void updateMembers(List<Favorite> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return favorites.size();
    }

    @Override
    public Object getItem(int position) {
        return favorites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FavoritesViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.favorite_item, parent, false);
            holder = new FavoritesViewHolder();
            holder.songName = (TextView) convertView.findViewById(R.id.SongName);
            holder.artistName = (TextView) convertView.findViewById(R.id.Artist);
            holder.albumArt = (ImageView) convertView.findViewById(R.id.AlbumArt);
            holder.addToPlaylist = (ImageView) convertView.findViewById(R.id.AddToPlaylist);

            convertView.setTag(holder);
        } else {
            holder = (FavoritesViewHolder) convertView.getTag();
        }

        final Favorite favorite = favorites.get(position);

        holder.songName.setText(favorite.getTitle());
        holder.artistName.setText(favorite.getArtist());

        if (favorite.getAlbumArtURL() == null || favorite.getAlbumArtURL().isEmpty()) {
            holder.albumArt.setImageDrawable(context.getResources().getDrawable(R.drawable.songimagefornosongartwork));
        } else {
            try {
                Glide.with(this.context)
                        .load(favorite.getAlbumArtURL())
                        .placeholder(R.drawable.songimagefornosongartwork)
                        .into(holder.albumArt);
            } catch (Exception e) {
                holder.albumArt.setImageDrawable(context.getResources().getDrawable(R.drawable.songimagefornosongartwork));
            }
        }

        if (!UserManager.INSTANCE.inParty()) {
            // if user isnt in a party, hide the add to playlist button
            holder.addToPlaylist.setVisibility(View.GONE);
        } else {
            holder.addToPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // user wants to add the song to the playlist so we need to check that it
                    // still exists on soundcloud, to do so search for the song by title then
                    // confirm the external ids match
                    Callback<List<SoundCloudSong>> SoundCloudSearchCallback = new Callback<List<SoundCloudSong>>() {
                        @Override
                        public void success(List<SoundCloudSong> songs, Response response) {
                            if (!songs.isEmpty() && songs.get(0).getExternalID() == Integer.parseInt(favorite.getExternalID())) {
                                // songs match so add the result to the playlist
                                APIService.INSTANCE.addSongToParty(songs.get(0).convertToSong(), AddSongCallback);
                            } else {
                                // song didnt match so dont add it =
                                Toast.makeText(context, "Could not add song", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    };

                    // search for the soundcloud song based on the title
                    SoundCloudService.INSTANCE.searchForFavorite(favorite.getTitle(), SoundCloudSearchCallback);
                }
            });
        }
        return convertView;
    }

    /**
     * Class for the Viewholder pattern
     */
    static class FavoritesViewHolder {
        TextView songName;
        TextView artistName;
        ImageView albumArt;
        ImageView addToPlaylist;
    }

}
