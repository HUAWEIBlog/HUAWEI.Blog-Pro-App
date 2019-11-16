package com.app.huaweiblogplus;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.huaweiblogplus.adapter.AdapterComments;
import com.app.huaweiblogplus.connection.API;
import com.app.huaweiblogplus.connection.RestAdapter;
import com.app.huaweiblogplus.connection.callbacks.CallbackDetailsPost;
import com.app.huaweiblogplus.data.AppConfig;
import com.app.huaweiblogplus.data.Constant;
import com.app.huaweiblogplus.data.GDPR;
import com.app.huaweiblogplus.data.SharedPref;
import com.app.huaweiblogplus.model.Comment;
import com.app.huaweiblogplus.model.Post;
import com.app.huaweiblogplus.realm.RealmController;
import com.app.huaweiblogplus.utils.NetworkCheck;
import com.app.huaweiblogplus.utils.Tools;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPostDetails extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    public static final String EXTRA_NOTIF = "key.EXTRA_NOTIF";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View transitionView, Post obj) {
        Intent intent = new Intent(activity, ActivityPostDetails.class);
        intent.putExtra(EXTRA_OBJC, obj);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    private Toolbar toolbar;
    private ActionBar actionBar;
    private View parent_view;
    private View lyt_parent;
    private View lyt_image_header;
    private MenuItem read_later_menu;
    private SwipeRefreshLayout swipe_refresh;

    // extra obj
    private Post post;
    private boolean from_notif;

    private SharedPref sharedPref;
    private boolean flag_read_later;
    private Call<CallbackDetailsPost> callbackCall = null;

    // for fullscreen video
    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private CustomWebChromeClient customWebChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        parent_view = findViewById(android.R.id.content);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        webview = (WebView) findViewById(R.id.content);
        lyt_parent = findViewById(R.id.lyt_parent);
        lyt_image_header = findViewById(R.id.lyt_image_header);

        sharedPref = new SharedPref(this);

        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.image), EXTRA_OBJC);

        // get extra object
        post = (Post) getIntent().getSerializableExtra(EXTRA_OBJC);
        from_notif = getIntent().getBooleanExtra(EXTRA_NOTIF, false);
        initToolbar();

        displayPostData(true);
        prepareAds();

        if (post.isDraft()) requestAction();

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
        ThisApplication.getInstance().trackScreenView("View post : " + post.title_plain);

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
    }

    private void requestDetailsPostApi() {
        API api = RestAdapter.createAPI();
        callbackCall = api.getPostDetailsById(post.id);
        callbackCall.enqueue(new Callback<CallbackDetailsPost>() {
            @Override
            public void onResponse(Call<CallbackDetailsPost> call, Response<CallbackDetailsPost> response) {
                CallbackDetailsPost resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    displayPostData(false);
                    swipeProgress(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackDetailsPost> call, Throwable t) {
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
                requestDetailsPostApi();
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

    private void displayPostData(boolean is_draft) {
        customViewContainer = (FrameLayout) findViewById( R.id.customViewContainer );
        ((TextView) findViewById( R.id.title )).setText( Html.fromHtml( post.title ) );

        String html_data = "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;} a:link { color: #e53232; } .wp-block-blockgallery-carousel, .wp-block-blockgallery-carousel .blockgallery { height: 100%; position: relative; margin-bottom: 400px !important; } .post-content, .post-share { line-height: 1.857; font-size: 16px !important; position: relative; }</style> " +
                "<script type='text/javascript' src='https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/themes/smart-mag/js/jquery.flexslider-min.js?ver=2.6.2'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/plugins/borlabs-cookie/javascript/borlabs-cookie.min.js?ver=1.9.7'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/themes/smart-mag/js/bunyad-theme.js?ver=2.6.2'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/plugins/live-blogging-plus/live-blogging.min.js?ver=5.0.3'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/themes/smart-mag/js/jquery.prettyPhoto.js?ver=5.0.3'></script>" +
                "<script type='text/javascript' src='https://www.huaweiblog.de/wp-content/plugins/ultimate-responsive-image-slider/js/jquery.sliderPro.js?ver=1.4.0'></script>" +
                "<script type=\"text/javascript\" src=\"https://www.huaweiblog.de/wp-content/plugins/aawp/public/assets/js/scripts.min.js?ver=3.8.9\"></script>" +
                "<link rel=\"stylesheet\" id=\"aawp-css\" href=\"https://www.huaweiblog.de/wp-content/plugins/aawp/public/assets/css/styles.min.css?ver=3.8.9\" type=\"text/css\" media=\"all\">" +
                "<link rel='stylesheet' id='smartmag-core-css'  href='https://www.huaweiblog.de/wp-content/themes/smart-mag-child/style.css?ver=2.6.2' type='text/css' media='all' />" +
                "<link rel='stylesheet' id='smartmag-responsive-css'  href='https://www.huaweiblog.de/wp-content/themes/smart-mag/css/responsive.css?ver=2.6.2' type='text/css' media='all' />" +
                "<link rel='stylesheet' id='pretty-photo-css'  href='https://www.huaweiblog.de/wp-content/themes/smart-mag/css/prettyPhoto.css?ver=2.6.2' type='text/css' media='all' />" +
                "<link rel='stylesheet' id='smartmag-font-awesome-css'  href='https://netdna.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.css?ver=5.0.3' type='text/css' media='all' />" +
                "<script type=\"text/javascript\" src=\"https://www.huaweiblog.de/wp-content/plugins/block-gallery/dist/js/vendors/flickity.min.js?ver=1.1.5\"></script>" +
                "<link rel=\"stylesheet\" id=\"block-gallery-frontend-css\" href=\"https://www.huaweiblog.de/wp-content/cache/asset-cleanup/css/min/block-gallery-frontend-v1.1.5.css\" type=\"text/css\" media=\"all\">" +
                "<link rel='stylesheet' id='ris-slider-css-css'  href='https://www.huaweiblog.de/wp-content/plugins/ultimate-responsive-image-slider/css/slider-pro.css?ver=5.0.3' type='text/css' media='all' />";
        html_data += post.content;
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings();
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setBackgroundColor(Color.TRANSPARENT);
        customWebChromeClient = new CustomWebChromeClient();
        webview.setWebChromeClient(customWebChromeClient);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Tools.directLinkToBrowser(ActivityPostDetails.this, url);
                return true;
            }
        });

        try {
            webview.loadData(URLEncoder.encode(html_data, "utf-8").replaceAll("\\+"," "), "text/html", "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // disable scroll on touch
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        ((TextView) findViewById(R.id.date)).setText(Tools.getFormatedDate(post.date));
        ((TextView) findViewById(R.id.comment)).setText(post.comment_count + "");
        ((TextView) findViewById(R.id.tv_comment)).setText(getString(R.string.show_tv_comments) + " (" + post.comment_count + ")");
        ((TextView) findViewById(R.id.category)).setText(Html.fromHtml(Tools.getCategoryTxt(post.categories)));
        ((TextView) findViewById(R.id.author)).setText(Tools.getAuthorTxt(Collections.singletonList(post.author)));
        Tools.displayImageThumbnail(this, post, ((ImageView) findViewById(R.id.image)));
        lyt_image_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String thumb = Tools.getPostThumbnailUrl(post);
                if (URLUtil.isValidUrl(thumb)) {
                    ActivityFullScreenImage.navigate(ActivityPostDetails.this, thumb);
                }
            }
        });

        if (is_draft) {
            return;
        }
        // when show comments click
        ((MaterialRippleLayout) findViewById(R.id.bt_show_comment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (post.comments.size() <= 0) {
                    Snackbar.make(parent_view, R.string.post_have_no_comment, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                dialogShowComments(post.comments);
            }
        });

        // when show social click
        ((MaterialRippleLayout) findViewById(R.id.bt_social)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.methodShare(ActivityPostDetails.this, post);
            }
        });

        // when post comments click
        ((MaterialRippleLayout) findViewById(R.id.bt_send_comment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AppConfig.MUST_REGISTER_TO_COMMENT) {
                    Intent i = new Intent(ActivityPostDetails.this, ActivityWebView.class);
                    if (sharedPref.isRespondEnabled()) {
                        i = new Intent(ActivityPostDetails.this, ActivitySendComment.class);
                    }
                    i.putExtra(EXTRA_OBJC, post);
                    startActivity(i);
                } else {
                    Tools.dialogCommentNeedLogin(ActivityPostDetails.this, post.url);
                }
            }
        });

        // small bar on footer
        //Snackbar.make(parent_view, R.string.post_detail_displayed_msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        } else if (item_id == R.id.action_share) {
            Tools.methodShare(ActivityPostDetails.this, post);
        } else if (item_id == R.id.action_later) {
            if (post.isDraft()) {
                Snackbar.make(parent_view, R.string.cannot_add_to_read_later, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            String str;
            if (flag_read_later) {
                RealmController.with(this).deletePost(post.id);
                str = getString(R.string.remove_from_msg);
            } else {
                RealmController.with(this).savePost(post);
                str = getString(R.string.added_to_msg);
            }
            Snackbar.make(parent_view, "Post " + str + " Read Later", Snackbar.LENGTH_SHORT).show();
            refreshReadLaterMenu();
        } else if (item_id == R.id.action_browser) {
            Tools.directLinkToBrowser(this, post.url);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_post_details, menu);
        read_later_menu = menu.findItem(R.id.action_later);
        refreshReadLaterMenu();
        return true;
    }

    private void dialogShowComments(List<Comment> items) {

        final Dialog dialog = new Dialog(ActivityPostDetails.this);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_comments);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        AdapterComments mAdapter = new AdapterComments(this, items);
        recyclerView.setAdapter(mAdapter);

        ((ImageView) dialog.findViewById(R.id.img_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void refreshReadLaterMenu() {
        flag_read_later = RealmController.with(this).getPost(post.id) != null;
        Drawable drawable = read_later_menu.getIcon();
        if (flag_read_later) {
            drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
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
        if (from_notif) {
            startActivity(new Intent(getApplicationContext(), ActivityMain.class));
            finish();
        } else {
            super.onBackPressed();
        }
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
            Tools.toggleFullScreenActivity(ActivityPostDetails.this, true);

            mCustomView = view;
            lyt_parent.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(ActivityPostDetails.this);
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
            Tools.toggleFullScreenActivity(ActivityPostDetails.this, false);

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