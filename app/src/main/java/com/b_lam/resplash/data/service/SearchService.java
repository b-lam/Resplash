package com.b_lam.resplash.data.service;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.api.SearchApi;
import com.b_lam.resplash.data.data.SearchCollectionsResult;
import com.b_lam.resplash.data.data.SearchPhotosResult;
import com.b_lam.resplash.data.data.SearchUsersResult;
import com.b_lam.resplash.data.tools.AuthInterceptor;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Search service.
 * */

public class SearchService {
    // widget
    private Call call;

    /** <br> data. */

    public void searchPhotos(String query, int page, int per_page, String collections, String orientation, final OnRequestPhotosListener l) {
        Call<SearchPhotosResult> searchPhotos = buildApi(buildClient()).searchPhotos(query, page, per_page, collections, orientation);
        searchPhotos.enqueue(new Callback<SearchPhotosResult>() {
            @Override
            public void onResponse(Call<SearchPhotosResult> call, retrofit2.Response<SearchPhotosResult> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<SearchPhotosResult> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
        call = searchPhotos;
    }

    public void searchUsers(String query, int page, int per_page, final OnRequestUsersListener l) {
        Call<SearchUsersResult> searchUsers = buildApi(buildClient()).searchUsers(query, page, per_page);
        searchUsers.enqueue(new Callback<SearchUsersResult>() {
            @Override
            public void onResponse(Call<SearchUsersResult> call, retrofit2.Response<SearchUsersResult> response) {
                if (l != null) {
                    l.onRequestUsersSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<SearchUsersResult> call, Throwable t) {
                if (l != null) {
                    l.onRequestUsersFailed(call, t);
                }
            }
        });
        call = searchUsers;
    }

    public void searchCollections(String query, int page, int per_page, final OnRequestCollectionsListener l) {
        Call<SearchCollectionsResult> searchCollections = buildApi(buildClient()).searchCollections(query, page, per_page);
        searchCollections.enqueue(new Callback<SearchCollectionsResult>() {
            @Override
            public void onResponse(Call<SearchCollectionsResult> call, retrofit2.Response<SearchCollectionsResult> response) {
                if (l != null) {
                    l.onRequestCollectionsSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<SearchCollectionsResult> call, Throwable t) {
                if (l != null) {
                    l.onRequestCollectionsFailed(call, t);
                }
            }
        });
        call = searchCollections;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    /** <br> build. */

    public static SearchService getService() {
        return new SearchService();
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    private SearchApi buildApi(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(Resplash.UNSPLASH_API_BASE_URL)
                .client(client)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder()
                                        .setDateFormat(Resplash.DATE_FORMAT)
                                        .create()))
                .build()
                .create((SearchApi.class));
    }

    /** <br> interface. */

    public interface OnRequestPhotosListener {
        void onRequestPhotosSuccess(Call<SearchPhotosResult> call, retrofit2.Response<SearchPhotosResult> response);
        void onRequestPhotosFailed(Call<SearchPhotosResult> call, Throwable t);
    }

    public interface OnRequestCollectionsListener {
        void onRequestCollectionsSuccess(Call<SearchCollectionsResult> call, retrofit2.Response<SearchCollectionsResult> response);
        void onRequestCollectionsFailed(Call<SearchCollectionsResult> call, Throwable t);
    }

    public interface OnRequestUsersListener {
        void onRequestUsersSuccess(Call<SearchUsersResult> call, retrofit2.Response<SearchUsersResult> response);
        void onRequestUsersFailed(Call<SearchUsersResult> call, Throwable t);
    }
}
