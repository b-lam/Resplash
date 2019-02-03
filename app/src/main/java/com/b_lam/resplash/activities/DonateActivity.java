package com.b_lam.resplash.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.util.billing.IabBroadcastReceiver;
import com.b_lam.resplash.util.billing.IabHelper;
import com.b_lam.resplash.util.billing.IabResult;
import com.b_lam.resplash.util.billing.Inventory;
import com.b_lam.resplash.util.billing.Purchase;
import com.b_lam.resplash.util.billing.SkuDetails;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Arrays;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;


public class DonateActivity extends BaseActivity implements View.OnClickListener, IabBroadcastReceiver.IabBroadcastListener{

    @BindView(R.id.donate_close_btn) ImageButton btnClose;
    @BindView(R.id.donate_thanks) ConstraintLayout mThanksView;
    @BindView(R.id.donate_loading) LinearLayout mLoadingProgress;
    @BindView(R.id.donate_products_card) CardView mProductCard;
    @BindView(R.id.donate_item1_price) TextView mProduct1Price;
    @BindView(R.id.donate_item2_price) TextView mProduct2Price;
    @BindView(R.id.donate_item3_price) TextView mProduct3Price;
    @BindView(R.id.donate_item4_price) TextView mProduct4Price;
    @BindView(R.id.donate_thanks_animation) LottieAnimationView mAnimation;

    static final String TAG = "DonateActivity";

    static final String DONATION_MADE_KEY = "donation_made";

    static final String SKU_COFFEE = "coffee";
    static final String SKU_SMOOTHIE = "smoothie";
    static final String SKU_PIZZA = "pizza";
    static final String SKU_MEAL = "meal";

    static final int RC_REQUEST = 10001;

    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donate);

        ButterKnife.bind(this);

        btnClose.setOnClickListener(this);

        String base64EncodedPublicKey = Resplash.GOOGLE_PLAY_LICENSE_KEY;

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        final List<String> skus = Arrays.asList(SKU_COFFEE, SKU_SMOOTHIE, SKU_PIZZA, SKU_MEAL);

        if (Resplash.isDebug(getApplicationContext())){
            mHelper.enableDebugLogging(true);
        }else{
            mHelper.enableDebugLogging(false);
        }

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(result -> {
            Log.d(TAG, "Setup finished.");

            if (!result.isSuccess()) {
                complain("Problem setting up in-app billing: " + result);
                return;
            }

            if (mHelper == null) return;

            mBroadcastReceiver = new IabBroadcastReceiver(DonateActivity.this);
            IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
            registerReceiver(mBroadcastReceiver, broadcastFilter);

            Log.d(TAG, "Setup successful. Querying inventory.");
            try {
                mHelper.queryInventoryAsync(true, skus, null, mGotInventoryListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error querying inventory. Another async operation in progress.");
            }
        });

        LinearLayout[] containers = new LinearLayout[] {
                findViewById(R.id.container_donate_item1),
                findViewById(R.id.container_donate_item2),
                findViewById(R.id.container_donate_item3),
                findViewById(R.id.container_donate_item4)};
        for (LinearLayout r : containers) {
            r.setOnClickListener(this);
        }

        mAnimation.setOnClickListener(view -> mAnimation.playAnimation());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_VIEW_DONATE, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            try {
                mHelper.disposeWhenFinished();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.container_donate_item1:
                buyItem(SKU_COFFEE);
                break;

            case R.id.container_donate_item2:
                buyItem(SKU_SMOOTHIE);
                break;

            case R.id.container_donate_item3:
                buyItem(SKU_PIZZA);
                break;

            case R.id.container_donate_item4:
                buyItem(SKU_MEAL);
                break;

            case R.id.donate_close_btn:
                finish();
                break;
        }
    }

    public void buyItem(String SKU){
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU, RC_REQUEST, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            Purchase coffee = inventory.getPurchase(SKU_COFFEE);
            Purchase smoothie = inventory.getPurchase(SKU_SMOOTHIE);
            Purchase pizza = inventory.getPurchase(SKU_PIZZA);
            Purchase meal = inventory.getPurchase(SKU_MEAL);

            SkuDetails skuDetailsCoffee = inventory.getSkuDetails(SKU_COFFEE);
            SkuDetails skuDetailsSmoothie = inventory.getSkuDetails(SKU_SMOOTHIE);
            SkuDetails skuDetailsPizza = inventory.getSkuDetails(SKU_PIZZA);
            SkuDetails skuDetailsMeal = inventory.getSkuDetails(SKU_MEAL);

            if (skuDetailsCoffee != null && skuDetailsSmoothie != null && skuDetailsPizza != null && skuDetailsMeal != null) {
                mProduct1Price.setText(skuDetailsCoffee.getPrice());
                mProduct2Price.setText(skuDetailsSmoothie.getPrice());
                mProduct3Price.setText(skuDetailsPizza.getPrice());
                mProduct4Price.setText(skuDetailsMeal.getPrice());

                mLoadingProgress.setVisibility(View.GONE);
                mProductCard.setVisibility(View.VISIBLE);
            }

            if (coffee != null || smoothie != null || pizza != null || meal != null ||
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(DONATION_MADE_KEY, false)) {
                mThanksView.setVisibility(View.VISIBLE);
            }

            // Consume any unconsumed purchases
            try {
                if (coffee != null) mHelper.consumeAsync(coffee, mConsumeFinishedListener);
                if (smoothie != null) mHelper.consumeAsync(smoothie, mConsumeFinishedListener);
                if (pizza != null) mHelper.consumeAsync(pizza, mConsumeFinishedListener);
                if (meal != null) mHelper.consumeAsync(meal, mConsumeFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Donation made: " + result + ", donation: " + purchase);

            if (mHelper == null) return;

            if (result.isSuccess()) {
                showThanksDialog();

                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            } else if (result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED){
                complain(result.toString());
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = (purchase, result) -> {
        if (result.isSuccess()) {
            Log.d(TAG, "Donation consumed: " + result + ", donation: " + purchase);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DONATION_MADE_KEY, true);
            editor.apply();
        } else {
            complain(result.toString());
        }
    };

    @Override
    public void receivedBroadcast(){
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    void complain(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Resplash Error: " + message);
    }

    void showThanksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.donate_thanks_layout, null);
        final LottieAnimationView animationView = view.findViewById(R.id.donate_thanks_animation);
        animationView.setOnClickListener(v -> animationView.playAnimation());
        builder.setView(view);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}
