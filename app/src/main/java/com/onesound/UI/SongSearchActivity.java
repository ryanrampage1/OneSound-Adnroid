/**
 *  SongSearchActivity.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.onesound.BuildConfig;
import com.onesound.MyApplication;
import com.onesound.R;
import com.onesound.UI.adapters.SongSearchAdapter;
import com.onesound.managers.PartyManager;
import com.onesound.models.SoundCloudSong;
import com.onesound.networking.SoundCloudService;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SongSearchActivity extends AppCompatActivity {

    final Callback<List<SoundCloudSong>> SoundCloudSearchCallback = new Callback<List<SoundCloudSong>>() {
        @Override
        public void success(List<SoundCloudSong> songs, Response response) {
            PartyManager.INSTANCE.setSongSearchResults(songs);

            List<SoundCloudSong> results = new ArrayList<>();
            for (SoundCloudSong s : songs) if (s != null && s.isStreamable()) results.add(s);

            ListView lv = (ListView) findViewById(R.id.party_search_results);
            SongSearchAdapter adapter = new SongSearchAdapter(SongSearchActivity.this, R.layout.song_search_item, results);
            if (lv != null) lv.setAdapter(adapter);
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Tracker t = ((MyApplication) this.getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
            t.setScreenName("SongSearch");
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        setContentView(R.layout.fragment_search);

        ((EditText) findViewById(R.id.party_search_textbox)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3)
                    SoundCloudService.INSTANCE.searchForSong(s.toString(), SoundCloudSearchCallback);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
