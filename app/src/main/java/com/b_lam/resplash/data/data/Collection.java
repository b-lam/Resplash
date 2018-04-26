package com.b_lam.resplash.data.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Collection
 **/

public class Collection {

    /**
     "id": 296,
     "title": "I like a man with a beard.",
     "description": "Yeah even Santa...",
     "published_at": "2016-01-27T18:47:13-05:00",
     "updated_at": "2016-07-10T11:00:01-05:00",
     "curated": false,
     "total_photos": 12,
     "private": false,
     "share_key": "312d188df257b957f8b86d2ce20e4766",
     "cover_photo": {
        "id": "C-mxLOk6ANs",
        "width": 5616,
        "height": 3744,
        "color": "#E4C6A2",
        "likes": 12,
        "liked_by_user": false,
        "description": "A man drinking a coffee.",
        "user": {
            "id": "xlt1-UPW7FE",
            "username": "lionsdenpro",
            "name": "Greg Raines",
            "portfolio_url": "https://example.com/",
            "bio": "Just an everyday Greg",
            "location": "Montreal",
            "total_likes": 5,
            "total_photos": 10,
            "total_collections": 13,
            "profile_image": {
                "small": "https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32",
                "medium": "https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64",
                "large": "https://images.unsplash.com/profile-1449546653256-0faea3006d34?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"
            },
            "links": {
                "self": "https://api.unsplash.com/users/lionsdenpro",
                "html": "https://unsplash.com/lionsdenpro",
                "photos": "https://api.unsplash.com/users/lionsdenpro/photos",
                "likes": "https://api.unsplash.com/users/lionsdenpro/likes",
                "portfolio": "https://api.unsplash.com/users/lionsdenpro/portfolio"
            }
        },
        "urls": {
            "raw": "https://images.unsplash.com/photo-1449614115178-cb924f730780",
            "full": "https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy",
            "regular": "https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=1080&fit=max",
            "small": "https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=400&fit=max",
            "thumb": "https://images.unsplash.com/photo-1449614115178-cb924f730780?ixlib=rb-0.3.5&q=80&fm=jpg&crop=entropy&w=200&fit=max"
        },
        "categories": [
        {
            "id": 4,
            "title": "Nature",
            "photo_count": 31454,
            "links": {
                "self": "https://api.unsplash.com/categories/4",
                "photos": "https://api.unsplash.com/categories/4/photos"
            }
        },
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
            "self": "https://api.unsplash.com/photos/C-mxLOk6ANs",
            "html": "https://unsplash.com/photos/C-mxLOk6ANs",
            "download": "https://unsplash.com/photos/C-mxLOk6ANs/download"
        }
     },
     "user": {
        "id": "IFcEhJqem0Q",
        "updated_at": "2016-07-10T11:00:01-05:00",
        "username": "fableandfolk",
        "name": "Annie Spratt",
        "portfolio_url": "http://mammasaurus.co.uk",
        "bio": "Follow me on Twitter &amp; Instagram @anniespratt\r\nEmail me at hello@fableandfolk.com",
        "location": "New Forest National Park, UK",
        "total_likes": 0,
        "total_photos": 273,
        "total_collections": 36,
        "profile_image": {
            "small": "https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=32&w=32",
            "medium": "https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=64&w=64",
            "large": "https://images.unsplash.com/profile-1450003783594-db47c765cea3?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&fit=crop&h=128&w=128"
        },
        "links": {
            "self": "https://api.unsplash.com/users/fableandfolk",
            "html": "https://unsplash.com/fableandfolk",
            "photos": "https://api.unsplash.com/users/fableandfolk/photos",
            "likes": "https://api.unsplash.com/users/fableandfolk/likes",
            "portfolio": "https://api.unsplash.com/users/fableandfolk/portfolio"
        }
     },
     "links": {
        "self": "https://api.unsplash.com/collections/296",
        "html": "https://unsplash.com/collections/296",
        "photos": "https://api.unsplash.com/collections/296/photos",
        "related": "https://api.unsplash.com/collections/296/related"
     }
     */

    public int id;
    public String title;
    public String description;
    public String published_at;
    public String updated_at;
    public boolean curated;
    public int total_photos;
    @SerializedName("private")
    public boolean privateX;
    public String share_key;

    public Photo cover_photo;

    public User user;

    public CollectionLinks links;

    public List<Tag> tags;
}
