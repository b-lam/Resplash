package com.b_lam.resplash.data.item;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.model.Collection;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.ViewPropertyTransition;
import com.mikepenz.fastadapter.items.ModelAbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CollectionMiniItem extends ModelAbstractItem<Collection, CollectionMiniItem, CollectionMiniItem.ViewHolder> {

    public CollectionMiniItem (Collection collection) {
        super(collection);
    }

    public CollectionMiniItem (Collection collection, List<Collection> currentUserCollections) {
        super(collection);
        mCurrentUserCollections = currentUserCollections;
    }

    private List<Collection> mCurrentUserCollections;

    // Fast Adapter methods
    @Override
    public int getType() {
        return R.id.item_collection_mini;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_collection_mini;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        String url;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        if (getModel().cover_photo != null && getModel().cover_photo.urls != null) {
            switch (sharedPreferences.getString("load_quality", "Regular")) {
                case "Raw":
                    url = getModel().cover_photo.urls.raw;
                    break;
                case "Full":
                    url = getModel().cover_photo.urls.full;
                    break;
                case "Regular":
                    url = getModel().cover_photo.urls.regular;
                    break;
                case "Small":
                    url = getModel().cover_photo.urls.small;
                    break;
                case "Thumb":
                    url = getModel().cover_photo.urls.thumb;
                    break;
                default:
                    url = getModel().cover_photo.urls.regular;
            }

            ViewPropertyTransition.Animator fadeAnimation = view -> {
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeAnim.setDuration(500);
                fadeAnim.start();
            };

            Glide.with(holder.itemView.getContext())
                    .load(url)
                    .transition(GenericTransitionOptions.with(fadeAnimation))
                    .into(holder.coverPhoto);
        } else if (holder.coverPhoto != null) {
            holder.coverPhoto.setImageResource(R.drawable.placeholder);
        }

        holder.collectionPrivate.setVisibility(View.INVISIBLE);
        holder.collectionAdded.setVisibility(View.INVISIBLE);

        if (getModel().privateX) {
            holder.collectionPrivate.setVisibility(View.VISIBLE);
        }

        for (Collection userCollection : mCurrentUserCollections) {
            if (getModel().id == userCollection.id) {
                holder.collectionAdded.setVisibility(View.VISIBLE);
            }
        }

        holder.collectionSize.setText((Resplash.getInstance().getResources().getString(R.string.photos, String.valueOf(getModel().total_photos))));
        holder.collectionName.setText(getModel().title);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected View view;
        @BindView(R.id.item_collection_mini_image) ImageView coverPhoto;
        @BindView(R.id.item_collection_mini_size) TextView collectionSize;
        @BindView(R.id.item_collection_mini_title) TextView collectionName;
        @BindView(R.id.item_collection_mini_private) ImageView collectionPrivate;
        @BindView(R.id.item_collection_mini_added) ImageView collectionAdded;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}
