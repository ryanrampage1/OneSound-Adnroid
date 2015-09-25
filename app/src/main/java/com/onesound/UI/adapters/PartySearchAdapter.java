/**
 *  PartySearchAdapter.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onesound.R;
import com.onesound.UI.PartySearchActivity;
import com.onesound.managers.PartyManager;
import com.onesound.models.Party;
import com.onesound.networking.APIService;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PartySearchAdapter extends ArrayAdapter<Party> {
    private PartySearchActivity mContext;
    private List<Party> mResults;

    static class PartySearchViewHolder {
        LinearLayout layout;
        TextView partyName;
        TextView createdBy;
        TextView memberCount;
    }

    public PartySearchAdapter(Context context, int resource, List<Party> results) {
        super(context, resource, results);
        this.mContext = (PartySearchActivity) context;
        this.mResults = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PartySearchViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.party_search_item, parent, false);
            holder = new PartySearchViewHolder();

            holder.layout = (LinearLayout) convertView.findViewById(R.id.SearchItemLayout);
            holder.partyName = (TextView) convertView.findViewById(R.id.PartySearchName);
            holder.createdBy = (TextView) convertView.findViewById(R.id.PartySearchCreator);
            holder.memberCount = (TextView) convertView.findViewById(R.id.PartySearchMemberCount);

            convertView.setTag(holder);
        } else {
            holder = (PartySearchViewHolder) convertView.getTag();
        }

        final Party party = mResults.get(position);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIService.INSTANCE.getRestAdapter().getParty(party.getPID())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Party>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Party p) {
                                if (p != null) {
                                    PartyManager.INSTANCE.SetParty(p);
                                    mContext.onBackPressed();
                                }
                            }
                        });
            }
        });
        holder.partyName.setText(party.getName());
        holder.createdBy.setText("Created by " + party.getHost());
        holder.memberCount.setText(Integer.toString(party.getMemberCount()) + " members");
        return convertView;

    }

}
