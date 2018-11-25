package com.b_lam.resplash.data.data;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.b_lam.resplash.Resplash;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.ViewPropertyTransition;
import com.google.gson.annotations.SerializedName;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;
import com.b_lam.resplash.R;

/**
 * Collection.
 * */

public class Collection extends AbstractItem<Collection, Collection.ViewHolder> {

    /**
     * id : 296
     * title : I like a man with a beard.
     * description : Yeah even Santa...
     * published_at : 2016-01-27T18:47:13-05:00
     * curated : false
     * total_photos : 12
     * private : false
     * share_key : 312d188df257b957f8b86d2ce20e4766
     * cover_photo : {"id":"C-mxLOk6ANs","width":5616,"height":3744,"color":"#E4C6A2","likes":12,"liked_by_user":false,"user":{"id":"xlt1-UPW7FE","username":"lionsdenpro","name":"Greg Raines","profile_image":{"small":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/lionsdenpro","html":"https://unsplash.com/lionsdenpro","photos":"https://api.unsplash.com/users/lionsdenpro/photos","likes":"https://api.unsplash.com/users/lionsdenpro/likes"}},"urls":{"raw":"https://images.unsplash.com/photo-1449614115178-cb924f730780","full":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy","regular":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max","small":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max","thumb":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"},"categories":[{"id":4,"title":"Nature","photo_count":31454,"links":{"self":"https://api.unsplash.com/categories/4","photos":"https://api.unsplash.com/categories/4/photos"}},{"id":6,"title":"People","photo_count":9844,"links":{"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}}],"links":{"self":"https://api.unsplash.com/photos/C-mxLOk6ANs","html":"https://unsplash.com/photos/C-mxLOk6ANs","download":"https://unsplash.com/photos/C-mxLOk6ANs/download"}}
     * user : {"id":"IFcEhJqem0Q","username":"fableandfolk","name":"Annie Spratt","bio":"","profile_image":{"small":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/fableandfolk","html":"https://unsplash.com/fableandfolk","photos":"https://api.unsplash.com/users/fableandfolk/photos","likes":"https://api.unsplash.com/users/fableandfolk/likes"}}
     * links : {"self":"https://api.unsplash.com/collections/296","html":"https://unsplash.com/collections/296","photos":"https://api.unsplash.com/collections/296/photos","related":"https://api.unsplash.com/collections/296/related"}
     */

    public int id;
    public String title;
    public String description;
    public String published_at;
    public boolean curated;
    public int total_photos;
    @SerializedName("private")
    public boolean privateX;
    public String share_key;
    /**
     * id : C-mxLOk6ANs
     * width : 5616
     * height : 3744
     * color : #E4C6A2
     * likes : 12
     * liked_by_user : false
     * user : {"id":"xlt1-UPW7FE","username":"lionsdenpro","name":"Greg Raines","profile_image":{"small":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/lionsdenpro","html":"https://unsplash.com/lionsdenpro","photos":"https://api.unsplash.com/users/lionsdenpro/photos","likes":"https://api.unsplash.com/users/lionsdenpro/likes"}}
     * urls : {"raw":"https://images.unsplash.com/photo-1449614115178-cb924f730780","full":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy","regular":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max","small":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max","thumb":"https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"}
     * categories : [{"id":4,"title":"Nature","photo_count":31454,"links":{"self":"https://api.unsplash.com/categories/4","photos":"https://api.unsplash.com/categories/4/photos"}},{"id":6,"title":"People","photo_count":9844,"links":{"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}}]
     * links : {"self":"https://api.unsplash.com/photos/C-mxLOk6ANs","html":"https://unsplash.com/photos/C-mxLOk6ANs","download":"https://unsplash.com/photos/C-mxLOk6ANs/download"}
     */

    public CoverPhoto cover_photo;
    /**
     * id : IFcEhJqem0Q
     * username : fableandfolk
     * name : Annie Spratt
     * bio :
     * profile_image : {"small":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"}
     * links : {"self":"https://api.unsplash.com/users/fableandfolk","html":"https://unsplash.com/fableandfolk","photos":"https://api.unsplash.com/users/fableandfolk/photos","likes":"https://api.unsplash.com/users/fableandfolk/likes"}
     */

    public User user;
    /**
     * self : https://api.unsplash.com/collections/296
     * html : https://unsplash.com/collections/296
     * photos : https://api.unsplash.com/collections/296/photos
     * related : https://api.unsplash.com/collections/296/related
     */

    public Links links;

    public static class CoverPhoto {
        // data
        public boolean hasFadeIn = false;

        public String id;
        public int width;
        public int height;
        public String color;
        public int likes;
        public boolean liked_by_user;
        /**
         * id : xlt1-UPW7FE
         * username : lionsdenpro
         * name : Greg Raines
         * profile_image : {"small":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"}
         * links : {"self":"https://api.unsplash.com/users/lionsdenpro","html":"https://unsplash.com/lionsdenpro","photos":"https://api.unsplash.com/users/lionsdenpro/photos","likes":"https://api.unsplash.com/users/lionsdenpro/likes"}
         */

        public User user;
        /**
         * raw : https://images.unsplash.com/photo-1449614115178-cb924f730780
         * full : https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy
         * regular : https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max
         * small : https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max
         * thumb : https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max
         */

        public Urls urls;
        /**
         * self : https://api.unsplash.com/photos/C-mxLOk6ANs
         * html : https://unsplash.com/photos/C-mxLOk6ANs
         * download : https://unsplash.com/photos/C-mxLOk6ANs/download
         */

        public Links links;
        /**
         * id : 4
         * title : Nature
         * photo_count : 31454
         * links : {"self":"https://api.unsplash.com/categories/4","photos":"https://api.unsplash.com/categories/4/photos"}
         */

        public List<Categories> categories;

        public static class User {
            public String id;
            public String username;
            public String name;
            /**
             * small : https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32
             * medium : https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64
             * large : https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128
             */

            public ProfileImage profile_image;
            /**
             * self : https://api.unsplash.com/users/lionsdenpro
             * html : https://unsplash.com/lionsdenpro
             * photos : https://api.unsplash.com/users/lionsdenpro/photos
             * likes : https://api.unsplash.com/users/lionsdenpro/likes
             */

            public Links links;

            public static class ProfileImage {
                public String small;
                public String medium;
                public String large;
            }

            public static class Links {
                public String self;
                public String html;
                public String photos;
                public String likes;
            }
        }

        public static class Urls {
            public String raw;
            public String full;
            public String regular;
            public String small;
            public String thumb;
        }

        public static class Links {
            public String self;
            public String html;
            public String download;
            public String download_location;
        }

        public static class Categories {
            public int id;
            public String title;
            public int photo_count;
            /**
             * self : https://api.unsplash.com/categories/4
             * photos : https://api.unsplash.com/categories/4/photos
             */

            public Links links;

            public static class Links {
                public String self;
                public String photos;
            }
        }
    }

    public static class User {
        public String id;
        public String username;
        public String name;
        public String bio;
        /**
         * small : https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32
         * medium : https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64
         * large : https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128
         */

        public ProfileImage profile_image;
        /**
         * self : https://api.unsplash.com/users/fableandfolk
         * html : https://unsplash.com/fableandfolk
         * photos : https://api.unsplash.com/users/fableandfolk/photos
         * likes : https://api.unsplash.com/users/fableandfolk/likes
         */

        public Links links;

        public static class ProfileImage {
            public String small;
            public String medium;
            public String large;
        }

        public static class Links {
            public String self;
            public String html;
            public String photos;
            public String likes;
        }
    }

    public static class Links {
        public String self;
        public String html;
        public String photos;
        public String related;
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
    public void bindView(Collection.ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        String url;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        if (this.cover_photo != null && this.cover_photo.urls != null) {
            switch (sharedPreferences.getString("load_quality", "Regular")) {
                case "Raw":
                    url = this.cover_photo.urls.raw;
                    break;
                case "Full":
                    url = this.cover_photo.urls.full;
                    break;
                case "Regular":
                    url = this.cover_photo.urls.regular;
                    break;
                case "Small":
                    url = this.cover_photo.urls.small;
                    break;
                case "Thumb":
                    url = this.cover_photo.urls.thumb;
                    break;
                default:
                    url = this.cover_photo.urls.regular;
            }

            DisplayMetrics displaymetrics = Resplash.getInstance().getResources().getDisplayMetrics();
            float finalHeight = displaymetrics.widthPixels / ((float)cover_photo.width/(float)cover_photo.height);

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
            holder.collectionNameCard.setText(this.title);
            holder.collectionSizeCard.setText(Resplash.getInstance().getResources().getString(R.string.photos, String.valueOf(this.total_photos)));
        }else{
            holder.collectionName.setText(this.title);
            holder.collectionSize.setText((Resplash.getInstance().getResources().getString(R.string.photos, String.valueOf(this.total_photos))));
        }
    }

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
                coverPhotoCard = (ImageView) itemView.findViewById(R.id.item_collection_card_img);
                collectionNameCard = (TextView) itemView.findViewById(R.id.item_collection_card_name);
                collectionSizeCard = (TextView) itemView.findViewById(R.id.item_collection_card_size);
            }else{
                coverPhoto = (ImageView) itemView.findViewById(R.id.item_collection_img);
                collectionName = (TextView) itemView.findViewById(R.id.item_collection_name);
                collectionSize = (TextView) itemView.findViewById(R.id.item_collection_size);
            }
        }
    }
}
