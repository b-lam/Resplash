package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.UserActivity;
import com.b_lam.resplash.data.model.SearchUsersResult;
import com.b_lam.resplash.data.model.User;
import com.b_lam.resplash.data.service.SearchService;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener;
import com.mikepenz.fastadapter.ui.items.ProgressItem;

import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;


public class SearchUserFragment extends Fragment {

    private String TAG = "SearchUserFragment";
    private SearchService mService;
    private FastItemAdapter<User> mUserAdapter;
    private SearchUsersResult mSearchUserResult;
    private List<User> mUsers;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
    private ConstraintLayout mNoResultView;
    private ItemAdapter mFooterAdapter;
    private int mPage;
    private String mQuery;

    public SearchUserFragment() {
    }

    public static SearchUserFragment newInstance(String query) {
        SearchUserFragment userFragment = new SearchUserFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        userFragment.setArguments(args);

        return userFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mQuery = getArguments().getString("query", null);

        mService = SearchService.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ButterKnife.bind(getActivity());

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_search_user, container, false);
        mImageRecycler = rootView.findViewById(R.id.fragment_search_user_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_search_user_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerSearchUser);
        mNoResultView = rootView.findViewById(R.id.no_results_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener((v, event) -> false);
        mImageRecycler.setItemViewCacheSize(5);
        mUserAdapter = new FastItemAdapter<>();

        mUserAdapter.setOnClickListener((v, adapter, item, position) -> {
            Intent intent = new Intent(getContext(), UserActivity.class);
            intent.putExtra("username", item.username);
            intent.putExtra("name", item.name);
            startActivity(intent);
            return false;
        });

        mFooterAdapter = new ItemAdapter();

        mUserAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mUserAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                ProgressItem progressItem = new ProgressItem();
                progressItem.setEnabled(false);
                mFooterAdapter.add(progressItem);
                loadMore();
            }
        });

        mSwipeContainer.setOnRefreshListener(this::fetchNew);

        fetchNew();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }
    }

    private void updateAdapter(List<User> users) {
        mUserAdapter.add(users);
    }

    public void loadMore(){
        if(mUsers == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        SearchService.OnRequestUsersListener mUserRequestListener = new SearchService.OnRequestUsersListener() {
            @Override
            public void onRequestUsersSuccess(Call<SearchUsersResult> call, Response<SearchUsersResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchUserResult = response.body();
                    mUsers = mSearchUserResult.results;
                    mFooterAdapter.clear();
                    SearchUserFragment.this.updateAdapter(mUsers);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                    if(mUserAdapter.getItemCount() == 0){
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRequestUsersFailed(Call<SearchUsersResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchUsers(mQuery, mPage, 30, mUserRequestListener);
            mNoResultView.setVisibility(View.GONE);
        }
    }

    private void fetchNew(){
        if(mUsers == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        final SearchService.OnRequestUsersListener mUserRequestListener = new SearchService.OnRequestUsersListener() {
            @Override
            public void onRequestUsersSuccess(Call<SearchUsersResult> call, Response<SearchUsersResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchUserResult = response.body();
                    mUsers = mSearchUserResult.results;
                    mUserAdapter.clear();
                    SearchUserFragment.this.updateAdapter(mUsers);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                    if(mUserAdapter.getItemCount() == 0){
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
                if(mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getContext(), getString(R.string.updated_users), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestUsersFailed(Call<SearchUsersResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchUsers(mQuery, mPage, Resplash.DEFAULT_PER_PAGE, mUserRequestListener);
            mNoResultView.setVisibility(View.GONE);
        }
    }

}
