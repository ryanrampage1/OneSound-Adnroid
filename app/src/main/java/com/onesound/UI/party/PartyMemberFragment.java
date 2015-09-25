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

import com.onesound.BaseFragment;
import com.onesound.R;
import com.onesound.UI.SongSearchActivity;
import com.onesound.UI.adapters.MembersRecyclerviewAdapter;
import com.onesound.managers.PartyManager;
import com.onesound.managers.UserManager;
import com.onesound.models.RootMemberModel;
import com.onesound.models.User;
import com.onesound.networking.APIService;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tanaysalpekar on 12/22/14.
 * members fragment that displays members in a party
 */
public class PartyMemberFragment extends BaseFragment {

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "PartyMemberFragment";
    @Bind(R.id.members_swipe_refresh_layout) SwipeRefreshLayout refreshLayout;
    @Bind(R.id.members_list) RecyclerView recyclerView;
    @Bind(R.id.membersLayout) RelativeLayout membersLayout;
    @Bind(R.id.noParty) RelativeLayout noParty;
    @Bind(R.id.progressBar) ProgressBar progressBar;

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.party_members, container, false);
        ButterKnife.bind(this, view);

        // Set up the refreshing layout
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light);

        // set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setContent();

        return view;
    }

    @OnClick(R.id.fabBtn)
    public void fabClick() {
        startActivity(new Intent(getActivity(), SongSearchActivity.class));
    }
    /**
     * Force an update of the party members fragment
     */
    public void getMembers() {
        compositeSubscription.add(APIService.INSTANCE.getRestAdapter().getPartyMembers(PartyManager.INSTANCE.getPID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RootMemberModel>() {
                               @Override
                               public void onCompleted() {

                               }

                               @Override
                               public void onError(Throwable e) {

                               }

                               @Override
                               public void onNext(RootMemberModel rootMemberModel) {
                                   if (PartyManager.INSTANCE.getMembers() == null)
                                       try {
                                           progressBar.setVisibility(View.GONE);
                                       } catch (NullPointerException e) {
                                           Log.e(TAG, e.toString());
                                       }

                                   // Disable the refreshing animation since the callback function has been completed
                                   refreshLayout.setRefreshing(false);

                                   if (rootMemberModel != null) {
                                       PartyManager.INSTANCE.setMembers(rootMemberModel.getResults());
                                       setContent();
                                   }
                               }

                           }));
    }

    /**
     * Set the content of the members fragment based on the the user and party manager data
     */
    private void setContent() {
        if (!UserManager.INSTANCE.inParty()) {
            noParty.setVisibility(View.VISIBLE);
            membersLayout.setVisibility(View.GONE);

        } else {
            noParty.setVisibility(View.GONE);
            membersLayout.setVisibility(View.VISIBLE);

            if (PartyManager.INSTANCE.getMembers() == null) {
                progressBar.setVisibility(View.VISIBLE);
                MembersRecyclerviewAdapter adapter = new MembersRecyclerviewAdapter(getActivity(), new ArrayList<User>());
                recyclerView.setAdapter(adapter);

            } else {
                MembersRecyclerviewAdapter adapter = new MembersRecyclerviewAdapter(getActivity(), PartyManager.INSTANCE.getMembers());
                recyclerView.setAdapter(adapter);

            }
        }
    }

    @Override
    public void onRefresh() {
        // Set the refreshing animation since the event was called
        if (refreshLayout != null && !refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(true);
        }

        if (UserManager.INSTANCE.inParty()) getMembers();
    }

}
