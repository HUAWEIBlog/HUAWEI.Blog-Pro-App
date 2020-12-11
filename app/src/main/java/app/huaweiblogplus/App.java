package app.huaweiblogplus;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import app.huaweiblogplus.providers.wordpress.PostItem;
import app.huaweiblogplus.providers.wordpress.api.WordpressGetTaskInfo;
import app.huaweiblogplus.providers.wordpress.ui.WordpressDetailActivity;
import app.huaweiblogplus.util.Log;
import app.huaweiblogplus.R;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;


import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static app.huaweiblogplus.Config.NOTIFICATION_BASEURL;

/**
 * This file is part of the Universal template
 * For license information, please check the LICENSE
 * file in the root of this project
 *
 * @author Sherdle
 * Copyright 2019
 */
public class App extends MultiDexApplication {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        if (Config.FIREBASE_ANALYTICS) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("Firebase", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            FirebaseCrashlytics.getInstance().setUserId(token);

                            // Log and toast
                            Log.d("Firebase", "Token: " + token);
                        }
                    });
        }

        //OneSignal Push

        // ---->>> Check HMS & GMS <<<----
        // Getting status
        int statusGMS = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this)== ConnectionResult.SUCCESS){
            // The SafetyDetect SysIntegrity HMS API is available.
            String oneSignalAppID = getResources().getString(R.string.onesignal_app_id_hms);

            if (!TextUtils.isEmpty(oneSignalAppID)) {
                OneSignal.init(this, getString(R.string.onesignal_hms_project_number), oneSignalAppID, new NotificationHandler());
                OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
            }

        } else if(statusGMS== ConnectionResult.SUCCESS) {
            // The SafetyDetect SysIntegrity GMS API is available.
            String oneSignalAppID = getResources().getString(R.string.onesignal_app_id);

            if (!TextUtils.isEmpty(oneSignalAppID)) {
                OneSignal.init(this, getString(R.string.onesignal_google_project_number), oneSignalAppID, new NotificationHandler());
                OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
            }

        }else{
            // No HMS, no GMS.
        }

        // - Uncomment the line below to send a test notification
        //OSNotificationOpenResult res = new OSNotificationOpenResult();
        //res.notification = new OSNotification();
        //res.notification.payload = new OSNotificationPayload();
        //res.notification.payload.launchURL = "http://yoururl.com/some-post/";
        //new NotificationHandler().notificationOpened(res);

    }

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    private class NotificationHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            try {
                JSONObject data = result.notification.payload.additionalData;

                //String browserUrl = (data != null) ? data.optString("url", null) : null;
                String browserUrl = result.notification.payload.launchURL;

                Log.d("POPUP", "baseURL: " + NOTIFICATION_BASEURL);
                Log.d("POPUP", "browserUrl: " + browserUrl);



                if (browserUrl != null) {
                    if (NOTIFICATION_BASEURL.length() > 0 && browserUrl != null) {
                        openWordPressPost(browserUrl);
                    } else {
                        Intent mainIntent;
                        mainIntent = new Intent(App.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        //HolderActivity.startWebViewActivity(App.this, webViewUrl, webViewUrl == null, false, null, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NEW_TASK);
                    }
                } else if (!result.notification.isAppInFocus) {
                    Intent mainIntent;
                    mainIntent = new Intent(App.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainIntent);
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    private void openWordPressPost(String postUrl){

        String newBaseUrl = "https://apiv2.huaweiblog.de/wp-json/wp/v2/";
        //Toast to indicate that posts are loading, a loading screen would be nicer.
        Toast.makeText(getApplicationContext(), R.string.loading, Toast.LENGTH_SHORT).show();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                WordpressGetTaskInfo info = new WordpressGetTaskInfo(null, null, newBaseUrl, false);

                //By default, we'll check the most recent posts and see if the notified url is used by one of these posts
                String requestUrl = info.provider.getRecentPosts(info) + 1;
                Log.d("POPUP", "info: " + info.provider);

                //However, if the notification url potentially contains the slug (last path contains -), we query by slug
                String potentialPostSlug = postUrl.split("/")[postUrl.split("/").length - 1].substring(3);
                Log.d("POPUP", "potentialPostSlug: " + potentialPostSlug);
                //if (potentialPostSlug.contains("-")) {
                    //TODO Dirty, integrate this into the provider interface (get post by slug)
                        requestUrl = newBaseUrl + "posts/?_embed=1&include=" + potentialPostSlug ;
                        Log.d("POPUP", "requestUrl: " + requestUrl);
                //}

                ArrayList<PostItem> posts = info.provider.parsePostsFromUrl(info,
                        requestUrl);

                for (PostItem post : posts) {
                    Log.d("POPUP", "postRequestUrl: " + post.getUrl());
                    Log.d("POPUP", "postUrl: " + postUrl);
                    //if (post.getUrl().equals(postUrl)) {
                        Intent intent = new Intent(getApplicationContext(), WordpressDetailActivity.class);
                        intent.putExtra(WordpressDetailActivity.EXTRA_POSTITEM, post);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(WordpressDetailActivity.EXTRA_API_BASE, newBaseUrl);

                        startActivity(intent);
                        return;
                    //}
                }
            }
        };
        AsyncTask.execute(runnable);
    }
}