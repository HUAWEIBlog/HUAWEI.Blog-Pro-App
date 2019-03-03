package com.app.huaweiblog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;


/*public class ActivityDeepLink extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_deeplink);

        TextView deepLinkUrl = (TextView) findViewById(R.id.deep_link);

        Intent intent = getIntent();
        Uri data = intent.getData();


        deepLinkUrl.setText("Deep link received - " + data);

    }
}*/

public class ActivityDeepLink extends Activity {
    public static final String CATEGORY_DEEP_LINK = "/category";
    public static final String NEWS_DEEP_LINK = "/news";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
        }

        openDeepLink(intent.getData());

        // Finish this activity
        finish();
    }

    private void openDeepLink(Uri deepLink) {
        String path = deepLink.getPath();

        if (CATEGORY_DEEP_LINK.equals(path)) {
            // Launch categories
            startActivity(new Intent(this, ActivityCategoryDetails.class));
        } else if (NEWS_DEEP_LINK.equals(path)) {
            // Launch news activity
            startActivity(new Intent(this, ActivityPostDetails.class));
        } else {
            // Fall back to the main activity
            startActivity(new Intent(this, ActivityMain.class));
        }
    }
}
