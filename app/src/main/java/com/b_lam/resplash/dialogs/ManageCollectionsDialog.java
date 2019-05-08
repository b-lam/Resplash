package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.adapters.ManageCollectionsDialogPagerAdapter;
import com.b_lam.resplash.data.item.CollectionMiniItem;
import com.b_lam.resplash.data.model.ChangeCollectionPhotoResult;
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.model.Me;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.service.CollectionService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.views.NoSwipeViewPager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    @IntDef({CollectionUpdateType.ADD, CollectionUpdateType.DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CollectionUpdateType {
        int ADD = 0;
        int DELETE = 1;
    }

    private CollectionService mService;
    private FastItemAdapter<CollectionMiniItem> mCollectionAdapter;
    private ItemAdapter mFooterAdapter;
    private List<Collection> mCurrentUserCollections;
    private Photo mPhoto;
    private Me mCurrentUser;
    private ManageCollectionsDialogListener mManageCollectionsDialogListener;
    private int mPage = 1;

    @BindView(R.id.add_to_collection_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.add_to_collection_progress) ProgressBar mProgressBar;
    @BindView(R.id.add_to_collection_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.manager_collections_dialog_pager) NoSwipeViewPager mViewPager;
    @BindView(R.id.create_collection_name_input_layout) TextInputLayout mNameTextInputLayout;
    @BindView(R.id.create_collection_name) TextInputEditText mNameEditText;
    @BindView(R.id.create_collection_description) TextInputEditText mDescriptionEditText;
    @BindView(R.id.create_collection_make_private_checkbox) CheckBox mMakePrivateCheckBox;
    @BindView(R.id.create_collection_create_button) Button mCreateCollectionButton;
    @BindView(R.id.create_collection_cancel_button) Button mCancelCollectionButton;
    @BindView(R.id.no_results_view) ConstraintLayout mNoResultsView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

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
        mFooterAdapter = new ItemAdapter<>();

        mCollectionAdapter.addAdapter(1, mFooterAdapter);

        mCollectionAdapter.withOnClickListener(mOnCollectionClickListener);

        mRecyclerView.setAdapter(mCollectionAdapter);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                mFooterAdapter.add(new ProgressItem().withEnabled(false));
                requestCollections();
            }
        });

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    mNameTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
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
        if (mNameEditText.getText() != null && !mNameEditText.getText().toString().isEmpty()) {
            mNameTextInputLayout.setError(null);
            mCreateCollectionButton.setEnabled(false);
            mCancelCollectionButton.setEnabled(false);
            mService.createCollection(mNameEditText.getText().toString(),
                    (mDescriptionEditText.getText() == null) ? "" : mDescriptionEditText.getText().toString(),
                    mMakePrivateCheckBox.isChecked(),
                    new CollectionService.OnRequestACollectionListener() {
                        @Override
                        public void onRequestACollectionSuccess(Call<Collection> call, Response<Collection> response) {
                            insertCollection(response.body());
                            mNameEditText.setText("");
                            mDescriptionEditText.setText("");
                            mMakePrivateCheckBox.setChecked(false);
                            mCreateCollectionButton.setEnabled(true);
                            mCancelCollectionButton.setEnabled(true);
                            mViewPager.setCurrentItem(ADD_TO_COLLECTION_ID);
                            mManageCollectionsDialogListener.onCollectionCreated();
                            hideKeyboard();
                        }

                        @Override
                        public void onRequestACollectionFailed(Call<Collection> call, Throwable t) {
                            mCreateCollectionButton.setEnabled(true);
                            mCancelCollectionButton.setEnabled(true);
                            Toast.makeText(getActivity(), R.string.failed_to_create_collection, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mNameTextInputLayout.setError(getString(R.string.collection_name_is_required));
        }
    }

    private OnClickListener<CollectionMiniItem> mOnCollectionClickListener = new OnClickListener<CollectionMiniItem>(){
        @Override
        public boolean onClick(View v, @NonNull IAdapter<CollectionMiniItem> adapter, @NonNull CollectionMiniItem collection, int position) {
            if (mPhoto != null) {
                final ProgressBar addProgress = v.findViewById(R.id.item_collection_mini_progress);
                final CollectionService.OnChangeCollectionPhotoListener onDeleteCollectionPhotoListener = new CollectionService.OnChangeCollectionPhotoListener() {
                    @Override
                    public void onChangePhotoSuccess(Call<ChangeCollectionPhotoResult> call, Response<ChangeCollectionPhotoResult> response) {
                        addProgress.setVisibility(View.INVISIBLE);
                        if (response.isSuccessful() && response.body() != null) {
                            ListIterator<Collection> iterator = mCurrentUserCollections.listIterator();
                            while (iterator.hasNext()) {
                                if (iterator.next().id == response.body().collection.id) {
                                    iterator.remove();
                                }
                            }
                            updateCollectionAtPosition(position, response.body().collection, mCurrentUserCollections);
                            mManageCollectionsDialogListener.onCollectionUpdated(CollectionUpdateType.DELETE, response.body().collection, mCurrentUserCollections);
                        }
                    }

                    @Override
                    public void onChangePhotoFailed(Call<ChangeCollectionPhotoResult> call, Throwable t) {
                        addProgress.setVisibility(View.INVISIBLE);
                    }
                };

                final CollectionService.OnChangeCollectionPhotoListener onAddCollectionPhotoListener = new CollectionService.OnChangeCollectionPhotoListener() {
                    @Override
                    public void onChangePhotoSuccess(Call<ChangeCollectionPhotoResult> call, Response<ChangeCollectionPhotoResult> response) {
                        addProgress.setVisibility(View.INVISIBLE);
                        if (response.isSuccessful() && response.body() != null) {
                            mCurrentUserCollections.add(response.body().collection);
                            updateCollectionAtPosition(position, response.body().collection, mCurrentUserCollections);
                            mManageCollectionsDialogListener.onCollectionUpdated(CollectionUpdateType.ADD, response.body().collection, mCurrentUserCollections);
                        }
                    }

                    @Override
                    public void onChangePhotoFailed(Call<ChangeCollectionPhotoResult> call, Throwable t) {
                        addProgress.setVisibility(View.INVISIBLE);
                    }
                };

                addProgress.setVisibility(View.VISIBLE);

                if (isPhotoInCollection(mPhoto, collection.getModel())) {
                    mService.deletePhotoFromCollection(collection.getModel().id, mPhoto.id, onDeleteCollectionPhotoListener);
                } else {
                    mService.addPhotoToCollection(collection.getModel().id, mPhoto.id, onAddCollectionPhotoListener);
                }
            }

            return true;
        }
    };

    public void setPhoto(Photo photo) {
        mPhoto = photo;
        mCurrentUserCollections = photo.current_user_collections;
    }

    public void setListener(ManageCollectionsDialogListener manageCollectionsDialogListener) {
        mManageCollectionsDialogListener = manageCollectionsDialogListener;
    }

    private void init() {
        if (AuthManager.getInstance().getState() == AuthManager.FREEDOM_STATE) {
            if (AuthManager.getInstance().getMe() == null) {
                requestProfile();
            } else {
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

        mService.requestUserCollections(mCurrentUser, mPage, Resplash.DEFAULT_PER_PAGE, new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (response.code() == 200 && response.body() != null) {
                    mFooterAdapter.clear();
                    updateAdapter(response.body());
                    mPage++;
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        });
    }

    private void updateAdapter(List<Collection> collections) {
        mProgressBar.setVisibility(View.GONE);

        List<CollectionMiniItem> userCollections = new ArrayList<>();

        for (Collection collection : collections) {
            userCollections.add(new CollectionMiniItem(collection, mCurrentUserCollections));
        }

        mCollectionAdapter.add(userCollections);

        if (mCollectionAdapter.getAdapterItemCount() > 0) {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            mNoResultsView.setVisibility(View.VISIBLE);
        }
    }

    private void insertCollection(Collection collection) {
        mCollectionAdapter.add(0, new CollectionMiniItem(collection, mCurrentUserCollections));
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void updateCollectionAtPosition(int position, Collection collection, List<Collection> currentUserCollections) {
        mCollectionAdapter.set(position, new CollectionMiniItem(collection, currentUserCollections));
        mCollectionAdapter.notifyAdapterItemChanged(position);
    }

    private void hideKeyboard() {
        if (getDialog() != null) {
            View view = getDialog().getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) getDialog().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
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

    private boolean isPhotoInCollection(Photo photo, Collection collection) {
        for (Collection userCollection : photo.current_user_collections) {
            if (userCollection.id == collection.id) return true;
        }
        return false;
    }

    public interface ManageCollectionsDialogListener {
        void onCollectionUpdated(@CollectionUpdateType int updateType, Collection collection, List<Collection> currentUserCollections);
        void onCollectionCreated();
    }
}
