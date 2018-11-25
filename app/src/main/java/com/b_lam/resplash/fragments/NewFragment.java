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
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.service.PhotoService;
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

/**
 * Created by Brandon on 10/8/2016.
 */

public class NewFragment extends Fragment {

    private String TAG = "NewFragment";
    private PhotoService mService;
    private FastItemAdapter<Photo> mPhotoAdapter;
    private List<Photo> mPhotos;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private ItemAdapter mFooterAdapter;
    private int mPage, mColumns;
    private String mSort;
    private SharedPreferences sharedPreferences;

    public NewFragment() {
        // Required empty public constructor
    }

    public static NewFragment newInstance(String sort) {
        NewFragment newFragment = new NewFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        newFragment.setArguments(args);

        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        String mLayoutType = sharedPreferences.getString("item_layout", "List");
        mSort = getArguments().getString("sort", "latest");
        if(mLayoutType.equals("List") || mLayoutType.equals("Cards")){
            mColumns = 1;
        }else{
            mColumns = 2;
        }

        mService = PhotoService.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_new, container, false);
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_new_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_new_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_new_error_view);
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerNew);

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
        if(mPhotos == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }


        PhotoService.OnRequestPhotosListener mPhotoRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mPhotos = response.body();
                    mFooterAdapter.clear();
                    NewFragment.this.updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                }else{
                    mImagesErrorView.setTitle(R.string.error_http);
                    mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
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

        mService.requestPhotos(mPage, Resplash.DEFAULT_PER_PAGE, mSort, mPhotoRequestListener);

    }

    public void fetchNew(){
        if(mPhotos == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        PhotoService.OnRequestPhotosListener mPhotoRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mPhotos = response.body();
                    mPhotoAdapter.clear();
                    NewFragment.this.updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
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
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
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

        mService.requestPhotos(mPage, Resplash.DEFAULT_PER_PAGE, mSort, mPhotoRequestListener);
    }
}
