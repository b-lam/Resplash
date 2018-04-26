package com.b_lam.resplash.data.data;

/**
 * Category
 **/

public class Category {

    /**
     "id": 6,
     "title": "People",
     "photo_count": 9844,
     "links": {
        "self": "https://api.unsplash.com/categories/6",
        "photos": "https://api.unsplash.com/categories/6/photos"
     }
     */

    public int id;
    public String title;
    public int photo_count;

    public CategoryLinks links;
}
