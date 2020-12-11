package app.huaweiblogplus;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Html;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.h6ah4i.android.compat.preference.MultiSelectListPreferenceCompat;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import app.huaweiblogplus.providers.web.NestedScrollWebView;
import app.huaweiblogplus.util.Log;
import app.huaweiblogplus.util.MultiSelectListPreference;
import app.huaweiblogplus.util.ThemeHelper;
import app.huaweiblogplus.R;

import java.util.Arrays;
import java.util.Set;

/**
 * This fragmnt is used to show a settings page to the user
 */

public class SettingsFragment extends androidx.core.preference.PreferenceFragment implements
		BillingProcessor.IBillingHandler {
	
	//You can change this setting if you would like to disable rate-my-app
	boolean HIDE_RATE_MY_APP = false;

	private BillingProcessor bp;
	private Preference preferencepurchase;
	private MultiSelectListPreference devices;
	
	private AlertDialog dialog;
	
    private static String PRODUCT_ID_BOUGHT = "item_1_bought";
	public static String SHOW_DIALOG = "show_dialog";

	private int count = 0;

	// fields
	private String mOrigSummaryText;
	private String listDevices2;
	private Object newValue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_settings);

		// open play store page
		Preference preferencerate = findPreference("rate");
		preferencerate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Uri uri = Uri.parse("market://details?id="
								+ getActivity().getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(getActivity(),
									"Could not open Play Store",
									Toast.LENGTH_SHORT).show();
							return true;
						}
						return true;
					}
				});

		// ex. MultiSelectListPreferenceCompat
		Preference preferenceDevices = (MultiSelectListPreferenceCompat) findPreference("listDevices");
		preferenceDevices.setOnPreferenceChangeListener(this::onPreferenceChange);

		Preference preferenceVersion = findPreference("version");
		try {
			PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			String version = pInfo.versionName;
			preferenceVersion.setSummary(version);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		preferenceVersion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				count++;
				if (count == 2) {
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

									Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
									sharingIntent.setType("text/plain");
									sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Token");
									sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, token);
									startActivity(Intent.createChooser(sharingIntent, "App Token"));

								}
							});
				}
				return false;
			}
		});

		// open about dialog
		Preference preferenceabout = findPreference("about");
		preferenceabout
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						ab.setMessage(Html.fromHtml(getResources().getString(
								R.string.about_text)));
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.about_header));
						ab.show();
						return true;
					}
				});

		// open support dialog

		Preference preferencesupport = findPreference("systeminfo");
		SharedPreferences pref = getContext().getSharedPreferences("my_pref", 0);
		String hwPushToken = pref.getString("hwPushToken", null);
		Log.i(NestedScrollWebView.TAG, "HW Token ID: " + hwPushToken);
		preferencesupport
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						//ab.setMessage(getResources().getString(R.string.token_text));
						//ab.setMessage();
						ab.setMessage(getResources().getString(R.string.token_text) + "\n\n" + hwPushToken);
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.information_header));
						ab.show();
						return true;
					}
				});


		// open term dialog
		Preference preferenceterm = findPreference("term");
		preferenceterm
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder ab = null;
						ab = new AlertDialog.Builder(getActivity());
						ab.setMessage(Html.fromHtml(getResources().getString(
								R.string.content_term)));
						ab.setPositiveButton(
								getResources().getString(R.string.ok), null);
						ab.setTitle(getResources().getString(
								R.string.pref_title_term));
						ab.show();
						return true;
					}
				});


		// open about dialog
		/*Preference preferencelicenses = findPreference("licenses");
		preferencelicenses
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						HolderActivity.startWebViewActivity(getActivity(), "file:///android_asset/open_source_licenses.html", false, true, null);
						return true;
					}
				});
		*/
		if (Config.HIDE_DRAWER || Config.DRAWER_OPEN_START) {
			PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general");
			Preference preferencedraweropen = findPreference("menuOpenOnStart");
			generalCategory.removePreference(preferencedraweropen);
		}

		// notifications

		Preference notificationsPreference = findPreference("notifications");
		// ---->>> Check HMS & GMS <<<----
		// Getting status
		int statusGMS = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(getActivity())== ConnectionResult.SUCCESS){
			// The SafetyDetect SysIntegrity HMS API is available.
			String oneSignalAppID = getResources().getString(R.string.onesignal_app_id_hms);

			if (null != oneSignalAppID && !oneSignalAppID.equals("")){
				notificationsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Context context = getActivity();
						Intent intent = new Intent();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
							intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
						} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
							intent.putExtra("app_package", context.getPackageName());
							intent.putExtra("app_uid", context.getApplicationInfo().uid);
						} else {
							intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.setData(Uri.parse("package:" + context.getPackageName()));
						}
						context.startActivity(intent);
						return true;
					}
				});
			} else {
				PreferenceCategory general = (PreferenceCategory) findPreference("general");
				general.removePreference(notificationsPreference);
			}
		} else if(statusGMS== ConnectionResult.SUCCESS) {
			// The SafetyDetect SysIntegrity GMS API is available.
			String oneSignalAppID = getResources().getString(R.string.onesignal_app_id);

			if (null != oneSignalAppID && !oneSignalAppID.equals("")){
				notificationsPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Context context = getActivity();
						Intent intent = new Intent();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
							intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
						} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
							intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
							intent.putExtra("app_package", context.getPackageName());
							intent.putExtra("app_uid", context.getApplicationInfo().uid);
						} else {
							intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.setData(Uri.parse("package:" + context.getPackageName()));
						}
						context.startActivity(intent);
						return true;
					}
				});
			} else {
				PreferenceCategory general = (PreferenceCategory) findPreference("general");
				general.removePreference(notificationsPreference);
			}
		}else{
			// No HMS, no GMS.
		}
		
		// purchase
		preferencepurchase = findPreference("purchase");
		String license = getResources().getString(R.string.google_play_license);
		if (null != license && !license.equals("")){
			bp = new BillingProcessor(getActivity(),
				license, this);
			bp.loadOwnedPurchasesFromGoogle();
		
			preferencepurchase
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						bp.purchase(getActivity(), PRODUCT_ID());
						return true;
					}
				});
		
			if (getIsPurchased(getActivity())){
				preferencepurchase.setIcon(R.drawable.ic_action_action_done);
			}
		} else {
			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");
			PreferenceCategory billing = (PreferenceCategory) findPreference("billing");
			preferenceScreen.removePreference(billing);
		}
		
		String[] extra = getArguments().getStringArray(MainActivity.FRAGMENT_DATA);
		if (null != extra && extra.length != 0 && extra[0].equals(SHOW_DIALOG)){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Add the buttons
			builder.setPositiveButton(R.string.settings_purchase, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   bp.purchase(getActivity(), PRODUCT_ID());
			           }
			       });
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User cancelled the dialog
			           }
			       });
			builder.setTitle(getResources().getString(R.string.dialog_purchase_title));
			builder.setMessage(getResources().getString(R.string.dialog_purchase));

			// Create the AlertDialog
			dialog = builder.create();
			dialog.show();
		}
		
		if (HIDE_RATE_MY_APP){
			PreferenceCategory other = (PreferenceCategory) findPreference("other");
			Preference preference = findPreference("rate");
			other.removePreference(preference);
		}

		ListPreference themePreference = (ListPreference) findPreference("themePref");
		if (themePreference != null) {
			themePreference.setOnPreferenceChangeListener(
					new Preference.OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							String themeOption = (String) newValue;
							ThemeHelper.applyTheme(themeOption);
							return true;
						}
					});
		}

	}

	@Override
	public void onBillingInitialized() {
		/*
		 * Called when BillingProcessor was initialized and it's ready to
		 * purchase
		 */
	}

	@Override
	public void onProductPurchased(String productId, TransactionDetails details) {
		if (productId.equals(PRODUCT_ID())){
			setIsPurchased(true, getActivity());
			preferencepurchase.setIcon(R.drawable.ic_action_action_done);
			Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_success), Toast.LENGTH_LONG).show();
		}
		Log.v("INFO", "Purchase purchased");
	}

	@Override
	public void onBillingError(int errorCode, Throwable error) {
		Toast.makeText(getActivity(), getResources().getString(R.string.settings_purchase_fail), Toast.LENGTH_LONG).show();
		Log.v("INFO", "Error");
	}

	@Override
	public void onPurchaseHistoryRestored() {
		if (bp.isPurchased(PRODUCT_ID())){
            	setIsPurchased(true, getActivity());
            	Log.v("INFO", "Purchase actually restored");
            	preferencepurchase.setIcon(R.drawable.ic_action_action_done);
            	if (dialog != null) dialog.cancel();
            	Toast.makeText(getActivity(), getResources().getString(R.string.settings_restore_purchase_success), Toast.LENGTH_LONG).show();
            }
		Log.v("INFO", "Purchase restored called");
	}
	
	public void setIsPurchased(boolean purchased, Context c){
    	SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(c);
    	
    	SharedPreferences.Editor editor= prefs.edit();
    	
    	editor.putBoolean(PRODUCT_ID_BOUGHT, purchased);
 	    editor.apply();
	}
	
	public static boolean getIsPurchased(Context c){
		SharedPreferences prefs = PreferenceManager
        	    .getDefaultSharedPreferences(c);
        
        boolean prefson = prefs.getBoolean(PRODUCT_ID_BOUGHT, false);
        
        return prefson;
	}
	
	private String PRODUCT_ID(){
		return getResources().getString(R.string.product_id);
	}
	

	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        bp.handleActivityResult(requestCode, resultCode, intent);
    }

	@SuppressWarnings("unchecked")
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();
		preference.setSummary(stringValue);

		// TODO: Multiselect results in NullPointerException
		final String key = preference.getKey();
		if (key.equals("listDevices2")) {
			final MultiSelectListPreferenceCompat multiselpref = (MultiSelectListPreferenceCompat) preference;

			Log.e("MSL", "multiselect:" + multiselpref); // Lieblingsgerät [123, 1234]

			multiselpref.setSummary(makeSummaryText(mOrigSummaryText, (Set<String>) value));

			return true;
		} else {
			return true;
		}

		//return true;
	}

	public static String makeSummaryText(String baseText, Set<String> values) {
		Log.e("MSL", "Values:" + values); // [123, 1234]
		return baseText + " " + sortedToString(values);
	}

	public static String sortedToString(Set<String> values) {
		// sort items
		Log.e("MSL", "Values-Sort:" + values); // [123, 1234]
		String[] sorted = new String[values.size()];
		values.toArray(sorted);
		Arrays.sort(sorted);

		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < sorted.length; i++) {
			if (i > 0)
				builder.append(",");

			builder.append(sorted[i]);
		}
		builder.append("]");

		return builder.toString();
	}

	private void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener((Preference.OnPreferenceChangeListener) getActivity());
		SharedPreferences preferences =
				PreferenceManager.getDefaultSharedPreferences(preference.getContext());
		String preferenceString = preferences.getString(preference.getKey(), "");
		onPreferenceChange(preference, preferenceString);
	}
	
	
	@Override
	public void onDestroy() {
	   if (bp != null) 
	        bp.release();

	    super.onDestroy();
	}
}
