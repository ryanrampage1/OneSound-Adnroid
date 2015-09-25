/**
 *  NearbyResultsAdapter.Java
 *  OneSound
 *
 *  Copyright (c) 2015 OneSound LLC. All rights reserved.
 *
 */
package com.onesound.UI.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onesound.R;
import com.onesound.UI.NearPartiesActivity;
import com.onesound.models.Party;

import java.util.List;

import de.greenrobot.event.EventBus;


public class NearbyResultAdapter  extends ArrayAdapter<Party> {
    /**
     * reference to a nearParty activity
     */
    private NearPartiesActivity mContext;
    /**
     * The returned list of parties
     */
    private List<Party> results;

    /**
     * Modle class for each object in the list
     */
    static class PartySearchViewHolder {
        RelativeLayout layout;
        TextView name;
        TextView host;
        TextView memberCount;
        TextView distance;
    }

    /**
     * Constuctor for a NearbyResultAdapter
     * @param context reference to a NearPartiesActivity activity
     * @param resource needed for the super call
     * @param results The returned list of parties that is used to populate the list
     */
    public NearbyResultAdapter(NearPartiesActivity context, int resource, List<Party> results) {
        super(context, resource, results);
        this.mContext =  context;
        this.results = results;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PartySearchViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.nearby_item, parent, false);
            holder = new PartySearchViewHolder();

            holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout);
            holder.name = (TextView) convertView.findViewById(R.id.party_name);
            holder.host = (TextView) convertView.findViewById(R.id.host_name);
            holder.memberCount = (TextView) convertView.findViewById(R.id.user_count);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);

            convertView.setTag(holder);
        } else {
            holder = (PartySearchViewHolder) convertView.getTag();
        }

        final Party party = results.get(position);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(party.getPID());
//                APIService.INSTANCE.getPartyFromSearch(party.getPID(), callback);
            }
        });
        holder.name.setText(party.getName());
        holder.host.setText(party.getHost());
        holder.memberCount.setText(Integer.toString(party.getMemberCount()) + " people");
        holder.distance.setText(Double.toString(party.getDistance()) + " miles");
        return convertView;
    }

}