package ex.MediaPlayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ImageView ivCover, ivSkipPrev, ivBack15, ivPlay, ivPause, ivNext15, ivSkipNext, ivSpeed, ivLoopOne, ivLoopAll, ivStopLoop, ivStop, ivPlayService;
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
}