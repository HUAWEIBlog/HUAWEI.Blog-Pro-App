package com.app.huaweiblog.plus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.huaweiblog.plus.widget.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ActivityFullScreenImage extends AppCompatActivity {

    public static final String EXTRA_URL = "key.EXTRA_URL";

    public static void navigate(AppCompatActivity activity, String url) {
        Intent intent = new Intent(activity, ActivityFullScreenImage.class);
        intent.putExtra(EXTRA_URL, url);
        activity.startActivity(intent);
    }

    private TouchImageView image;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(this, R.string.invalid_image_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        image = (TouchImageView) findViewById(R.id.image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Picasso.with(this).load(url).fit().centerInside().into(image, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                finish();
                Toast.makeText(getApplicationContext(), R.string.failed_when_open_image, Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageButton) findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

}
