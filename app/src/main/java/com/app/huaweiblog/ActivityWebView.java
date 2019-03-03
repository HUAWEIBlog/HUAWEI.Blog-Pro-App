package com.app.huaweiblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.app.huaweiblog.utils.Tools;

public class ActivityWebView extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    public static void navigate(AppCompatActivity activity, String url) {
        Intent intent = new Intent(activity, ActivityWebView.class);
        intent.putExtra(EXTRA_OBJC, url);
        activity.startActivity(intent);
    }

    private Toolbar toolbar;
    private ActionBar actionBar;

    private WebView webView;
    private String url;
    private View parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        parent_view = findViewById(android.R.id.content);

        webView = (WebView) findViewById(R.id.webView);

        // get extra object
        url = getIntent().getStringExtra(EXTRA_OBJC);
        initToolbar();
        loadWebFromUrl();


        // analytics tracking
        ThisApplication.getInstance().trackScreenView("WebView : " + url);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, android.R.style.TextAppearance_Material_Subhead);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.activity_title_webview);
    }

    private void loadWebFromUrl() {
        webView.loadUrl("about:blank");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings();
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                actionBar.setTitle(getString(R.string.webview_loading) + progress + " %");
                if (progress == 100) {
                    actionBar.setTitle(R.string.activity_title_webview);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_refresh) {
            loadWebFromUrl();
        } else if (item.getItemId() == R.id.action_browser) {
            Tools.directLinkToBrowser(this, url);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_webview, menu);
        return true;
    }

    @Override
    protected void onResume() {
        webView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }
}
