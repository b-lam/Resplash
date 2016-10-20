package io.github.b_lam.resplash.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.Activities.UserActivity;
import io.github.b_lam.resplash.Data.Data.SearchUsersResult;
import io.github.b_lam.resplash.Data.Data.User;
import io.github.b_lam.resplash.Data.Service.SearchService;
import io.github.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;
import tr.xip.errorview.ErrorView;

public class SearchUserFragment extends Fragment {

    private String TAG = "SearchUserFragment";
    private SearchService mService;
    private FastItemAdapter<User> mUserAdapter;
    private SearchUsersResult mSearchUserResult;
    private List<User> mUsers;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private TextView mNoResultTextView;
    private FooterAdapter<ProgressItem> mFooterAdapter;
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
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_search_user_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_search_user_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_search_user_error_view);
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerSearchUser);
        mNoResultTextView = (TextView) rootView.findViewById(R.id.user_no_results);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mImageRecycler.setItemViewCacheSize(20);
        mUserAdapter = new FastItemAdapter<>();

        mUserAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new FooterAdapter<>();

        mImageRecycler.setAdapter(mFooterAdapter.wrap(mUserAdapter));

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                mFooterAdapter.add(new ProgressItem().withEnabled(false));
                loadMore();
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                loadMore();
            }
        });

        loadMore();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }
    }

    private FastAdapter.OnClickListener<User> onClickListener = new FastAdapter.OnClickListener<User>(){
        @Override
        public boolean onClick(View v, IAdapter<User> adapter, User item, int position) {
            Intent intent = new Intent(getContext(), UserActivity.class);
            intent.putExtra("username", item.username);
            intent.putExtra("name", item.name);
            startActivity(intent);
            return false;
        }
    };

    public void updateAdapter(List<User> users) {
        mUserAdapter.add(users);
    }

    public void loadMore(){
        if(mUsers == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
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
                    mImagesErrorView.setVisibility(View.GONE);
                    if(mUserAdapter.getItemCount() == 0){
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultTextView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mImagesErrorView.setTitle(R.string.error_http);
                    mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                }
                if(mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getContext(), "Updated images!", Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestUsersFailed(Call<SearchUsersResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.showRetryButton(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchUsers(mQuery, mPage, mUserRequestListener);
            mNoResultTextView.setVisibility(View.GONE);
        }
    }

}
