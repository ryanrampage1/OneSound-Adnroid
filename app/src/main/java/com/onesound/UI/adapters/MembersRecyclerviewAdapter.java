package com.onesound.UI.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.onesound.R;
import com.onesound.models.User;

import java.util.List;

/**
 * Created by ryan on 8/29/15.
 */
public class MembersRecyclerviewAdapter  extends RecyclerView.Adapter<MembersRecyclerviewAdapter.MemberViewHolder> {

    private Context context;
    private List<User> members;

    public MembersRecyclerviewAdapter(Context context, List<User> members) {
        this.context = context;
        this.members = members;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView hotnessScore;
        TextView upvoteScore;
        TextView songScore;
        TextView userName;
//        ImageView color;
        View background;

        public MemberViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public MembersRecyclerviewAdapter.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item_2, parent, false);

        MemberViewHolder vh = new MemberViewHolder(v);
        vh.hotnessScore = (TextView) v.findViewById(R.id.MembersHotnessScore);
        vh.songScore = (TextView) v.findViewById(R.id.MembersSongScore);
//        vh.upvoteScore = (TextView) v.findViewById(R.id.MembersUpVoteScore);
        vh.icon = (ImageView) v.findViewById(R.id.MembersIcon);
        vh.userName = (TextView) v.findViewById(R.id.MembersUserName);
//        vh.color = (ImageView) v.findViewById(R.id.color);
        vh.background = v.findViewById(R.id.cardBackground);

        return vh;
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        final User member = members.get(position);

        holder.hotnessScore.setText(Integer.toString(member.getHotnessPercent()));
        holder.songScore.setText(Integer.toString(member.getSongCount()));
//        holder.upvoteScore.setText(Integer.toString(member.getVoteCount()));
        holder.userName.setText(member.getName());

//                        holder.background.setBackgroundColor(context.getResources().getColor(R.color.transp_white_50));

        // set background of the card to the users color
        switch (member.getColorSelected()){
            case Yellow:
                holder.background.setBackgroundColor(context.getResources().getColor(R.color.user_yellow));

//                holder.color.setImageDrawable(context.getResources().getDrawable(R.drawable.yellow_triangle));
                break;
            case Orange:
                holder.background.setBackgroundColor(context.getResources().getColor(R.color.user_orange));

//                holder.color.setImageDrawable(context.getResources().getDrawable(R.drawable.orange_triangle));
                break;
            case Purple:
                holder.background.setBackgroundColor(context.getResources().getColor(R.color.user_purple));

//                holder.color.setImageDrawable(context.getResources().getDrawable(R.drawable.purple_triangle));
                break;
            case Terqouise:
                holder.background.setBackgroundColor(context.getResources().getColor(R.color.user_terqouise));

//                holder.color.setImageDrawable(context.getResources().getDrawable(R.drawable.green_triangle));
                break;
            case Red:
                holder.background.setBackgroundColor(context.getResources().getColor(R.color.user_red));

//                holder.color.setImageDrawable(context.getResources().getDrawable(R.drawable.red_triangle));
                break;
        }

        if (member.isGuest()){
            try {
                Glide.with(this.context).load(R.drawable.defaultuserimageformainparty)
//                        .transform(new com.onesound.Utility.RoundedCorner(context, 120, 0))
                        .into(holder.icon);

            } catch (Exception e) {
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            }
        } else {
            try {
                Glide.with(this.context).load(member.getPhotoURL())
                        .placeholder(R.drawable.defaultuserimageformainparty)
//                        .transform(new com.onesound.Utility.RoundedCorner(context, 95, 0))
                        .into(holder.icon);

            } catch (Exception e) {
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.defaultuserimageformainparty));
            }
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<User> members) {
        this.members = members;
        notifyDataSetChanged();
    }
}
