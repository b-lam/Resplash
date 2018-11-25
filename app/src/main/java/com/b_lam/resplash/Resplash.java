package com.b_lam.resplash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatDelegate;

import android.util.Log;

import com.b_lam.resplash.activities.MainActivity;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.User;

import java.util.ArrayList;
import java.util.List;

import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.b_lam.resplash.util.Utils;

/**
 * Created by Brandon on 10/6/2016.
 */

public class Resplash extends Application{

    private List<Activity> activityList;
    private Photo photo;
    private Collection collection;
    private User user;
    private Drawable drawable;
    private boolean myOwnCollection = false;
    private boolean activityInBackstage = false;
    private static String TAG = "Resplash";

    public static final String GOOGLE_PLAY_LICENSE_KEY = BuildConfig.GOOGLE_PLAY_LICENSE_KEY;

    // Unsplash url.
    public static final String UNSPLASH_API_BASE_URL = "https://api.unsplash.com/";
    public static final String UNSPLASH_URL = "https://unsplash.com/";
    public static final String UNSPLASH_UPLOAD_URL = "https://unsplash.com/submit";
    public static final String UNSPLASH_JOIN_URL = "https://unsplash.com/join";
    public static final String UNSPLASH_LOGIN_CALLBACK = "unsplash-auth-callback";
    public static final String UNSPLASH_UTM_PARAMETERS = "?utm_source=resplash&utm_medium=referral&utm_campaign=api-credit";

    public static final String DATE_FORMAT = "yyyy/MM/dd";
    public static final String DOWNLOAD_PATH = "/Pictures/Resplash/";
    public static final String DOWNLOAD_PHOTO_FORMAT = ".jpg";

    public static final int DEFAULT_PER_PAGE = 30;
    public static final int SEARCH_PER_PAGE = 20;

    public static final String RESPLASH_USER_GROUP = "resplash_user_group";

    // permission code.
    public static final int WRITE_EXTERNAL_STORAGE = 1;

    // Firebase
    public static final String FIREBASE_EVENT_LOGIN = "unsplash_login";
    public static final String FIREBASE_EVENT_VIEW_DONATE = "view_donate";
    public static final String FIREBASE_EVENT_VIEW_ABOUT = "view_about";
    public static final String FIREBASE_EVENT_RATE_FROM_APP = "rate_from_app";
    public static final String FIREBASE_EVENT_DOWNLOAD = "download_photo";
    public static final String FIREBASE_EVENT_SET_WALLPAPER = "set_wallpaper";
    public static final String FIREBASE_EVENT_LIKE_PHOTO = "like_photo";
    public static final String FIREBASE_EVENT_SHARE_PHOTO = "share_photo";
    public static final String FIREBASE_EVENT_VIEW_PHOTO_STATS = "view_photo_stats";
    public static final String FIREBASE_EVENT_VIEW_PHOTO_INFO = "view_photo_info";
    public static final String FIREBASE_EVENT_CLEAR_CACHE = "clear_cache";

    /** <br> life cycle. */

    @Override
    public void onCreate() {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.ResplashTheme_Primary_Base_Dark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.ResplashTheme_Primary_Base_Black);
                break;
        }

        super.onCreate();
        initialize();
    }

    public static String getAppId(Context c) {
        if (isDebug(c)) {
            Log.d(TAG, "Using debug keys");
            return BuildConfig.DEV_APP_ID;
        } else if (Utils.getUserGroup() == 1){
            Log.d(TAG, "Using release keys 1");
            return BuildConfig.RELEASE_APP_ID_1;
        } else {
            Log.d(TAG, "Using release keys 2");
            return BuildConfig.RELEASE_APP_ID_2;
        }
    }

    public static String getSecret(Context c) {
        if (isDebug(c)) {
            return BuildConfig.DEV_SECRET;
        } else if (Utils.getUserGroup() == 1){
            return BuildConfig.RELEASE_SECRET_1;
        } else {
            return BuildConfig.RELEASE_SECRET_2;
        }
    }

    public static boolean isDebug(Context c) {
        try {
            return (c.getApplicationInfo().flags
                    & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception ignored) {

        }
        return false;
    }

    public static String getLoginUrl(Context c){
        return Resplash.UNSPLASH_URL + "oauth/authorize"
                + "?client_id=" + Resplash.getAppId(c)
                + "&redirect_uri=" + "resplash%3A%2F%2F" + Resplash.UNSPLASH_LOGIN_CALLBACK
                + "&response_type=" + "code"
                + "&scope=" + "public+read_user+write_user+read_photos+write_photos+write_likes+read_collections+write_collections";
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();
        LocaleUtils.loadLocale(this);
    }

    /** <br> data. */

    public void addActivity(Activity a) {
        activityList.add(a);
    }

    public void removeActivity() {
        activityList.remove(activityList.size() - 1);
    }
    /*
        public List<Activity> getActivityList() {
            return activityList;
        }
    */
    public Activity getLatestActivity() {
        if (activityList.size() > 0) {
            return activityList.get(activityList.size() - 1);
        } else {
            return null;
        }
    }

    public MainActivity getMainActivity() {
        if (activityList.get(0) instanceof MainActivity) {
            return (MainActivity) activityList.get(0);
        } else {
            return null;
        }
    }

    public int getActivityCount() {
        return activityList.size();
    }

    public void setPhoto(Photo p) {
        this.photo = p;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setCollection(Collection c) {
        this.collection = c;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setUser(User u) {
        this.user = u;
    }

    public User getUser() {
        return user;
    }

    public void setDrawable(Drawable d) {
        this.drawable = d;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setMyOwnCollection(boolean own) {
        this.myOwnCollection = own;
    }

    public boolean isMyOwnCollection() {
        return myOwnCollection;
    }

    public void setActivityInBackstage(boolean showing) {
        this.activityInBackstage = showing;
    }

    public boolean isActivityInBackstage() {
        return activityInBackstage;
    }

    /** <br> singleton. */

    private static Resplash instance;

    public static Resplash getInstance() {
        return instance;
    }

}
