package io.github.b_lam.resplash.Data.Service;


import io.github.b_lam.resplash.Data.Api.AuthorizeApi;
import io.github.b_lam.resplash.Data.Data.AccessToken;
import io.github.b_lam.resplash.Resplash;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Authorize service.
 * */

public class AuthorizeService {
    // widget
    private Call call;

    /** <br> data. */

    public void requestAccessToken(String code, final OnRequestAccessTokenListener l) {
        Call<AccessToken> getAccessToken = buildApi()
                .getAccessToken(
                        Resplash.APPLICATION_ID,
                        Resplash.SECRET,
                        "mysplash://" + Resplash.UNSPLASH_LOGIN_CALLBACK,
                        code,
                        "authorization_code");
        getAccessToken.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (l != null) {
                    l.onRequestAccessTokenSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                if (l != null) {
                    l.onRequestAccessTokenFailed(call, t);
                }
            }
        });
        call = getAccessToken;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    /** <br> build. */

    public static AuthorizeService getService() {
        return new AuthorizeService();
    }

    private AuthorizeApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(Resplash.UNSPLASH_AUTH_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((AuthorizeApi.class));
    }

    /** <br> interface. */

    public interface OnRequestAccessTokenListener {
        void onRequestAccessTokenSuccess(Call<AccessToken> call, retrofit2.Response<AccessToken> response);
        void onRequestAccessTokenFailed(Call<AccessToken> call, Throwable t);
    }
}
