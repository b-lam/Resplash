package com.b_lam.resplash.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.SearchPhotosResult;
import com.b_lam.resplash.data.service.SearchService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;
import tr.xip.errorview.ErrorView;

public class SearchPhotoFragment extends Fragment {

    private String TAG = "SearchPhotoFragment";
    private SearchService mService;
    private FastItemAdapter<Photo> mPhotoAdapter;
    private SearchPhotosResult mSearchPhotosResult;
    private List<Photo> mPhotos;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private TextView mNoResultTextView;
    private ItemAdapter mFooterAdapter;
    private int mPage, mColumns;
    private String mQuery;
    private SharedPreferences sharedPreferences;

    public SearchPhotoFragment() {
    }

    public static SearchPhotoFragment newInstance(String query) {
        SearchPhotoFragment photoFragment = new SearchPhotoFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        String mLayoutType = sharedPreferences.getString("item_layout", "List");
        if(mLayoutType.equals("List") || mLayoutType.equals("Cards")){
            mColumns = 1;
        }else{
            mColumns = 2;
        }

        mQuery = getArguments().getString("query", null);

        mService = SearchService.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_search_photo, container, false);
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_search_photo_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_search_photo_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_search_photo_error_view);
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerSearchPhoto);
        mNoResultTextView = (TextView) rootView.findViewById(R.id.photo_no_results);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mImageRecycler.setItemViewCacheSize(5);
        mPhotoAdapter = new FastItemAdapter<>();

        mPhotoAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter();

        mPhotoAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mPhotoAdapter);

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
                fetchNew();
            }
        });

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

    private OnClickListener<Photo> onClickListener = new OnClickListener<Photo>(){
        @Override
        public boolean onClick(View v, IAdapter<Photo> adapter, Photo item, int position) {
            Intent i = new Intent(getContext(), DetailActivity.class);
            i.putExtra("Photo", new Gson().toJson(item));

            String layout = sharedPreferences.getString("item_layout", "List");

            ImageView imageView;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || sharedPreferences.getString("item_layout", "List").equals("Grid")) {
                startActivity(i);
            } else if (layout.equals("Cards")) {
                imageView = (ImageView) v.findViewById(R.id.item_image_card_img);
                if (imageView.getDrawable() != null)
                    Resplash.getInstance().setDrawable(imageView.getDrawable());
                startActivity(i);
            } else {
                imageView = (ImageView) v.findViewById(R.id.item_image_img);
                if (imageView.getDrawable() != null)
                    Resplash.getInstance().setDrawable(imageView.getDrawable());
//                v.setTransitionName("photoScale");
//                Pair<View, String> p1 = Pair.create(v, v.getTransitionName());
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p1);
//                startActivity(i, options.toBundle());
                startActivity(i);
            }
            return false;
        }
    };

    public void updateAdapter(List<Photo> photos) {
        mPhotoAdapter.add(photos);
    }

    public void loadMore(){
        if(mPhotos == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        SearchService.OnRequestPhotosListener mPhotoRequestListener = new SearchService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<SearchPhotosResult> call, Response<SearchPhotosResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchPhotosResult = response.body();
                    mPhotos = mSearchPhotosResult.results;
                    mFooterAdapter.clear();
                    SearchPhotoFragment.this.updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                    if(mPhotoAdapter.getItemCount() == 0){
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
            }

            @Override
            public void onRequestPhotosFailed(Call<SearchPhotosResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.setRetryVisible(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchPhotos(mQuery, mPage, mPhotoRequestListener);
            mNoResultTextView.setVisibility(View.GONE);
        }
    }

    public void fetchNew(){
        if(mPhotos == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        SearchService.OnRequestPhotosListener mPhotoRequestListener = new SearchService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<SearchPhotosResult> call, Response<SearchPhotosResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchPhotosResult = response.body();
                    mPhotos = mSearchPhotosResult.results;
                    mPhotoAdapter.clear();
                    SearchPhotoFragment.this.updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                    if(mPhotoAdapter.getItemCount() == 0){
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
                    Toast.makeText(getContext(), getString(R.string.updated_photos), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<SearchPhotosResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.setRetryVisible(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchPhotos(mQuery, mPage, mPhotoRequestListener);
            mNoResultTextView.setVisibility(View.GONE);
        }
    }
}
