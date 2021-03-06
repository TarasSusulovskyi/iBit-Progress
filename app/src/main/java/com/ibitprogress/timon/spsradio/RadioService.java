package com.ibitprogress.timon.spsradio;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;


public class RadioService extends Service {


    //ExoPlayer
    PlayerView playerView;
    ExoPlayer player;
    private long playbackPosition = 0;
    private int currentWindow = 0;
    private boolean playWhenReady = false;

    private static final String CHANNEL_WHATEVER="channel_whatever";
    private static int NOTIFY_ID=1337;
    private static int FOREGROUND_ID=1338;

    PendingIntent pIntentPlay;
    PendingIntent pIntentPause;


    @Override
    public void onCreate() {

        Intent intentPause = new Intent(this, MainActivity.class);
        intentPause.putExtra("PauseClick",true);
        pIntentPause = PendingIntent.getActivity(this, 0, intentPause, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentPlay = new Intent(this, MainActivity.class);
        intentPlay.putExtra("PlayClick",true);
        pIntentPlay = PendingIntent.getActivity(this, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        //region Start Player

        playerView = MainActivity.view.findViewById(R.id.video_view);
        initializePlayer();

        //endregion

        startForeground(FOREGROUND_ID,
                buildForegroundNotification());

        player.setPlayWhenReady(true);
        player.getPlaybackState();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private android.app.Notification buildForegroundNotification() {
        NotificationCompat.Builder b =
                new NotificationCompat.Builder(this, CHANNEL_WHATEVER);



        b.setOngoing(true)
                .setContentTitle(getApplication().getPackageName())
                .setContentText("")
                .setAutoCancel(true)
                .addAction(R.drawable.ic_baseline_play_arrow_24px, "Play", pIntentPlay)
                .addAction(R.drawable.ic_baseline_pause_24px, "Pause", pIntentPause)
                .setSmallIcon(R.drawable.ic_baseline_notifications_24px);

        return (b.build());
    }

    //region ExoPlayer
    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());
        Uri uri = Uri.parse(getString(R.string.media_url_mp3));
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);

    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getContentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    public void pausePlayer() {

        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    public void startPlayer() {
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @SuppressLint("InlineApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).createMediaSource(uri);
    }

    //endregion
}
