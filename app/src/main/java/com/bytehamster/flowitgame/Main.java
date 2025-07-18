package com.bytehamster.flowitgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import com.bytehamster.flowitgame.model.LevelPack;
import com.bytehamster.flowitgame.state.ExitState;
import com.bytehamster.flowitgame.state.GameState;
import com.bytehamster.flowitgame.state.LevelPackSelectState;
import com.bytehamster.flowitgame.state.LevelSelectState;
import com.bytehamster.flowitgame.state.MainMenuState;
import com.bytehamster.flowitgame.state.SettingsState;
import com.bytehamster.flowitgame.state.State;
import com.bytehamster.flowitgame.state.TutorialState;

public class Main extends Activity {
    private MyGLSurfaceView glSurfaceView;
    private SoundPool soundPool;
    private State currentState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        glSurfaceView = findViewById(R.id.gl_surface_view);

        getSharedPreferences("preferences", Context.MODE_PRIVATE).edit()
                .putInt("lastAppVersion", BuildConfig.VERSION_CODE).apply();

        LevelPack.parsePacks(this);
        createViews();

        if (Build.VERSION.SDK_INT >= 21) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name), bm, 0xff206dbc);
            setTaskDescription(taskDesc);
        }
    }

    private void createViews() {
        glSurfaceView.getRenderer().setOnViewportSetupComplete(() -> {
            soundPool = new SoundPool(Main.this);
            soundPool.loadSound(R.raw.click);
            soundPool.loadSound(R.raw.fill);
            soundPool.loadSound(R.raw.won);

            State[] states = new State[]{
                    MainMenuState.getInstance(),
                    ExitState.getInstance(),
                    SettingsState.getInstance(),
                    LevelPackSelectState.getInstance(),
                    LevelSelectState.getInstance(),
                    GameState.getInstance(),
                    TutorialState.getInstance()
            };

            for (State state : states) {
                state.initialize(glSurfaceView.getRenderer(), soundPool, Main.this);
            }

            currentState = MainMenuState.getInstance();
            currentState.entry();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (currentState != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                currentState.onBackPressed();
                switchState();
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (currentState != null) {
            event.offsetLocation(-glSurfaceView.getX(), -glSurfaceView.getY());
            currentState.onTouchEvent(event);
            switchState();
        }
        return false;
    }

    private void switchState() {
        State newState = currentState.next();
        if (currentState != newState) {
            currentState.exit();
            currentState = newState;
            currentState.entry();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
