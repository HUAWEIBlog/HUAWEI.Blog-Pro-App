package app.huaweiblogplus.providers.photos.api;

import android.content.Context;
import android.os.AsyncTask;

import app.huaweiblogplus.providers.photos.PhotoItem;
import app.huaweiblogplus.providers.wordpress.PostItem;
import app.huaweiblogplus.providers.wordpress.api.WordpressGetTaskInfo;

import org.jsoup.helper.StringUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WordpressClient implements PhotoProvider {

    private String[] params;
    private PhotosCallback callback;
    private WeakReference<Context> contextReference;

    private int totalPages;
    private int currentPage;

    public WordpressClient(String[] params, Context context, PhotosCallback callback){
        this.params = params;
        this.callback = callback;
        this.contextReference = new WeakReference<>(context);
    }

    @Override
    public void requestPhotos(int page) {
        this.currentPage = page;
        new WordpressTask().execute();
    }


    private class WordpressTask extends AsyncTask<Void, Void, ArrayList<PhotoItem>> {

        @Override
        protected ArrayList<PhotoItem> doInBackground(Void... voids) {
            String apiUrl = params[0];
            String category = params.length > 1 ? params[1] : "";
            WordpressGetTaskInfo info = new WordpressGetTaskInfo(null, null, apiUrl, false);
            ArrayList<PostItem> posts = info.provider.parsePostsFromUrl(info, (StringUtil.isBlank(category)?
                    info.provider.getRecentPosts(info) :
                    info.provider.getCategoryPosts(info, category)) + currentPage);

            if (info.pages == null || posts == null) return null;
            totalPages = info.pages;

            final ArrayList<PhotoItem> results = new ArrayList<>();
            for (final PostItem post : posts) {
                if (post.getFeaturedImageUrl() != null && !post.getFeaturedImageUrl().isEmpty()) {
                    PhotoItem photo = new PhotoItem(post.getId().toString(), post.getUrl(), post.getFeaturedImageUrl(), post.getTitle());
                    results.add(photo);
                }
            }

            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<PhotoItem> results) {
            if (results != null) {
                boolean canLoadMore = currentPage < totalPages;
                callback.completed(results, canLoadMore);
            } else {
                callback.failed();
            }

        }
    }
}
