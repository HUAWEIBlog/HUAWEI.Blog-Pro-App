package com.app.huaweiblogplus.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.app.huaweiblogplus.ActivityMain;
import com.app.huaweiblogplus.ActivityPostDetails;
import com.app.huaweiblogplus.R;
import com.app.huaweiblogplus.adapter.AdapterPostListModern;
import com.app.huaweiblogplus.model.Post;
import com.app.huaweiblogplus.realm.RealmController;

public class FragmentLater extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterPostListModern mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_later, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        recyclerView = (RecyclerView) root_view.findViewById(R.id.recyclerViewLater);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterPostListModern(getActivity(), recyclerView, new ArrayList<Post>());
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterPostListModern.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Post obj, int position) {
                ActivityPostDetails.navigate((ActivityMain) getActivity(), v.findViewById(R.id.image), obj);
            }
        });
        return root_view;
    }

    @Override
    public void onResume() {
        showNoItemView(false);
        if(RealmController.with(this).getPostSize() > 0){
            displayData(RealmController.with(this).getPost());
        } else {
            showNoItemView(true);
        }
        super.onResume();
    }

    private void displayData(final List<Post> posts) {
        mAdapter.resetListData();
        mAdapter.insertData(posts);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = (View) root_view.findViewById(R.id.lyt_no_item_later);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }
}
