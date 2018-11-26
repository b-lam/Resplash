package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.b_lam.resplash.NoSwipeViewPager;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.adapters.ManageCollectionsDialogPagerAdapter;
import com.b_lam.resplash.data.data.ChangeCollectionPhotoResult;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.Me;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.item.CollectionMiniItem;
import com.b_lam.resplash.data.service.CollectionService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class ManageCollectionsDialog extends DialogFragment implements
        AuthManager.OnAuthDataChangedListener {

    private static final String TAG = "ManageCollectionsDialog";

    private static final int ADD_TO_COLLECTION_ID = 0;
    private static final int CREATE_COLLECTION_ID = 1;

    private CollectionService mService;
    private FastItemAdapter<CollectionMiniItem> mCollectionAdapter;
    private ItemAdapter mFooterAdapter;
    private List<Collection> mCollections;
    private Photo mPhoto;
    private Me mCurrentUser;
    private CollectionService.OnRequestCollectionsListener mCollectionRequestListener;
    private int mPage = 1;

    @BindView(R.id.add_to_collection_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.add_to_collection_progress) ProgressBar mProgressBar;
    @BindView(R.id.add_to_collection_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.manager_collections_dialog_pager) NoSwipeViewPager mViewPager;
    @BindView(R.id.create_collection_name_input_layout) TextInputLayout mNameTextInputLayout;
    @BindView(R.id.create_collection_name) TextInputEditText mNameEditText;
    @BindView(R.id.create_collection_description) TextInputEditText mDescriptionEditText;
    @BindView(R.id.create_collection_make_private_checkbox) CheckBox mMakePrivateCheckBox;
    @BindView(R.id.no_results_view) ConstraintLayout mNoResultsView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_manage_collections, null, false);
        ButterKnife.bind(this, view);

        ManageCollectionsDialogPagerAdapter adapter = new ManageCollectionsDialogPagerAdapter();
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);

        mService = CollectionService.getService();

        AuthManager.getInstance().addOnWriteDataListener(this);

        mCurrentUser = AuthManager.getInstance().getMe();

        mSwipeRefreshLayout.setEnabled(false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemViewCacheSize(5);

        mCollectionAdapter = new FastItemAdapter<>();

        mCollectionAdapter.withOnClickListener(mOnCollectionClickListener);

        mFooterAdapter = new ItemAdapter();

//        mCollectionAdapter.addAdapter(1, mFooterAdapter);

        mRecyclerView.setAdapter(mCollectionAdapter);

//        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
//            @Override
//            public void onLoadMore(int currentPage) {
//                mFooterAdapter.clear();
//                mFooterAdapter.add(new ProgressItem().withEnabled(false));
//                requestCollections();
//            }
//        });

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    mNameTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        init();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AuthManager.getInstance().removeOnWriteDataListener(this);
        if (mService != null) {
            mService.cancel();
        }
    }

    @OnClick(R.id.create_new_collection_button)
    public void createNewCollection() {
        mViewPager.setCurrentItem(CREATE_COLLECTION_ID);
    }

    @OnClick(R.id.create_collection_cancel_button)
    public void cancelCreateCollection() {
        mViewPager.setCurrentItem(ADD_TO_COLLECTION_ID);
    }

    @OnClick(R.id.create_collection_create_button)
    public void createCollection() {
        if (!mNameEditText.getText().toString().isEmpty()) {
            mNameTextInputLayout.setError(null);
            mService.createCollection(mNameEditText.getText().toString(),
                    mDescriptionEditText.getText().toString(),
                    mMakePrivateCheckBox.isChecked(),
                    new CollectionService.OnRequestACollectionListener() {
                        @Override
                        public void onRequestACollectionSuccess(Call<Collection> call, Response<Collection> response) {
                            init();
                            mViewPager.setCurrentItem(ADD_TO_COLLECTION_ID);
                            hideKeyboard();
                        }

                        @Override
                        public void onRequestACollectionFailed(Call<Collection> call, Throwable t) {
                            Toast.makeText(getActivity(), R.string.failed_to_create_collection, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mNameTextInputLayout.setError(getString(R.string.collection_name_is_required));
        }
    }

    private OnClickListener<CollectionMiniItem> mOnCollectionClickListener = new OnClickListener<CollectionMiniItem>(){
        @Override
        public boolean onClick(View v, IAdapter<CollectionMiniItem> adapter, CollectionMiniItem collection, int position) {
            if (mPhoto != null) {
                final ProgressBar mAddProgress = v.findViewById(R.id.item_collection_mini_progress);
                final ImageView mAddStatus = v.findViewById(R.id.item_collection_mini_added);
                mAddProgress.setVisibility(View.VISIBLE);
                mService.addPhotoToCollection(collection.getModel().id, mPhoto.id, new CollectionService.OnChangeCollectionPhotoListener() {
                    @Override
                    public void onChangePhotoSuccess(Call<ChangeCollectionPhotoResult> call, Response<ChangeCollectionPhotoResult> response) {
                        mAddProgress.setVisibility(View.GONE);
                        mAddStatus.setVisibility(View.VISIBLE);
                        init();
                    }

                    @Override
                    public void onChangePhotoFailed(Call<ChangeCollectionPhotoResult> call, Throwable t) {
                        Log.d(TAG, t.toString());
                    }
                });
            }
            return true;
        }
    };

    public void setPhoto(Photo photo) {
        mPhoto = photo;
    }

    public void setCollections(List<Collection> collections) {
        mCollections = collections;
    }

    private void init() {
        if (AuthManager.getInstance().getState() == AuthManager.FREEDOM_STATE) {
            if (AuthManager.getInstance().getMe() == null) {
                requestProfile();
            } else {
//                int listSize = AuthManager.getInstance()
//                        .getCollectionsManager()
//                        .getCollectionList()
//                        .size();
//                if (listSize > 0) {
//                    AuthManager.getInstance().getCollectionsManager().clearCollections();
//                    mCollectionAdapter.notifyItemRangeRemoved(1, listSize);
//                    mCollectionAdapter.notifyItemChanged(1);
//                }
                mPage = 1;
                requestCollections();
            }
        }
    }

    private void requestProfile() {
        AuthManager.getInstance().requestPersonalProfile();
    }

    private void requestCollections() {
        mService.cancel();

        mCollectionRequestListener = new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mCollections = response.body();
                    mCollectionAdapter.clear();
                    updateAdapter(mCollections);
                    mPage++;
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        };

        mService.requestUserCollections(mCurrentUser, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
    }

    public void updateAdapter(List<Collection> collections) {
        mProgressBar.setVisibility(View.GONE);

        for (Collection collection : collections) {
            mCollectionAdapter.add(new CollectionMiniItem(collection, mPhoto));
        }

        if (mCollectionAdapter.getAdapterItemCount() > 0) {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            mNoResultsView.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        View view = getDialog().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getDialog().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onWriteAccessToken() { }

    @Override
    public void onWriteUserInfo() {
        if (mCurrentUser == null) {
            mCurrentUser = AuthManager.getInstance().getMe();
            requestCollections();
        }
    }

    @Override
    public void onWriteAvatarPath() { }

    @Override
    public void onLogout() { }

    public interface ManageCollectionsDialogListener {
        void onPhotoAddedToCollection();
    }
}
