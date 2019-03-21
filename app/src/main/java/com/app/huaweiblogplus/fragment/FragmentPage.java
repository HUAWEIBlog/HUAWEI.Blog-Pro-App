package com.app.huaweiblogplus.plus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.huaweiblogplus.plus.ActivityMain;
import com.app.huaweiblogplus.plus.ActivityPageDetails;
import com.app.huaweiblogplus.plus.R;
import com.app.huaweiblogplus.plus.adapter.AdapterPageList;
import com.app.huaweiblogplus.plus.connection.API;
import com.app.huaweiblogplus.plus.connection.RestAdapter;
import com.app.huaweiblogplus.plus.connection.callbacks.CallbackListPage;
import com.app.huaweiblogplus.plus.model.Page;
import com.app.huaweiblogplus.plus.utils.NetworkCheck;
import com.app.huaweiblogplus.plus.utils.Tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentPage extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterPageList mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private Call<CallbackListPage> callbackCall = null;

    private Set<Long> pageIdFilter = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_page, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        swipe_refresh = (SwipeRefreshLayout) root_view.findViewById(R.id.swipe_refresh_layout_page);
        recyclerView = (RecyclerView) root_view.findViewById(R.id.recyclerViewPage);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterPageList(getActivity(), new ArrayList<Page>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterPageList.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Page obj, int position) {
                ActivityPageDetails.navigate((ActivityMain) getActivity(), v.findViewById(R.id.image), obj);
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                mAdapter.resetListData();
                requestAction();
            }
        });

        requestAction();

        return root_view;
    }

    private void displayApiResult(final List<Page> pages) {
        // filtering page
        String page_id[] = getResources().getStringArray(R.array.page_id_filter);
        for (String i : page_id) pageIdFilter.add(Long.parseLong(i));

        Iterator<Page> iterator = pages.iterator();
        while (iterator.hasNext() && pageIdFilter.size() > 0) {
            Page p = iterator.next();
            if (pageIdFilter.contains(p.id)) {
                pageIdFilter.remove(p.id);
                iterator.remove();
            }
        }

        mAdapter.insertData(Tools.getSortedPageById(pages));
        swipeProgress(false);
        if (pages.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi() {
        API api = RestAdapter.createAPI();
        callbackCall = api.getPagesByPage();
        callbackCall.enqueue(new Callback<CallbackListPage>() {
            @Override
            public void onResponse(Call<CallbackListPage> call, Response<CallbackListPage> response) {
                CallbackListPage resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.pages);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackListPage> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        requestListPostApi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = (View) root_view.findViewById(R.id.lyt_failed_page);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        ((Button) root_view.findViewById(R.id.failed_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = (View) root_view.findViewById(R.id.lyt_no_item_page);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_page);
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

}

