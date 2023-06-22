package ex.MediaPlayer;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
    int unit = 1;

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
        String url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-" + unit + ".mp3";

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ivPlay.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
            mediaPlayer.start();
            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();
            if (oneTimeOnly == 0) {
                seekBar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }

            tvAll.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime))));

            tvCurrent.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    startTime))));
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
        });

        ivPause.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Pausing sound", Toast.LENGTH_SHORT).show();
            mediaPlayer.pause();
        });

        ivClose.setOnClickListener(v -> {
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
    }

    private final Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            tvCurrent.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };
}