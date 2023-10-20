package com.example.droneuisample;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

public class VideoActivity extends AppCompatActivity {
    private static final String url = "rtsp://admin:Sde_12345@192.168.100.64";

    private LibVLC libVlc;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;
    ImageButton btnConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        libVlc = new LibVLC(this);
        mediaPlayer = new MediaPlayer(libVlc);
        videoLayout = findViewById(R.id.videoLayout);

    }
    @Override
    protected void onStart()
    {
        super.onStart();

        mediaPlayer.attachViews(videoLayout, null, false, false);

        Media media = new Media(libVlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=600");

        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();
    }
}