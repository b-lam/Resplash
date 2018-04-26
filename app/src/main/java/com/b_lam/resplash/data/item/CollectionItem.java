package com.b_lam.resplash.data.item;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.Collection;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.ViewPropertyTransition;
import com.mikepenz.fastadapter.items.ModelAbstractItem;

import java.util.List;

public class CollectionItem extends ModelAbstractItem<Collection, CollectionItem, CollectionItem.ViewHolder> {

    public CollectionItem (Collection collection) {
        super(collection);
    }

    // Fast Adapter methods
    @Override
    public int getType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        switch (sharedPreferences.getString("item_layout", "List")){
            case "List":
                return R.id.item_collection;
            case "Cards":
                return R.id.item_collection_card;
            case "Grid":
                return R.id.item_collection;
            default:
                return R.id.item_collection;
        }
    }

    @Override
    public int getLayoutRes() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        switch (sharedPreferences.getString("item_layout", "List")){
            case "List":
                return R.layout.item_collection;
            case "Cards":
                return R.layout.item_collection_card;
            case "Grid":
                return R.layout.item_collection;
            default:
                return R.layout.item_collection;
        }    }

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

            DisplayMetrics displaymetrics = Resplash.getInstance().getResources().getDisplayMetrics();
            float finalHeight = displaymetrics.widthPixels / ((float)getModel().cover_photo.width/(float)getModel().cover_photo.height);

            ViewPropertyTransition.Animator fadeAnimation = new ViewPropertyTransition.Animator() {
                @Override
                public void animate(View view) {
                    ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                    fadeAnim.setDuration(500);
                    fadeAnim.start();
                }
            };

            if(sharedPreferences.getString("item_layout", "List").equals("Cards")){
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(GenericTransitionOptions.with(fadeAnimation))
                        .into(holder.coverPhotoCard);
                holder.coverPhotoCard.setMinimumHeight((int) finalHeight);
            }else{
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(GenericTransitionOptions.with(fadeAnimation))
                        .into(holder.coverPhoto);
                holder.coverPhoto.setMinimumHeight((int) finalHeight);
            }
        }else if(holder.coverPhotoCard != null){
            holder.coverPhotoCard.setImageResource(R.drawable.placeholder);
        }else if(holder.coverPhoto != null){
            holder.coverPhoto.setImageResource(R.drawable.placeholder);
        }

        if(sharedPreferences.getString("item_layout", "List").equals("Cards")){
            holder.collectionNameCard.setText(getModel().title);
            holder.collectionSizeCard.setText(Resplash.getInstance().getResources().getString(R.string.photos, String.valueOf(getModel().total_photos)));
        }else{
            holder.collectionName.setText(getModel().title);
            holder.collectionSize.setText((Resplash.getInstance().getResources().getString(R.string.photos, String.valueOf(getModel().total_photos))));
        }
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView coverPhoto, coverPhotoCard;
        TextView collectionName, collectionNameCard;
        TextView collectionSize, collectionSizeCard;

        public ViewHolder(View itemView) {
            super(itemView);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
            if(sharedPreferences.getString("item_layout", "List").equals("Cards")) {
                coverPhotoCard = itemView.findViewById(R.id.item_collection_card_img);
                collectionNameCard = itemView.findViewById(R.id.item_collection_card_name);
                collectionSizeCard = itemView.findViewById(R.id.item_collection_card_size);
            }else{
                coverPhoto = itemView.findViewById(R.id.item_collection_img);
                collectionName = itemView.findViewById(R.id.item_collection_name);
                collectionSize = itemView.findViewById(R.id.item_collection_size);
            }
        }
    }
}
