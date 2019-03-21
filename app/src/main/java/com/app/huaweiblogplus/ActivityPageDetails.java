package com.app.huaweiblogplus;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.huaweiblogplus.connection.API;
import com.app.huaweiblogplus.connection.RestAdapter;
import com.app.huaweiblogplus.connection.callbacks.CallbackDetailsPage;
import com.app.huaweiblogplus.data.AppConfig;
import com.app.huaweiblogplus.data.Constant;
import com.app.huaweiblogplus.data.GDPR;
import com.app.huaweiblogplus.data.SharedPref;
import com.app.huaweiblogplus.model.Page;
import com.app.huaweiblogplus.utils.NetworkCheck;
import com.app.huaweiblogplus.utils.Tools;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPageDetails extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View transitionView, Page obj) {
        Intent intent = new Intent(activity, ActivityPageDetails.class);
        intent.putExtra(EXTRA_OBJC, obj);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    private Toolbar toolbar;
    private ActionBar actionBar;
    private View parent_view;
    private View lyt_parent;
    private View lyt_image_header;
    private SwipeRefreshLayout swipe_refresh;

    // extra obj
    private Page page;

    private SharedPref sharedPref;
    private Call<CallbackDetailsPage> callbackCall = null;

    // for fullscreen video
    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private CustomWebChromeClient customWebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_details);

        parent_view = findViewById(android.R.id.content);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        webview = (WebView) findViewById(R.id.content);
        lyt_parent = findViewById(R.id.lyt_parent);
        lyt_image_header = findViewById(R.id.lyt_image_header);

        sharedPref = new SharedPref(this);

        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.image), EXTRA_OBJC);

        Log.d("Page", "Before:" + page);

        // get extra object
        page = (Page) getIntent().getSerializableExtra(EXTRA_OBJC);

        Log.d("Page", "After:" + page);

        initToolbar();

        displayPageData(true);
        prepareAds();

        if (page.isDraft()) requestAction();

        // on swipe
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAction();
            }
        });

        // get enabled controllers
        Tools.requestInfoApi(this);

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View page : " + page.title_plain);

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }

    private void requestDetailsPageApi() {
        API api = RestAdapter.createAPI();
        callbackCall = api.getPageDetailsById(page.id);
        callbackCall.enqueue(new Callback<CallbackDetailsPage>() {
            @Override
            public void onResponse(Call<CallbackDetailsPage> call, Response<CallbackDetailsPage> response) {
                CallbackDetailsPage resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    page = resp.page;
                    displayPageData(false);
                    swipeProgress(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackDetailsPage> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestDetailsPageApi();
            }
        }, Constant.DELAY_TIME_MEDIUM);
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private WebView webview;

    private void displayPageData(boolean is_draft) {
        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        ((TextView) findViewById(R.id.title)).setText(Html.fromHtml(page.title));

        String html_data = "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> ";
        html_data += page.content;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings();
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        customWebChromeClient = new CustomWebChromeClient();
        webview.setWebChromeClient(customWebChromeClient);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Tools.directLinkToBrowser(ActivityPageDetails.this, url);
                return true;
            }
        });
        webview.loadData(html_data, "text/html; charset=UTF-8", null);
        // disable scroll on touch
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        ((TextView) findViewById(R.id.date)).setText(Tools.getFormatedDate(page.date));
        Tools.displayImageThumbnailPage(this, page, ((ImageView) findViewById(R.id.image)));

        if (is_draft) {
            return;
        }

        lyt_image_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thumb = Tools.getPageThumbnailUrl(page);
                if (URLUtil.isValidUrl(thumb)) {
                    ActivityFullScreenImage.navigate(ActivityPageDetails.this, thumb);
                }
            }
        });

        //Snackbar.make(parent_view, R.string.page_detail_displayed_msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        } else if (item_id == R.id.action_browser) {
            Tools.directLinkToBrowser(this, page.url);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_page_details, menu);
        return true;
    }

    private void prepareAds() {
        if (AppConfig.ENABLE_ADSENSE && NetworkCheck.isConnect(getApplicationContext())) {
            AdView mAdView = (AdView) findViewById(R.id.ad_view);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        } else {
            ((RelativeLayout) findViewById(R.id.banner_layout)).setVisibility(View.GONE);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) findViewById(R.id.lyt_failed);
        View lyt_main_content = (View) findViewById(R.id.lyt_main_content);

        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_main_content.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_main_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        ((Button) findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            return;
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        webview.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        webview.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCustomView != null) {
            customWebChromeClient.onHideCustomView();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                customWebChromeClient.onHideCustomView();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    class CustomWebChromeClient extends WebChromeClient {
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            // landscape and fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Tools.toggleFullScreenActivity(ActivityPageDetails.this, true);

            mCustomView = view;
            lyt_parent.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(ActivityPageDetails.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCustomView == null) return;

            // revert landscape and fullscreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Tools.toggleFullScreenActivity(ActivityPageDetails.this, false);

            lyt_parent.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;
        }
    }

}
