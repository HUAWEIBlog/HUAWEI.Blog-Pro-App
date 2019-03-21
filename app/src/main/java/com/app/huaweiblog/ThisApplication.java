package com.app.huaweiblog.plus;

import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.app.huaweiblog.plus.connection.API;
import com.app.huaweiblog.plus.connection.RestAdapter;
import com.app.huaweiblog.plus.connection.callbacks.CallbackDevice;
import com.app.huaweiblog.plus.data.AppConfig;
import com.app.huaweiblog.plus.data.SharedPref;
import com.app.huaweiblog.plus.model.DeviceInfo;
import com.app.huaweiblog.plus.utils.NetworkCheck;
import com.app.huaweiblog.plus.utils.Tools;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThisApplication extends Application {

    private Call<CallbackDevice> callbackCall = null;
    private static ThisApplication mInstance;
    private SharedPref sharedPref;
    private Tracker tracker;

    private int fcm_count = 0;
    private final int FCM_MAX_COUNT = 10;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            obtainFirebaseToken();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        sharedPref = new SharedPref(this);

        // initialize firebase
        FirebaseApp.initializeApp(this);

        // obtain regId & registering device to server
        obtainFirebaseToken();

        // init realm database
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("koran.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // activate analytics tracker
        getGoogleAnalyticsTracker();

        // get enabled controllers
        Tools.requestInfoApi(this);

    }

    public static synchronized ThisApplication getInstance() {
        return mInstance;
    }

    private void obtainFirebaseToken() {

        if (NetworkCheck.isConnect(this) && sharedPref.isOpenAppCounterReach()) {
            fcm_count++;

            Task<InstanceIdResult> resultTask = FirebaseInstanceId.getInstance().getInstanceId();
            resultTask.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    String regId = instanceIdResult.getToken();
                    sharedPref.setFcmRegId(regId);
                    if (!TextUtils.isEmpty(regId)) sendRegistrationToServer(regId);
                }
            });

            resultTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (fcm_count > FCM_MAX_COUNT) return;
                    obtainFirebaseToken();
                }
            });
        }
    }

    private void sendRegistrationToServer(String token) {

        API api = RestAdapter.createAPI();
        DeviceInfo deviceInfo = Tools.getDeviceInfo(this);
        deviceInfo.regid = token;

        callbackCall = api.registerDevice(deviceInfo);
        callbackCall.enqueue(new Callback<CallbackDevice>() {
            @Override
            public void onResponse(Call<CallbackDevice> call, Response<CallbackDevice> response) {
                CallbackDevice resp = response.body();
                if (resp != null && resp.status.equals("success")) {
                    sharedPref.setOpenAppCounter(0);
                }
            }

            @Override
            public void onFailure(Call<CallbackDevice> call, Throwable t) {
            }

        });
    }

    /**
     * --------------------------------------------------------------------------------------------
     * For Google Analytics
     */
    public synchronized Tracker getGoogleAnalyticsTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(!AppConfig.ENABLE_ANALYTICS);
            tracker = analytics.newTracker(R.xml.app_tracker);
        }
        return tracker;
    }

    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();
        // Set screen name.
        t.setScreenName(screenName);
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();
            t.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e))
                    .setFatal(false)
                    .build()
            );
        }
    }

    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

    /** ---------------------------------------- End of analytics --------------------------------- */
}
