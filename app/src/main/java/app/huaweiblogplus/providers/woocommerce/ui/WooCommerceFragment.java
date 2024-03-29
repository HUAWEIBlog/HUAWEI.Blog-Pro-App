package app.huaweiblogplus.providers.woocommerce.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import app.huaweiblogplus.Config;
import app.huaweiblogplus.HolderActivity;
import app.huaweiblogplus.MainActivity;
import app.huaweiblogplus.providers.woocommerce.adapter.ProductsAdapter;
import app.huaweiblogplus.providers.woocommerce.model.RestAPI;
import app.huaweiblogplus.providers.woocommerce.model.products.Category;
import app.huaweiblogplus.providers.woocommerce.model.products.Image;
import app.huaweiblogplus.providers.woocommerce.model.products.Product;
import app.huaweiblogplus.R;
import app.huaweiblogplus.providers.Provider;
import app.huaweiblogplus.providers.woocommerce.WooCommerceProductFilter;
import app.huaweiblogplus.providers.woocommerce.WooCommerceTask;
import app.huaweiblogplus.util.Helper;
import app.huaweiblogplus.util.InfiniteRecyclerViewAdapter;
import app.huaweiblogplus.util.Log;
import app.huaweiblogplus.util.ThemeUtils;
import app.huaweiblogplus.util.ViewModeUtils;
import app.huaweiblogplus.util.layout.StaggeredGridSpacingItemDecoration;
import com.squareup.picasso.Picasso;

import org.jsoup.helper.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2019
 */
public class WooCommerceFragment extends Fragment implements WooCommerceTask.Callback<Product>, InfiniteRecyclerViewAdapter.LoadMoreListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProductsAdapter productsAdapter;
    private ViewModeUtils viewModeUtils;
    private List<Product> productList;
    private Activity mAct;
    private SearchView searchView;
    private MenuItem searchMenu;

    private int page = 1;
    private boolean isHomePage = false;

    private int category;
    private static final String HOME = "home";
    private static final String FEATURED = "featured";
    private static final String SALE = "sale";

    private WooCommerceProductFilter filter;
    private List<String> headerImages;
    private String searchQuery;

    public WooCommerceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_refresh, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.list);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        productList = new ArrayList<>();
        productsAdapter = new ProductsAdapter(getContext(), productList, this);
        productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        recyclerView.setAdapter(productsAdapter);

        mAct = getActivity();
        filter = new WooCommerceProductFilter();

        String[] args = this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
        if (args.length > 0 && args[0].matches("^-?\\d+$")) {
            category = Integer.parseInt(args[0]);
        } else if (args.length > 0 && args[0].equals(HOME)){
            isHomePage = true;
        } else if (args.length > 0 && args[0].equals(FEATURED)){
            filter.onlyFeatured(true);
        } else if (args.length > 0 && args[0].equals(SALE)){
            filter.onlySale(true);
        }
        headerImages = new ArrayList<>();
        if (args.length > 1 && args[1].startsWith("http")) {
            headerImages.add(args[1]);
            if (args.length > 2 && args[2].startsWith("http")) {
                headerImages.add(args[2]);
            }
        }

        setViewMode();
        recyclerView.addItemDecoration(new StaggeredGridSpacingItemDecoration((int) getResources().getDimension(R.dimen.woocommerce_padding), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(getResources().getColor(R.color.white));

        if (getString(R.string.woocommerce_url).isEmpty() || !getString(R.string.woocommerce_url).startsWith("http")) {
            Toast.makeText(mAct, "You need to enter a valid WooCommerce url and API tokens as documented!", Toast.LENGTH_SHORT).show();
            return;
        }

        refreshItems();
        updateHeaders();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    @Override
    public void success(ArrayList<Product> result) {
        if (result.size() > 0) {
            productList.addAll(result);
        } else {
            productsAdapter.setHasMore(false);
        }
        productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void failed() {
        productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void refreshItems() {
        // Load items
        page = 1;
        productList.clear();
        productsAdapter.setHasMore(true);
        productsAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        requestItems();
    }

    @Override
    public void onMoreRequested() {
        page = page + 1;
        requestItems();
    }

    private void requestItems() {
        WooCommerceTask.WooCommerceBuilder builder = new WooCommerceTask.WooCommerceBuilder(mAct);
        if (searchQuery != null)
            builder.getProductsForQuery(this, searchQuery, page, filter).execute();
        else if (category != 0)
            builder.getProductsForCategory(this, category, page, filter).execute();
        else
            builder.getProducts(this, page, filter).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.woocommerce_menu, menu);

        // set & get the search button in the actionbar
        searchView = new SearchView(getActivity());
        searchView.setQueryHint(getResources().getString(
                R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    query = URLEncoder.encode(query, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.printStackTrace(e);
                }
                searchView.clearFocus();

                searchQuery = query;
                refreshItems();
                updateHeaders();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        searchView.addOnAttachStateChangeListener(
                new View.OnAttachStateChangeListener() {

                    @Override
                    public void onViewDetachedFromWindow(View arg0) {
                        searchQuery = null;
                        updateHeaders();
                        refreshItems();
                    }

                    @Override
                    public void onViewAttachedToWindow(View arg0) {
                        // search was opened
                    }
                });

        searchMenu = menu.findItem(R.id.menu_search);
        searchMenu.setActionView(searchView);

        if (searchQuery == null & isHomePage)
            searchMenu.setVisible(false);

        ThemeUtils.tintAllIcons(menu, mAct);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_cart:
                HolderActivity.startActivity(getActivity(), CartFragment.class);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFilterDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        LayoutInflater inflater = mAct.getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_wc_filter_dialog, null);

        final EditText minPriceField = view.findViewById(R.id.min_price);
        final EditText maxPriceField = view.findViewById(R.id.max_price);
        final CheckBox saleCheckbox = view.findViewById(R.id.checkbox_sale);
        final CheckBox featuredCheckbox = view.findViewById(R.id.checkbox_featured);
        final Spinner orderSpinner = view.findViewById(R.id.order_spinner);

        ArrayMap<String, String> orderByOptions = new ArrayMap<>();
        orderByOptions.put("date", getString(R.string.order_date));
        orderByOptions.put("price", getString(R.string.order_price));
        orderByOptions.put("popularity", getString(R.string.order_popularity));
        orderByOptions.put("rating", getString(R.string.order_rating));
        String[] values = orderByOptions.values().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, values);
        orderSpinner.setAdapter(adapter);

        TextView currency_max = view.findViewById(R.id.currency_max_price);
        TextView currency_min = view.findViewById(R.id.currency_min_price);
        currency_max.setText(String.format(RestAPI.getCurrencyFormat(), ""));
        currency_min.setText(String.format(RestAPI.getCurrencyFormat(), ""));

        if (filter.getMinPrice() != 0)
            minPriceField.setText(Double.toString(filter.getMinPrice()));
        if (filter.getMaxPrice() != 0)
            maxPriceField.setText(Double.toString(filter.getMaxPrice()));
        if (filter.isOnlySale())
            saleCheckbox.setChecked(true);
        if (filter.isOnlyFeatured())
            featuredCheckbox.setChecked(true);
        if (filter.getOrderBy() != null)
            orderSpinner.setSelection(orderByOptions.indexOfKey(filter.getOrderBy()));
        else
            //set date as default, since this is also the API default
            orderSpinner.setSelection(orderByOptions.indexOfKey("date"));

        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.filter));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double minPrice = minPriceField.getText().toString().isEmpty() ? 0 :
                        Double.parseDouble(minPriceField.getText().toString());
                double maxPrice = maxPriceField.getText().toString().isEmpty() ? 0 :
                        Double.parseDouble(maxPriceField.getText().toString());
                boolean onlySale = saleCheckbox.isChecked();
                boolean onlyFeatured = featuredCheckbox.isChecked();
                String orderBy = orderByOptions.keyAt(orderSpinner.getSelectedItemPosition());

                String order;
                if (orderBy.equals("price")) {
                    order = "asc";
                } else {
                    order = "desc";
                }

                filter.maxPrice(maxPrice)
                        .minPrice(minPrice)
                        .onlyFeatured(onlyFeatured)
                        .onlySale(onlySale)
                        .orderBy(orderBy)
                        .order(order);

                refreshItems();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int randomGradientResource(int index) {
        index += 1;
        if (index == 6) index = 1;

        return Helper.getGradient(index);
    }

    /**
     * Headers for Woocommerce list
     */

    public void updateHeaders(){
        productsAdapter.clearHeaders();

        if (searchQuery != null){
            loadFilterHeader(0);
            return;
        }

        if (isHomePage) {
            int index = 0;
            loadSearchHeader(index); index++;
            if (headerImages.size() > 0) {
                loadHeaderImage(index, headerImages.get(0), RestAPI.home_banner_one); index++;
            }
            loadCategorySlider(index);
            if (headerImages.size() > 1) {
                loadTextHeader(index, getString(R.string.sale)); index++;
                loadHeaderImage(index, headerImages.get(1), RestAPI.home_banner_two); index++;
            }
            loadTextHeader(index, getString(R.string.latest_products));
        } else {
            if (headerImages.size() > 0)
                loadHeaderImage(0, headerImages.get(0), null);
            loadFilterHeader(headerImages.size() > 0 ? 1 :  0);
        }

    }

    /**
     * When the categories have been loaded, they will be inserted at the index
     * @param index Index to insert into
     */
    private void loadCategorySlider(final int index) {
        if (!Config.WC_CHIPS) return;

        WooCommerceTask.Callback<Category> callback = new WooCommerceTask.Callback<Category>() {
            @Override
            public void success(ArrayList<Category> categories) {
                LayoutInflater layoutInflater = LayoutInflater.from(mAct);

                final ViewGroup sliderView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_header_slider, null);
                for (final Category item : categories) {
                    ViewGroup itemView;
                    if (item.getImage() != null && item.getImage() instanceof JsonObject) {
                        Image image = new Gson().fromJson(item.getImage(), Image.class);
                        itemView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_category_card_image, null);
                        Picasso.get().load(image.getSrc()).into((ImageView) itemView.findViewById(R.id.image));
                    } else {
                        itemView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_category_card_text, null);
                        itemView.findViewById(R.id.background).setBackgroundResource(randomGradientResource(categories.indexOf(item)));
                    }

                    TextView title = itemView.findViewById(R.id.title);
                    title.setText(item.getName());
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            HolderActivity.startActivity(mAct, WooCommerceFragment.class, Provider.WOOCOMMERCE, new String[]{(Integer.toString(item.getId()))});
                        }
                    });
                    ((LinearLayout) sliderView.findViewById(R.id.slider_content)).addView(itemView);
                }

                productsAdapter.addHeaderToIndex(sliderView, index);

                //Animate the appearance
                sliderView.setAlpha(0);
                sliderView.animate().alpha(1).setDuration(500).start();
            }

            @Override
            public void failed() {

            }
        };

        new WooCommerceTask.WooCommerceBuilder(mAct).getCategories(callback).execute();
    }

    private void loadSearchHeader(int index) {
        LayoutInflater layoutInflater = LayoutInflater.from(mAct);
        final ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_header_search, null);
        final EditText searchBar = headerView.findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchMenu.setVisible(true);
                    searchView.onActionViewExpanded();
                    searchMenu.expandActionView();
                    searchView.setQuery(searchBar.getText(), true);
                    return true;
                }
                return false;
            }
        });
        productsAdapter.addHeaderToIndex(headerView, index);
    }

    private void loadHeaderImage(int index, String imageUrl, final String actionArgument) {
        if (imageUrl != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mAct);
            final ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_header_image, null);
            Picasso.get().load(imageUrl).into((ImageView) headerView.findViewById(R.id.header_image));
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!StringUtil.isBlank(actionArgument))
                        HolderActivity.startActivity(mAct, WooCommerceFragment.class, Provider.WOOCOMMERCE, new String[]{(actionArgument)});
                }
            });
            productsAdapter.addHeaderToIndex(headerView, index);
        }
    }

    private void loadTextHeader(int index, String string){
        LayoutInflater layoutInflater = LayoutInflater.from(mAct);
        final ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_header_text, null);
        ((TextView) headerView.findViewById(R.id.text)).setText(string);
        productsAdapter.addHeaderToIndex(headerView, index);
    }

    private void loadFilterHeader(int index) {
            LayoutInflater layoutInflater = LayoutInflater.from(mAct);

            ViewGroup headerView = (ViewGroup) layoutInflater.inflate(R.layout.fragment_wc_filter_header, null);
            final ImageButton normal = headerView.findViewById(R.id.normal);
            final ImageButton compact = headerView.findViewById(R.id.compact);
            updateViewModeButtons(normal, compact);

            Button filter = headerView.findViewById(R.id.filter);
            filter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFilterDialog();
                }
            });

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewModeUtils.saveToPreferences(view.equals(compact) ? ViewModeUtils.COMPACT : ViewModeUtils.NORMAL);
                    setViewMode();
                    updateViewModeButtons(normal, compact);
                }
            };
            normal.setOnClickListener(listener);
            compact.setOnClickListener(listener);

            productsAdapter.addHeaderToIndex(headerView, index);
    }

    private void setViewMode(){
        if (viewModeUtils == null)
            viewModeUtils = new ViewModeUtils(getContext(), getClass());

        int spanCount = viewModeUtils.getViewMode() == ViewModeUtils.COMPACT ||
                (isHomePage && searchQuery == null) ? 2 : 1;
        RecyclerView.LayoutManager mLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void updateViewModeButtons(View normal, View compact){
        if (viewModeUtils.getViewMode() == ViewModeUtils.NORMAL){
            normal.setAlpha(1.0f);
            compact.setAlpha(0.5f);
        } else {
            normal.setAlpha(0.5f);
            compact.setAlpha(1.0f);
        }
    }
}