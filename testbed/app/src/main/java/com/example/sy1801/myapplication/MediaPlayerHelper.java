package com.example.sy1801.myapplication;

import android.Manifest;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;

public class MediaPlayerHelper {
    public String videoPath = Environment.getExternalStorageDirectory().getPath()+"/cc_long_tate_1021.mp4";

    private Surface mSurface;
    private MediaPlayer mediaPlayer;

    public MediaPlayerHelper(Context context, SurfaceTexture surfaceTexture){


//        Display display = ((WindowManager)context
//                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        DisplayMetrics metrics = new DisplayMetrics();
//        display.getMetrics(metrics);
//        int screenWidth = metrics.widthPixels;
//        int screenHidth = metrics.heightPixels;
//
//        //设置图像像素比位4:3
//        surfaceTexture.
//                setDefaultBufferSize(4*screenWidth/3,3 * screenWidth / 4);
        mSurface = new Surface(surfaceTexture);
    }

    public void initMediaPlayer() {
        if(null != mediaPlayer)
            return;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(mSurface);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        if (mp != null) {
                            mp.start(); //视频开始播放了
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            });
            mediaPlayer.setLooping(true);
        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void destory()
    {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer =null;
        }
    }

}
