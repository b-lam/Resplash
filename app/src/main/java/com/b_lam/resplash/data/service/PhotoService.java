package com.b_lam.resplash.data.service;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.api.PhotoApi;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.LikePhotoResult;
import com.b_lam.resplash.data.data.Me;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.PhotoDetails;
import com.b_lam.resplash.data.data.PhotoStats;
import com.b_lam.resplash.data.data.User;
import com.b_lam.resplash.data.tools.AuthInterceptor;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Photo service.
 * */

public class PhotoService {
    // widget
    private Call call;

    /** <br> data. */

    public void requestPhotos(int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getPhotos = buildApi(buildClient()).getPhotos(page, per_page, order_by);
        getPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
        call = getPhotos;
    }

    public void requestCuratedPhotos(int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getCuratedPhotos = buildApi(buildClient()).getCuratedPhotos(page, per_page, order_by);
        getCuratedPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
        call = getCuratedPhotos;
    }

    public void requestStats(String id, final OnRequestStatsListener l) {
        Call<PhotoStats> getStats = buildApi(buildClient()).getPhotoStats(id);
        getStats.enqueue(new Callback<PhotoStats>() {
            @Override
            public void onResponse(Call<PhotoStats> call, Response<PhotoStats> response) {
                if (l != null) {
                    l.onRequestStatsSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<PhotoStats> call, Throwable t) {
                if (l != null) {
                    l.onRequestStatsFailed(call, t);
                }
            }
        });
        call = getStats;
    }

    public void requestPhotosInAGivenCategory(int id, int page, int per_page, final OnRequestPhotosListener l) {
        Call<List<Photo>> getPhotosInAGivenCategory = buildApi(buildClient()).getPhotosInAGivenCategory(id, page, per_page);
        getPhotosInAGivenCategory.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
        call = getPhotosInAGivenCategory;
    }

    public void setLikeForAPhoto(String id, boolean like, final OnSetLikeListener l) {
        Call<LikePhotoResult> setLikeForAPhoto = like ?
                buildApi(buildClient()).likeAPhoto(id) : buildApi(buildClient()).unlikeAPhoto(id);
        setLikeForAPhoto.enqueue(new Callback<LikePhotoResult>() {
            @Override
            public void onResponse(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
                if (l != null) {
                    l.onSetLikeSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<LikePhotoResult> call, Throwable t) {
                if (l != null) {
                    l.onSetLikeFailed(call, t);
                }
            }
        });
    }

    public void requestPhotoDetails(String id, final OnRequestPhotoDetailsListener l) {
        Call<PhotoDetails> getAPhoto = buildApi(buildClient()).getAPhoto(id);
        getAPhoto.enqueue(new Callback<PhotoDetails>() {
            @Override
            public void onResponse(Call<PhotoDetails> call, Response<PhotoDetails> response) {
                if (l != null) {
                    l.onRequestPhotoDetailsSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<PhotoDetails> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotoDetailsFailed(call, t);
                }
            }
        });
    }

    public void requestUserPhotos(User u, int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getUserPhotos = buildApi(buildClient()).getUserPhotos(u.username, page, per_page, order_by);
        getUserPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void requestUserPhotos(Me me, int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getUserPhotos = buildApi(buildClient()).getUserPhotos(me.username, page, per_page, order_by);
        getUserPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void requestUserLikes(User u, int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getUserLikes = buildApi(buildClient()).getUserLikes(u.username, page, per_page, order_by);
        getUserLikes.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void requestUserLikes(Me me, int page, int per_page, String order_by, final OnRequestPhotosListener l) {
        Call<List<Photo>> getUserLikes = buildApi(buildClient()).getUserLikes(me.username, page, per_page, order_by);
        getUserLikes.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void requestCollectionPhotos(Collection c, int page, int per_page, final OnRequestPhotosListener l) {
        Call<List<Photo>> getCollectionPhotos = buildApi(buildClient()).getCollectionPhotos(c.id, page, per_page);
        getCollectionPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void requestCuratedCollectionPhotos(Collection c, int page, int per_page, final OnRequestPhotosListener l) {
        Call<List<Photo>> getCuratedCollectionPhotos = buildApi(buildClient()).getCuratedCollectionPhotos(c.id, page, per_page);
        getCuratedCollectionPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }



    public void requestRandomPhotos(Integer categoryId, Boolean featured,
                                     String username, String query,
                                     String orientation, final OnRequestPhotosListener l) {
        Call<List<Photo>> getRandomPhotos = buildApi(buildClient()).getRandomPhotos(
                categoryId, featured,
                username, query,
                orientation, Resplash.DEFAULT_PER_PAGE);
        getRandomPhotos.enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (l != null) {
                    l.onRequestPhotosSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                if (l != null) {
                    l.onRequestPhotosFailed(call, t);
                }
            }
        });
    }

    public void reportDownload(String id, final OnReportDownloadListener l) {
        Call<ResponseBody> reportDownload = buildApi(buildClient()).reportDownload(id);
        reportDownload.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (l != null) {
                    l.onReportDownloadSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (l != null) {
                    l.onReportDownloadFailed(call, t);
                }
            }
        });
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    /** <br> build. */

    public static PhotoService getService() {
        return new PhotoService();
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    private PhotoApi buildApi(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(Resplash.UNSPLASH_API_BASE_URL)
                .client(client)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                new GsonBuilder()
                                        .setDateFormat(Resplash.DATE_FORMAT)
                                        .create()))
                .build()
                .create((PhotoApi.class));
    }

    /** <br> interface. */

    public interface OnRequestPhotosListener {
        void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response);
        void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t);
    }

    public interface OnRequestStatsListener {
        void onRequestStatsSuccess(Call<PhotoStats> call, Response<PhotoStats> response);
        void onRequestStatsFailed(Call<PhotoStats> call, Throwable t);
    }

    public interface OnSetLikeListener {
        void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response);
        void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t);
    }

    public interface OnRequestPhotoDetailsListener {
        void onRequestPhotoDetailsSuccess(Call<PhotoDetails> call, Response<PhotoDetails> response);
        void onRequestPhotoDetailsFailed(Call<PhotoDetails> call, Throwable t);
    }

    public interface OnReportDownloadListener {
        void onReportDownloadSuccess(Call<ResponseBody> call, Response<ResponseBody> response);
        void onReportDownloadFailed(Call<ResponseBody> call, Throwable t);
    }
}
