package com.app.huaweiblog.connection;


import com.app.huaweiblog.connection.callbacks.CallbackCategories;
import com.app.huaweiblog.connection.callbacks.CallbackCategoryDetails;
import com.app.huaweiblog.connection.callbacks.CallbackComment;
import com.app.huaweiblog.connection.callbacks.CallbackDetailsPage;
import com.app.huaweiblog.connection.callbacks.CallbackDetailsPost;
import com.app.huaweiblog.connection.callbacks.CallbackDevice;
import com.app.huaweiblog.connection.callbacks.CallbackInfo;
import com.app.huaweiblog.connection.callbacks.CallbackListPage;
import com.app.huaweiblog.connection.callbacks.CallbackListPost;
import com.app.huaweiblog.model.DeviceInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {

    /* your wordPress url */
    String BASE_URL = "https://www.huaweiblog.de/api/";


    // minimize field for list of post
    String EXCLUDE_FIELD = "&exclude=content,categories,tags,comments,custom_fields";
    String EXCLUDE_FIELD_PAGE = "&exclude=content,categories,tags,comments,custom_fields,attachments,author";
    String USER_AGENT = "HuaweiBlogApp";

    /* info API transaction ------------------------------- */

    @GET("?json=info")
    Call<CallbackInfo> getInfo();


    /* Post API transaction ------------------------------- */

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_posts" + EXCLUDE_FIELD)
    Call<CallbackListPost> getPostByPage(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_post")
    Call<CallbackDetailsPost> getPostDetailsById(
            @Query("id") long id
    );

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_search_results" + EXCLUDE_FIELD)
    Call<CallbackListPost> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count
    );


    /* Category API transaction --------------------------- */

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_category_index")
    Call<CallbackCategories> getAllCategories();

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_category_posts" + EXCLUDE_FIELD)
    Call<CallbackCategoryDetails> getCategoryDetailsByPage(
            @Query("id") long id,
            @Query("page") long page,
            @Query("count") long count
    );

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=respond/submit_comment")
    Call<CallbackComment> sendComment(
            @Query("post_id") long post_id,
            @Query("name") String name,
            @Query("email") String email,
            @Query("content") String content
    );

    /* Page API transaction --------------------------- */

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_page_index" + EXCLUDE_FIELD_PAGE)
    Call<CallbackListPage> getPagesByPage();

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @GET("?json=get_page")
    Call<CallbackDetailsPage> getPageDetailsById(
            @Query("id") long id
    );

    /* FCM notification API transaction --------------------------- */

    @Headers({"Cache-Control: max-age=0", "User-Agent: "+USER_AGENT})
    @POST("?api-fcm=register")
    Call<CallbackDevice> registerDevice(@Body DeviceInfo deviceInfo);


}
