package app.huaweiblogplus.providers.wordpress.api.providers;

import app.huaweiblogplus.providers.wordpress.CategoryItem;
import app.huaweiblogplus.providers.wordpress.PostItem;
import app.huaweiblogplus.providers.wordpress.api.WordpressGetTaskInfo;

import java.util.ArrayList;
import java.util.Set;

/**
 * This is an interface for Wordpress API Providers.
 */
public interface WordpressProvider {

    String getRecentPosts(WordpressGetTaskInfo info);

    String getTagPosts(WordpressGetTaskInfo info, String tag);

    String getTagPostsArray(WordpressGetTaskInfo info, Set<String> tag);

    String getCategoryPosts(WordpressGetTaskInfo info, String category);

    String getPage(WordpressGetTaskInfo info, String pageId);

    String getSearchPosts(WordpressGetTaskInfo info, String query);

    ArrayList<CategoryItem> getCategories(WordpressGetTaskInfo info);

    ArrayList<PostItem> parsePostsFromUrl(WordpressGetTaskInfo info, String url);

}
