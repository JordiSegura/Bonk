package com.ncs.BonkJordi;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        GameView gameView = new GameView(this);
        setContentView(gameView);

        gameEngine = new GameEngine(this, gameView);
        gameEngine.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        gameEngine.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        gameEngine.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gameEngine.stop();
    }
}