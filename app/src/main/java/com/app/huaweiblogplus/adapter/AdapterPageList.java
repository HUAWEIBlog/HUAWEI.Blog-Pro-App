package com.app.huaweiblogplus.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.huaweiblogplus.R;
import com.app.huaweiblogplus.model.Page;
import com.app.huaweiblogplus.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterPageList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Page> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Page obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterPageList(Context context, List<Page> items) {
        this.items = items;
        ctx = context;
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView short_content;
        public TextView date;
        public TextView comment;
        public ImageView image;
        public LinearLayout lyt_parent;

        public MainViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            short_content = (TextView) v.findViewById(R.id.short_content);
            date = (TextView) v.findViewById(R.id.date);
            comment = (TextView) v.findViewById(R.id.comment);
            image = (ImageView) v.findViewById(R.id.image);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_page, parent, false);
        vh = new MainViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MainViewHolder) {
            final Page p = items.get(position);
            MainViewHolder vItem = (MainViewHolder) holder;
            vItem.title.setText(Html.fromHtml(p.title));
            vItem.short_content.setText(Html.fromHtml(p.excerpt));
            vItem.date.setText(Tools.getFormatedDateSimple(p.date));
            Tools.displayImageThumbnailPage(ctx, p, vItem.image);

            vItem.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public void insertData(List<Page> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

}