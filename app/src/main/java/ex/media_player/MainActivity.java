package ex.media_player;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ex.MediaPlayer.R;
import ex.media_player.bubbles.BubbleLayout;
import ex.media_player.bubbles.BubblesManager;
import ex.media_player.bubbles.OnInitializedCallback;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    ImageView ivCover, ivSkipPrev, ivBack15, ivPlay, ivPause, ivNext15, ivSkipNext, ivSpeed, ivLoopOne, ivLoopAll, ivStopLoop, ivStop, ivPlayService, ivBubble, ivPip;
    SeekBar seekBar;
    TextView tvCurrent, tvAll;
    double startTime = 0;
    double finalTime = 0;
    MediaPlayer mediaPlayer;
    Handler myHandler = new Handler();
    int forwardTime = 15000;
    int backwardTime = 15000;
    int oneTimeOnly = 0;
    String url = "";
    int currentPosition = 1;
    Boolean callSound = false;
    Boolean isComplete = false;
    int checkLoop = 3;
    final int LOOP_ONE = 1;
    final int LOOP_ALL = 2;
    final int STOP_LOOP = 3;

    @SuppressLint({"MissingInflatedId", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivCover = findViewById(R.id.ivCover);
        ivSkipPrev = findViewById(R.id.ivSkipPrev);
        ivBack15 = findViewById(R.id.ivBack15);
        ivPlay = findViewById(R.id.ivPlay);
        ivPause = findViewById(R.id.ivPause);
        ivNext15 = findViewById(R.id.ivNext15);
        ivSkipNext = findViewById(R.id.ivSkipNext);
        ivSpeed = findViewById(R.id.ivSpeed);
        ivLoopOne = findViewById(R.id.ivLoopOne);
        ivLoopAll = findViewById(R.id.ivLoopAll);
        ivStopLoop = findViewById(R.id.ivStopLoop);
        ivStop = findViewById(R.id.ivStop);
        seekBar = findViewById(R.id.seekBar);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvAll = findViewById(R.id.tvAll);
        ivPlayService = findViewById(R.id.ivPlayService);
        ivBubble = findViewById(R.id.ivBubble);
        ivPip = findViewById(R.id.ivPiP);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + currentPosition + ".mp3";
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(mp -> {
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    callSound = true;
                });
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    return false;
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_SHORT).show();
                    isComplete = true;
                    loopMedia();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ivPlay.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
            mediaPlayer.start();
            checkTime();
        });

        ivPause.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
            mediaPlayer.pause();
        });

        ivStop.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
        });

        ivNext15.setOnClickListener(v -> {
            int temp = (int) startTime;
            if ((temp + forwardTime) <= finalTime) {
                startTime = startTime + forwardTime;
                mediaPlayer.seekTo((int) startTime);
                Toast.makeText(getApplicationContext(), "You have Jumped forward 15 seconds", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cannot jump forward 15 seconds", Toast.LENGTH_SHORT).show();
            }
        });

        ivBack15.setOnClickListener(v -> {
            int temp = (int) startTime;
            if ((temp - backwardTime) > 0) {
                startTime = startTime - backwardTime;
                mediaPlayer.seekTo((int) startTime);
                Toast.makeText(getApplicationContext(), "You have Jumped backward 15 seconds", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Cannot jump backward 15 seconds", Toast.LENGTH_SHORT).show();
            }
        });

        ivSkipNext.setOnClickListener(v -> {
            currentPosition = currentPosition + 1;
            if (currentPosition >= 17) {
                currentPosition = 1;
            }
            playNextBack();
            playMedia();
        });

        ivSkipPrev.setOnClickListener(v -> {
            currentPosition = currentPosition - 1;
            if (currentPosition < 1) {
                currentPosition = 17;
            }
            playNextBack();
            playMedia();
        });

        ivLoopOne.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Loop One", Toast.LENGTH_SHORT).show();
            checkLoop = LOOP_ONE;
            mediaPlayer.setLooping(true);
        });

        ivStopLoop.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Stop Loop", Toast.LENGTH_SHORT).show();
            checkLoop = STOP_LOOP;
            mediaPlayer.setLooping(false);
        });

        ivLoopAll.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Loop All", Toast.LENGTH_SHORT).show();
            checkLoop = LOOP_ALL;
            mediaPlayer.setLooping(false);
        });

        ivSpeed.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Set Speed 2x", Toast.LENGTH_SHORT).show();
            float speed = 2.0f;
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
        });

        ivPlayService.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Play Service", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, PlayServiceActivity.class);
            startActivity(intent);
            finish();
        });

        ivBubble.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Bubble", Toast.LENGTH_SHORT).show();
            if (isLayoutOverlayPermissionGranted(MainActivity.this)) {
                initializeBubblesManager();
            } else {
                grantLayoutOverlayPermission(MainActivity.this);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.prepareAsync();
    }

    private void playNextBack() {
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + currentPosition + ".mp3";
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void playMedia() {
        if (callSound) {
            mediaPlayer.start();
            checkTime();
        } else {
            Toast.makeText(getApplicationContext(), "Waiting", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    private void checkTime() {
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();
        if (oneTimeOnly == 0) {
            Log.d("check0", "0: " + finalTime);
            seekBar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        tvCurrent.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
        tvAll.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime), TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
        Log.d("check1", "1: " + startTime);
        seekBar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 1000);
    }

    private final Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tvCurrent.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
            Log.d("check", "2: " + startTime);
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 1000);
        }
    };

    private void loopMedia() {
        Log.d("checkLoop", checkLoop + "");
        switch (checkLoop) {
            case LOOP_ALL:
                mediaPlayer.setLooping(false);
                currentPosition = currentPosition + 1;
                if (currentPosition >= 17) {
                    currentPosition = 1;
                }
                playNextBack();
                playMedia();
                break;

            case LOOP_ONE:
                mediaPlayer.setLooping(true);
                playNextBack();
                playMedia();
                break;
            default:
                mediaPlayer.setLooping(false);
                currentPosition = currentPosition + 1;
                if (currentPosition >= 17) {
                    return;
                }
                playNextBack();
                playMedia();
                break;
        }
    }

    private BubblesManager bubblesManager = null;

    private void initializeBubblesManager() {
        if (bubblesManager != null) {
            bubblesManager.recycle();
        }

        bubblesManager = new BubblesManager.Builder(MyApp.appContext)
                .setTrashLayout(R.layout.bubble_trash)
                .setInitializationCallback(this::addNewBubble)
                .build();
        bubblesManager.initialize();
    }

    private void addNewBubble() {
        @SuppressLint("InflateParams") BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.bubble_media, null);

        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, 60, 60);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(MainActivity.this, "Vui lòng cấp quyền vị trí đế sử dụng ứng dụng", Toast.LENGTH_SHORT).show();
                startActivity(
                        new Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null)
                        )
                );
            }
        }
    }

    private boolean isLayoutOverlayPermissionGranted(Activity activity) {
        Log.v(TAG, "Granting Layout Overlay Permission..");
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity)) {
            Log.v(TAG, "Permission is denied");
            return false;
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void grantLayoutOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }
}