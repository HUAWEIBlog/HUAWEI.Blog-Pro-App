package app.huaweiblogplus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;


public class SharedPref {

    private static SharedPreferences custom_static_preference;
    private static Context ctx;
    private SharedPreferences custom_preference;
    private SharedPreferences default_preference;

    public static final int MAX_OPEN_COUNTER = 10 ;

    public SharedPref(Context context) {
        this.ctx = context;
        custom_preference = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
        default_preference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void storeHWRegIdInPref(String token) {
        //custom_static_preference.edit().putString("hwPushToken", token).apply();
        SharedPreferences pref = ctx.getSharedPreferences("my_pref", 0);
        pref.edit().putString("hwPushToken", token).apply();
    }

    public void storeGRegIdInPref(String token) {
        SharedPreferences pref = ctx.getSharedPreferences("my_pref", 0);
        pref.edit().putString("gPushToken", token).apply();
    }

    public String getSmartphone() {
        //return default_preference.getString("listDevices", "NULL");
        Set<String> prefs = default_preference.getStringSet("listDevices", new HashSet<String>());

        String smartphones = prefs.toString();
        return smartphones.substring(1, smartphones.length() - 1);

    }


    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        custom_preference.edit().putBoolean(key, value).apply();
    }


}

