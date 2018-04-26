package com.b_lam.resplash.data.data;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
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
 * Photo
 **/

public class Photo extends AbstractItem<Photo, Photo.ViewHolder>  {

    /**
     "id": "Dwu85P9SOIk",
     "created_at": "2016-05-03T11:00:28-04:00",
     "updated_at": "2016-07-10T11:00:01-05:00",
     "width": 2448,
     "height": 3264,
     "color": "#6E633A",
     "downloads": 1345,
     "likes": 24,
     "liked_by_user": false,
     "description": "A man drinking a coffee.",
     "exif": {
        "make": "Canon",
        "model": "Canon EOS 40D",
        "exposure_time": "0.011111111111111112",
        "aperture": "4.970854",
        "focal_length": "37",
        "iso": 100
     },
     "location": {
        "city": "Montreal",
        "country": "Canada",
        "position": {
        "latitude": 45.4732984,
        "longitude": -73.6384879
     }
     },
     "current_user_collections": [ // The *current user's* collections that this photo belongs to.
     {
        "id": 206,
        "title": "Makers: Cat and Ben",
        "published_at": "2016-01-12T18:16:09-05:00",
        "updated_at": "2016-07-10T11:00:01-05:00",
        "curated": false,
        "cover_photo": {
            "id": "xCmvrpzctaQ",
            "width": 7360,
            "height": 4912,
            "color": "#040C14",
            "likes": 12,
            "liked_by_user": false,
            "description": "A man drinking a coffee.",
            "user": {
                "id": "eUO1o53muso",
                "username": "crew",
                "name": "James Example",
                "first_name": "James",
                "last_name": "Example",
                "instagram_username": "instantgrammer",
                "twitter_username": "crew",
                "portfolio_url": "https://crew.co/",
                "bio": "Work with the best designers and developers without breaking the bank.",
                "location": "Montreal",
                "total_likes": 0,
                "total_photos": 74,
                "total_collections": 52,
                "profile_image": {
                    "small": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32",
                    "medium": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64",
                    "large": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"
                },
                "links": {
                    "self": "https://api.unsplash.com/users/crew",
                    "html": "http://unsplash.com/crew",
                    "photos": "https://api.unsplash.com/users/crew/photos",
                    "likes": "https://api.unsplash.com/users/crew/likes",
                    "portfolio": "https://api.unsplash.com/users/crew/portfolio"
                }
            },
            "urls": {
                "raw":  "https://images.unsplash.com/photo-1452457807411-4979b707c5be",
                "full": "https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy",
                "regular": "https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max",
                "small": "https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max",
                "thumb": "https://images.unsplash.com/photo-1452457807411-4979b707c5be?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"
            },
            "categories": [
            {
                "id": 6,
                "title": "People",
                "photo_count": 9844,
                "links": {
                    "self": "https://api.unsplash.com/categories/6",
                    "photos": "https://api.unsplash.com/categories/6/photos"
                }
            }
            ],
            "links": {
                "self": "https://api.unsplash.com/photos/xCmvrpzctaQ",
                "html": "https://unsplash.com/photos/xCmvrpzctaQ",
                "download": "https://unsplash.com/photos/xCmvrpzctaQ/download",
                "download_location": "https://api.unsplash.com/photos/xCmvrpzctaQ/download"
            }
        },
        "user": {
            "id": "eUO1o53muso",
            "updated_at": "2016-07-10T11:00:01-05:00",
            "username": "crew",
            "name": "Crew",
            "portfolio_url": "https://crew.co/",
            "bio": "Work with the best designers and developers without breaking the bank.",
            "location": "Montreal",
            "total_likes": 0,
            "total_photos": 74,
            "total_collections": 52,
            "profile_image": {
                "small": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32",
                "medium": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64",
                "large": "https://images.unsplash.com/profile-1441298102341-b7ba36fdc35c?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"
            },
            "links": {
                "self": "https://api.unsplash.com/users/crew",
                "html": "https://unsplash.com/crew",
                "photos": "https://api.unsplash.com/users/crew/photos",
                "likes": "https://api.unsplash.com/users/crew/likes",
                "portfolio": "https://api.unsplash.com/users/crew/portfolio"
            }
        },
        "links": {
            "self": "https://api.unsplash.com/collections/206",
            "html": "https://unsplash.com/collections/206",
            "photos": "https://api.unsplash.com/collections/206/photos"
        }
     },
     // ... more collections
     ],
     "urls": {
        "raw": "https://images.unsplash.com/photo-1417325384643-aac51acc9e5d",
        "full": "https://images.unsplash.com/photo-1417325384643-aac51acc9e5d?q=75&fm=jpg",
        "regular": "https://images.unsplash.com/photo-1417325384643-aac51acc9e5d?q=75&fm=jpg&w=1080&fit=max",
        "small": "https://images.unsplash.com/photo-1417325384643-aac51acc9e5d?q=75&fm=jpg&w=400&fit=max",
        "thumb": "https://images.unsplash.com/photo-1417325384643-aac51acc9e5d?q=75&fm=jpg&w=200&fit=max"
     },
     "categories": [
     {
        "id": 4,
        "title": "Nature",
        "photo_count": 24783,
        "links": {
            "self": "https://api.unsplash.com/categories/4",
            "photos": "https://api.unsplash.com/categories/4/photos"
        }
     }
     ],
     "links": {
        "self": "https://api.unsplash.com/photos/Dwu85P9SOIk",
        "html": "https://unsplash.com/photos/Dwu85P9SOIk",
        "download": "https://unsplash.com/photos/Dwu85P9SOIk/download"
        "download_location": "https://api.unsplash.com/photos/Dwu85P9SOIk/download"
     },
     "user": {
        "id": "QPxL2MGqfrw",
        "updated_at": "2016-07-10T11:00:01-05:00",
        "username": "exampleuser",
        "name": "Joe Example",
        "portfolio_url": "https://example.com/",
        "bio": "Just an everyday Joe",
        "location": "Montreal",
        "total_likes": 5,
        "total_photos": 10,
        "total_collections": 13,
        "links": {
            "self": "https://api.unsplash.com/users/exampleuser",
            "html": "https://unsplash.com/exampleuser",
            "photos": "https://api.unsplash.com/users/exampleuser/photos",
            "likes": "https://api.unsplash.com/users/exampleuser/likes",
            "portfolio": "https://api.unsplash.com/users/exampleuser/portfolio"
        }
     }
     */

    public String id;
    public String created_at;
    public String updated_at;
    public int width;
    public int height;
    public String color;
    public int downloads;
    public int likes;
    public boolean liked_by_user;
    public String description;

    public Exif exif;

    public Location location;

    public List<Collection> current_user_collections;

    public PhotoUrls urls;

    public List<Category> categories;

    public PhotoLinks links;

    public User user;

    public Story story;

    public List<Tag> tags;

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
