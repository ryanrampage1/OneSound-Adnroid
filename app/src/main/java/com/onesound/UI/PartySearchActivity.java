/**
 *  SerchFragment.Java
 *  OneSound
 *
 *  Created by Ryan Casler on 8/10/14.
 *  Copyright (c) 2014 OneSound LLC. All rights reserved.
 *
 *  This class is the fragment for the search tab
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
import com.onesound.UI.adapters.PartySearchAdapter;
import com.onesound.models.Party;
import com.onesound.networking.APIService;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PartySearchActivity extends AppCompatActivity {

    public PartySearchActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Tracker t = ((MyApplication) this.getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
            t.setScreenName("PartySearch");
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        setContentView(R.layout.fragment_search);

        EditText mTextBox = (EditText) findViewById(R.id.party_search_textbox);

        mTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                APIService.INSTANCE.getRestAdapter().searchForParty(s.toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Subscriber<List<Party>>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(List<Party> parties) {
                                        ListView lv = (ListView) findViewById(R.id.party_search_results);
                                        PartySearchAdapter adapter = new PartySearchAdapter(PartySearchActivity.this, R.layout.party_search_item, parties);
                                        lv.setAdapter(adapter);
                                    }
                                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}

