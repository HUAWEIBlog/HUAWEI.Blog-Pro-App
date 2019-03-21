package com.app.huaweiblogplus.plus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.huaweiblogplus.plus.adapter.AdapterPostListModern;
import com.app.huaweiblogplus.plus.connection.API;
import com.app.huaweiblogplus.plus.connection.RestAdapter;
import com.app.huaweiblogplus.plus.connection.callbacks.CallbackCategoryDetails;
import com.app.huaweiblogplus.plus.data.Constant;
import com.app.huaweiblogplus.plus.model.Category;
import com.app.huaweiblogplus.plus.model.Post;
import com.app.huaweiblogplus.plus.utils.NetworkCheck;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryTippsDetails extends AppCompatActivity {

        public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

        // give preparation animation activity transition
        public static void navigate(AppCompatActivity activity, View transitionView, Category obj) {
            Intent intent = new Intent(activity, com.app.huaweiblogplus.plus.ActivityCategoryEnterpriseDetails.class);
            intent.putExtra(EXTRA_OBJC, obj);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC);
            ActivityCompat.startActivity(activity, intent, options.toBundle());
        }

        private Toolbar toolbar;
        private ActionBar actionBar;

        private RecyclerView recyclerView;
        private AdapterPostListModern mAdapter;
        private SwipeRefreshLayout swipe_refresh;
        private Call<CallbackCategoryDetails> callbackCall = null;

        private String category = "tweaks";

        // extra obj
        //private Category category;
        private View parent_view;

        private long post_total = 0;
        private long failed_page = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_category_modern_details );
            parent_view = findViewById(android.R.id.content);
            // animation transition
            ViewCompat.setTransitionName(findViewById(R.id.toolbar), EXTRA_OBJC);

            // get extra object
            category = (String) getIntent().getSerializableExtra(EXTRA_OBJC);
            post_total = 100;

            swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);

            //set data and list adapter
            mAdapter = new AdapterPostListModern(this, recyclerView, new ArrayList<Post>());
            recyclerView.setAdapter(mAdapter);

            // on item list clicked
            mAdapter.setOnItemClickListener(new AdapterPostListModern.OnItemClickListener() {
                @Override
                public void onItemClick(View v, Post obj, int position) {
                    ActivityPostDetails.navigate( com.app.huaweiblogplus.plus.ActivityCategoryTippsDetails.this, v.findViewById(R.id.image), obj);
                }
            });

            // detect when scroll reach bottom
            mAdapter.setOnLoadMoreListener(new AdapterPostListModern.OnLoadMoreListener() {
                @Override
                public void onLoadMore(int current_page) {
                    if (post_total > mAdapter.getItemCount() && current_page != 0) {
                        int next_page = current_page + 1;
                        requestAction(next_page);
                    } else {
                        mAdapter.setLoaded();
                    }
                }
            });

            // on swipe list
            swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (callbackCall != null && callbackCall.isExecuted()) {
                        callbackCall.cancel();
                    }
                    mAdapter.resetListData();
                    requestAction(1);
                }
            });

            requestAction(1);

            initToolbar();

            // analytics tracking
            ThisApplication.getInstance().trackScreenView("View category : Tweaks");
        }

        private void initToolbar() {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle( Html.fromHtml("Tipps & Tricks"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            } else {
                Snackbar.make(parent_view, item.getTitle() + " clicked", Snackbar.LENGTH_SHORT).show();
            }
            return super.onOptionsItemSelected(item);
        }

        private void displayApiResult(final List<Post> posts) {
            mAdapter.insertData(posts);
            swipeProgress(false);
            if (posts.size() == 0) {
                showNoItemView(true);
            }
        }

        private void requestPostApi(final long page_no) {
            API api = RestAdapter.createAPI();
            callbackCall = api.getCategoryDetailsByPage(2040, page_no, Constant.POST_PER_REQUEST);
            callbackCall.enqueue(new Callback<CallbackCategoryDetails>() {
                @Override
                public void onResponse(Call<CallbackCategoryDetails> call, Response<CallbackCategoryDetails> response) {
                    CallbackCategoryDetails resp = response.body();
                    if (resp != null && resp.status.equals("ok")) {
                        displayApiResult(resp.posts);
                    } else {
                        onFailRequest(page_no);
                    }
                }

                @Override
                public void onFailure(Call<CallbackCategoryDetails> call, Throwable t) {
                    if (!call.isCanceled()) onFailRequest(page_no);
                }

            });
        }

        private void onFailRequest(long page_no) {
            failed_page = page_no;
            mAdapter.setLoaded();
            swipeProgress(false);
            if (NetworkCheck.isConnect(getApplicationContext())) {
                showFailedView(true, getString(R.string.failed_text));
            } else {
                showFailedView(true, getString(R.string.no_internet_text));
            }
        }

        private void requestAction(final long page_no) {
            showFailedView(false, "");
            showNoItemView(false);
            if (page_no == 1) {
                swipeProgress(true);
            } else {
                mAdapter.setLoading();
            }
            new Handler().postDelayed( new Runnable() {
                @Override
                public void run() {
                    requestPostApi(page_no);
                }
            }, Constant.DELAY_TIME);
        }

        private void showFailedView(boolean show, String message) {
            View lyt_failed = (View) findViewById(R.id.lyt_failed);
            ((TextView) findViewById(R.id.failed_message)).setText(message);
            if (show) {
                recyclerView.setVisibility(View.GONE);
                lyt_failed.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                lyt_failed.setVisibility(View.GONE);
            }
            ((Button) findViewById(R.id.failed_retry)).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestAction(failed_page);
                }
            });
        }

        private void showNoItemView(boolean show) {
            View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
            ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post);
            if (show) {
                recyclerView.setVisibility(View.GONE);
                lyt_no_item.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                lyt_no_item.setVisibility(View.GONE);
            }
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
        public void onDestroy() {
            super.onDestroy();
            swipeProgress(false);
            if (callbackCall != null && callbackCall.isExecuted()) {
                callbackCall.cancel();
            }
        }
    }

