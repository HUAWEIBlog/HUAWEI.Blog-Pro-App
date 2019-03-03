package com.app.huaweiblog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class ActivityYouTubePlayer extends AppCompatActivity implements YouTubePlayer.OnInitializedListener{

    private YouTubePlayer mPlayer;
    private String iD;
    private String videoTitle;
    private String videoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_youtube);

        YouTubePlayerFragment playerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById( R.id.youtube_player_fragment );

        String youTubeKey = "AIzaSyBrofPJO4yiy6s-yk5wKvzzkLerT4cI1To";
        playerFragment.initialize( youTubeKey, this);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                iD= null;
            } else {
                iD= extras.getString("videoId");
                videoTitle= extras.getString("videoTitle");
                videoText= extras.getString("videoText");
            }
        } else {
            iD= (String) savedInstanceState.getSerializable("videoId");
            videoTitle= (String) savedInstanceState.getSerializable("videoTitle");
            videoText= (String) savedInstanceState.getSerializable("videoText");
        }

        ((TextView) findViewById(R.id.id_video_play_text)).setText(videoTitle);
        //((TextView) findViewById(R.id.id_video_play_desc)).setText(videoText);


        /*RelativeLayout textViewDesc =
                (RelativeLayout) findViewById(R.id.id_video_play_desc);
        TextView videoTextView = new TextView(this);
        textViewDesc.setText(videoText);
        textViewDesc.addView(videoTextView);*/




        //Log.d("videoId A", "ID:" + iD);

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        mPlayer = player;

        //Enables automatic control of orientation
        mPlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);

        //Show full screen in landscape mode always
        mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);

        //System controls will appear automatically
        mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);

        //Log.d("videoId B", "ID:" + iD);

        if (!wasRestored) {
            //player.cueVideo("9rLZYyMbJic");
            mPlayer.loadVideo(iD);
        }
        else
        {
            mPlayer.play();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        mPlayer = null;

        Log.d("Status", "This Application is crashed");
    }

}
