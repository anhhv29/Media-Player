package ex.MediaPlayer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

public class AudioService extends Service {
    MediaPlayer mediaPlayer;
    String url;
    int temp;
    double startTime = 0;
    double finalTime = 0;
    Handler myHandler = new Handler();
    int forwardTime = 15000;
    int backwardTime = 15000;
    int oneTimeOnly = 0;
    int currentPosition = 1;
    Boolean callSound = false;
    Boolean isComplete = false;
    int checkLoop = 3;
    final int LOOP_ONE = 1;
    final int LOOP_ALL = 2;
    final int STOP_LOOP = 3;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

        String actionService = intent.getExtras().getString("playService", "");

        switch (actionService) {
            case "play":
                Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();
                playMedia();
                break;
            case "pause":
                Toast.makeText(getApplicationContext(), "Pause sound", Toast.LENGTH_SHORT).show();
                mediaPlayer.pause();
                break;
            case "stop":
                Toast.makeText(getApplicationContext(), "Stop sound", Toast.LENGTH_SHORT).show();
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
                break;
            case "next15":
                Toast.makeText(getApplicationContext(), "Next 15", Toast.LENGTH_SHORT).show();
                temp = (int) startTime;
                if ((temp + forwardTime) <= finalTime) {
                    startTime = startTime + forwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped forward 15 seconds", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 15 seconds", Toast.LENGTH_SHORT).show();
                }
                break;
            case "back15":
                Toast.makeText(getApplicationContext(), "Back 15", Toast.LENGTH_SHORT).show();
                temp = (int) startTime;
                if ((temp - backwardTime) > 0) {
                    startTime = startTime - backwardTime;
                    mediaPlayer.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped backward 15 seconds", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 15 seconds", Toast.LENGTH_SHORT).show();
                }
                break;
            case "skipNext":
                Toast.makeText(getApplicationContext(), "Skip Next", Toast.LENGTH_SHORT).show();
                currentPosition = currentPosition + 1;
                if (currentPosition >= 17) {
                    currentPosition = 1;
                }
                playNextBack();
                playMedia();
                break;
            case "skipPrev":
                Toast.makeText(getApplicationContext(), "Skip Prev", Toast.LENGTH_SHORT).show();
                currentPosition = currentPosition - 1;
                if (currentPosition < 1) {
                    currentPosition = 17;
                }
                playNextBack();
                playMedia();
                break;
            case "loopOne":
                Toast.makeText(getApplicationContext(), "Loop One", Toast.LENGTH_SHORT).show();
                checkLoop = LOOP_ONE;
                mediaPlayer.setLooping(true);
                break;
            case "stopLoop":
                Toast.makeText(getApplicationContext(), "Stop Loop", Toast.LENGTH_SHORT).show();
                checkLoop = STOP_LOOP;
                mediaPlayer.setLooping(false);
                break;
            case "loopAll":
                Toast.makeText(getApplicationContext(), "Loop All", Toast.LENGTH_SHORT).show();
                checkLoop = LOOP_ALL;
                mediaPlayer.setLooping(false);
                break;
            case "speed":
                Toast.makeText(getApplicationContext(), "Set Speed 2x", Toast.LENGTH_SHORT).show();
                float speed = 2.0f;
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                break;
            default:
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        mediaPlayer.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
//            seekBar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
//        tvCurrent.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
//        tvAll.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime), TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
        Log.d("check1", "1: " + startTime);
//        seekBar.setProgress((int) startTime);
        myHandler.postDelayed(UpdateSongTime, 1000);
    }

    private final Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
//            tvCurrent.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
            Log.d("check", "2: " + startTime);
//            seekBar.setProgress((int) startTime);
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
