package ex.MediaPlayer;

import android.annotation.SuppressLint;
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
    ImageView ivCover, ivSkipPrev, ivBack15, ivPlay, ivPause, ivNext15, ivSkipNext, ivSpeed, ivRepeat, ivClose;
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
        ivRepeat = findViewById(R.id.ivRepeat);
        ivClose = findViewById(R.id.ivClose);
        seekBar = findViewById(R.id.seekBar);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvAll = findViewById(R.id.tvAll);

        mediaPlayer = new MediaPlayer();

        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + currentPosition + ".mp3";
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                callSound = true;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                return false;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ivPlay.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
            playMedia();
        });

        ivPause.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
            mediaPlayer.pause();
        });

        ivClose.setOnClickListener(v -> {
            callSound = false;
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
//            mediaPlayer.release();
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
    }

    private void playNextBack() {
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + currentPosition + ".mp3";
        try {
            mediaPlayer.reset();
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
//            mediaPlayer.setOnCompletionListener(mp -> Toast.makeText(getApplicationContext(), "Completed", Toast.LENGTH_SHORT).show());
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
}