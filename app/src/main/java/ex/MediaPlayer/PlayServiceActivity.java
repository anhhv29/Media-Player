package ex.MediaPlayer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class PlayServiceActivity extends AppCompatActivity {
    ImageView ivCover, ivSkipPrev, ivBack15, ivPlay, ivPause, ivNext15, ivSkipNext, ivSpeed, ivLoopOne, ivLoopAll, ivStopLoop, ivStop;
    SeekBar seekBar;
    TextView tvCurrent, tvAll;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Broadcast Receiver", Toast.LENGTH_SHORT).show();
            double startTime = intent.getExtras().getDouble("startTime");
            double finalTime = intent.getExtras().getDouble("finalTime");
            Log.d("receive", "0: " + startTime);
            Log.d("receive", "1: " + finalTime);
            tvCurrent.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) startTime), TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
            tvAll.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime), TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
            seekBar.setProgress((int) startTime);
            seekBar.setMax((int) finalTime);
        }
    };

    @SuppressLint({"MissingInflatedId", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

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

        Intent intent = new Intent(PlayServiceActivity.this, AudioService.class);

        ivPlay.setOnClickListener(v -> {
            intent.putExtra("playService", "play");
            startService(intent);
        });

        ivPause.setOnClickListener(v -> {
            intent.putExtra("playService", "pause");
            startService(intent);
        });

        ivStop.setOnClickListener(v -> {
            intent.putExtra("playService", "stop");
            startService(intent);
        });

        ivNext15.setOnClickListener(v -> {
            intent.putExtra("playService", "next15");
            startService(intent);

        });

        ivBack15.setOnClickListener(v -> {
            intent.putExtra("playService", "back15");
            startService(intent);
        });

        ivSkipNext.setOnClickListener(v -> {
            intent.putExtra("playService", "skipNext");
            startService(intent);
        });

        ivSkipPrev.setOnClickListener(v -> {
            intent.putExtra("playService", "skipPrev");
            startService(intent);
        });

        ivLoopOne.setOnClickListener(v -> {
            intent.putExtra("playService", "loopOne");
            startService(intent);
        });

        ivStopLoop.setOnClickListener(v -> {
            intent.putExtra("playService", "stopLoop");
            startService(intent);
        });

        ivLoopAll.setOnClickListener(v -> {
            intent.putExtra("playService", "loopAll");
            startService(intent);
        });

        ivSpeed.setOnClickListener(v -> {
            intent.putExtra("playService", "speed");
            startService(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("broadcast");
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }
}