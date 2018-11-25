package com.b_lam.resplash.data.data;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.util.ThemeUtils;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.ViewPropertyTransition;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;
import com.b_lam.resplash.R;

/**
 * Photo.
 * */

public class Photo extends AbstractItem<Photo, Photo.ViewHolder>  {
    // data
    public boolean loadPhotoSuccess = false;
    public boolean hasFadedIn = false;

    /**
     * id : LBI7cgq3pbM
     * created_at : 2016-05-03T11:00:28-04:00
     * width : 5245
     * height : 3497
     * color : #60544D
     * likes : 12
     * liked_by_user : false
     * user : {"id":"pXhwzz1JtQU","username":"poorkane","name":"Gilbert Kane","profile_image":{"small":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/poorkane","html":"https://unsplash.com/poorkane","photos":"https://api.unsplash.com/users/poorkane/photos","likes":"https://api.unsplash.com/users/poorkane/likes"}}
     * current_user_collections : [{"id":206,"title":"Makers: Cat and Ben","published_at":"2016-01-12T18:16:09-05:00","curated":false,"cover_photo":{"id":"xCmvrpzctaQ","width":7360,"height":4912,"color":"#040C14","likes":12,"liked_by_user":false,"user":{"id":"eUO1o53muso","username":"crew","name":"Crew","profile_image":{"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/crew","html":"http://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}},"urls":{"raw":"https://images.unsplash.com/photo-1452457807411-4979b707c5be","full":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy","regular":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max","small":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max","thumb":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"},"categories":[{"id":6,"title":"People","photo_count":9844,"links":{"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}}],"links":{"self":"https://api.unsplash.com/photos/xCmvrpzctaQ","html":"https://unsplash.com/photos/xCmvrpzctaQ","download":"https://unsplash.com/photos/xCmvrpzctaQ/download"}},"user":{"id":"eUO1o53muso","username":"crew","name":"Crew","bio":"Work with the best designers and developers without breaking the bank. Creators of Unsplash.","profile_image":{"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/crew","html":"https://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}},"links":{"self":"https://api.unsplash.com/collections/206","html":"https://unsplash.com/collections/206","photos":"https://api.unsplash.com/collections/206/photos"}}]
     * urls : {"raw":"https://images.unsplash.com/face-springmorning.jpg","full":"https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg","regular":"https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=1080&fit=max","small":"https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=400&fit=max","thumb":"https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=200&fit=max"}
     * links : {"self":"https://api.unsplash.com/photos/LBI7cgq3pbM","html":"https://unsplash.com/photos/LBI7cgq3pbM","download":"https://unsplash.com/photos/LBI7cgq3pbM/download"}
     */

    public String id;
    public String created_at;
    public int width;
    public int height;
    public String color;
    public int likes;
    public boolean liked_by_user;
    /**
     * id : pXhwzz1JtQU
     * username : poorkane
     * name : Gilbert Kane
     * profile_image : {"small":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"}
     * links : {"self":"https://api.unsplash.com/users/poorkane","html":"https://unsplash.com/poorkane","photos":"https://api.unsplash.com/users/poorkane/photos","likes":"https://api.unsplash.com/users/poorkane/likes"}
     */

    public User user;
    /**
     * raw : https://images.unsplash.com/face-springmorning.jpg
     * full : https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg
     * regular : https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=1080&fit=max
     * small : https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=400&fit=max
     * thumb : https://images.unsplash.com/face-springmorning.jpg?q=75&fm=jpg&w=200&fit=max
     */

    public Urls urls;
    /**
     * self : https://api.unsplash.com/photos/LBI7cgq3pbM
     * html : https://unsplash.com/photos/LBI7cgq3pbM
     * download : https://unsplash.com/photos/LBI7cgq3pbM/download
     */

    public Links links;
    /**
     * id : 206
     * title : Makers: Cat and Ben
     * published_at : 2016-01-12T18:16:09-05:00
     * curated : false
     * cover_photo : {"id":"xCmvrpzctaQ","width":7360,"height":4912,"color":"#040C14","likes":12,"liked_by_user":false,"user":{"id":"eUO1o53muso","username":"crew","name":"Crew","profile_image":{"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/crew","html":"http://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}},"urls":{"raw":"https://images.unsplash.com/photo-1452457807411-4979b707c5be","full":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy","regular":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max","small":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max","thumb":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"},"categories":[{"id":6,"title":"People","photo_count":9844,"links":{"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}}],"links":{"self":"https://api.unsplash.com/photos/xCmvrpzctaQ","html":"https://unsplash.com/photos/xCmvrpzctaQ","download":"https://unsplash.com/photos/xCmvrpzctaQ/download"}}
     * user : {"id":"eUO1o53muso","username":"crew","name":"Crew","bio":"Work with the best designers and developers without breaking the bank. Creators of Unsplash.","profile_image":{"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/crew","html":"https://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}}
     * links : {"self":"https://api.unsplash.com/collections/206","html":"https://unsplash.com/collections/206","photos":"https://api.unsplash.com/collections/206/photos"}
     */

    public List<CurrentUserCollections> current_user_collections;

    public static class User {
        public String id;
        public String username;
        public String name;
        /**
         * small : https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32
         * medium : https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64
         * large : https://images.unsplash.com/face-springmorning.jpg?q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128
         */

        public ProfileImage profile_image;
        /**
         * self : https://api.unsplash.com/users/poorkane
         * html : https://unsplash.com/poorkane
         * photos : https://api.unsplash.com/users/poorkane/photos
         * likes : https://api.unsplash.com/users/poorkane/likes
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

    public static class CurrentUserCollections {
        public int id;
        public String title;
        public String published_at;
        public boolean curated;
        /**
         * id : xCmvrpzctaQ
         * width : 7360
         * height : 4912
         * color : #040C14
         * likes : 12
         * liked_by_user : false
         * user : {"id":"eUO1o53muso","username":"crew","name":"Crew","profile_image":{"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"},"links":{"self":"https://api.unsplash.com/users/crew","html":"http://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}}
         * urls : {"raw":"https://images.unsplash.com/photo-1452457807411-4979b707c5be","full":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy","regular":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max","small":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max","thumb":"https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"}
         * categories : [{"id":6,"title":"People","photo_count":9844,"links":{"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}}]
         * links : {"self":"https://api.unsplash.com/photos/xCmvrpzctaQ","html":"https://unsplash.com/photos/xCmvrpzctaQ","download":"https://unsplash.com/photos/xCmvrpzctaQ/download"}
         */

        public CoverPhoto cover_photo;
        /**
         * id : eUO1o53muso
         * username : crew
         * name : Crew
         * bio : Work with the best designers and developers without breaking the bank. Creators of Unsplash.
         * profile_image : {"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"}
         * links : {"self":"https://api.unsplash.com/users/crew","html":"https://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}
         */

        public User user;
        /**
         * self : https://api.unsplash.com/collections/206
         * html : https://unsplash.com/collections/206
         * photos : https://api.unsplash.com/collections/206/photos
         */

        public Links links;

        public static class CoverPhoto {
            public String id;
            public int width;
            public int height;
            public String color;
            public int likes;
            public boolean liked_by_user;
            /**
             * id : eUO1o53muso
             * username : crew
             * name : Crew
             * profile_image : {"small":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32","medium":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64","large":"https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"}
             * links : {"self":"https://api.unsplash.com/users/crew","html":"http://unsplash.com/crew","photos":"https://api.unsplash.com/users/crew/photos","likes":"https://api.unsplash.com/users/crew/likes"}
             */

            public User user;
            /**
             * raw : https://images.unsplash.com/photo-1452457807411-4979b707c5be
             * full : https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy
             * regular : https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max
             * small : https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max
             * thumb : https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max
             */

            public Urls urls;
            /**
             * self : https://api.unsplash.com/photos/xCmvrpzctaQ
             * html : https://unsplash.com/photos/xCmvrpzctaQ
             * download : https://unsplash.com/photos/xCmvrpzctaQ/download
             */

            public Links links;
            /**
             * id : 6
             * title : People
             * photo_count : 9844
             * links : {"self":"https://api.unsplash.com/categories/6","photos":"https://api.unsplash.com/categories/6/photos"}
             */

            public List<Categories> categories;

            public static class User {
                public String id;
                public String username;
                public String name;
                /**
                 * small : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32
                 * medium : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64
                 * large : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128
                 */

                public ProfileImage profile_image;
                /**
                 * self : https://api.unsplash.com/users/crew
                 * html : http://unsplash.com/crew
                 * photos : https://api.unsplash.com/users/crew/photos
                 * likes : https://api.unsplash.com/users/crew/likes
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
                 * self : https://api.unsplash.com/categories/6
                 * photos : https://api.unsplash.com/categories/6/photos
                 */

                public Links links;

                public static class Links {
                    public String self;
                    public String photos;
                }
            }
        }

        public static class User {
            public boolean hasFadedIn = false;

            public String id;
            public String username;
            public String name;
            public String bio;
            /**
             * small : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32
             * medium : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64
             * large : https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128
             */

            public ProfileImage profile_image;
            /**
             * self : https://api.unsplash.com/users/crew
             * html : https://unsplash.com/crew
             * photos : https://api.unsplash.com/users/crew/photos
             * likes : https://api.unsplash.com/users/crew/likes
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
        }
    }

    // Fast Adapter methods
    @Override
    public int getType() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        switch (sharedPreferences.getString("item_layout", "List")){
            case "List":
                return R.id.item_image;
            case "Cards":
                return R.id.item_image_card;
            case "Grid":
                return R.id.item_image_grid;
            default:
                return R.id.item_image;
        }
    }

    @Override
    public int getLayoutRes() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        switch (sharedPreferences.getString("item_layout", "List")){
            case "List":
                return R.layout.item_image;
            case "Cards":
                return R.layout.item_image_card;
            case "Grid":
                return R.layout.item_image_grid;
            default:
                return R.layout.item_image;
        }
    }

    @Override
    public void bindView(final Photo.ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        String url = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        if (this.urls != null) {
            switch (sharedPreferences.getString("load_quality", "Regular")) {
                case "Raw":
                    url = this.urls.raw;
                    break;
                case "Full":
                    url = this.urls.full;
                    break;
                case "Regular":
                    url = this.urls.regular;
                    break;
                case "Small":
                    url = this.urls.small;
                    break;
                case "Thumb":
                    url = this.urls.thumb;
                    break;
                default:
                    url = this.urls.regular;
            }
        }

        DisplayMetrics displaymetrics = Resplash.getInstance().getResources().getDisplayMetrics();
        float finalHeight = displaymetrics.widthPixels / ((float)width/(float)height);

        ViewPropertyTransition.Animator fadeAnimation = new ViewPropertyTransition.Animator() {
            @Override
            public void animate(View view) {
                ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeAnim.setDuration(500);
                fadeAnim.start();
            }
        };

        switch (sharedPreferences.getString("item_layout", "List")){
            case "List":
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(GenericTransitionOptions.with(fadeAnimation))
                        .into(holder.imageList);

                holder.imageList.setMinimumHeight((int) finalHeight);
                int colorFrom = ThemeUtils.getThemeAttrColor(holder.itemView.getContext(), R.attr.colorPrimary);
                int colorTo;
                if(this.color != null){
                    colorTo = Color.parseColor(this.color);
                }else{
                    colorTo = colorFrom;
                }
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(1000);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        holder.imageList.setBackgroundColor((int) animator.getAnimatedValue());
                    }

                });
                colorAnimation.start();
                break;
            case "Cards":
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(GenericTransitionOptions.with(fadeAnimation))
                        .into(holder.imageCard);

                holder.imageCard.setMinimumHeight((int) finalHeight);
                holder.authorCard.setText(Resplash.getInstance().getResources().getString(R.string.by_author, this.user.name));
                break;
            case "Grid":
                Glide.with(holder.itemView.getContext())
                        .load(url)
                        .transition(GenericTransitionOptions.with(fadeAnimation))
                        .apply(new RequestOptions().centerCrop())
                        .into(holder.imageGrid);
                break;
        }
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    // Manually create the ViewHolder class
    protected static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageList;
        ImageView imageCard;
        TextView authorCard;
        ImageView imageGrid;

        public ViewHolder(View itemView) {
            super(itemView);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
            switch (sharedPreferences.getString("item_layout", "List")){
                case "List":
                    imageList = (ImageView) itemView.findViewById(R.id.item_image_img);
                    break;
                case "Cards":
                    imageCard = (ImageView) itemView.findViewById(R.id.item_image_card_img);
                    authorCard = (TextView) itemView.findViewById(R.id.item_image_card_author);
                    break;
                case "Grid":
                    imageGrid = (ImageView) itemView.findViewById(R.id.item_image_grid_img);
                    break;
            }
        }
    }
}
