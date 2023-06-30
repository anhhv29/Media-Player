package ex.MediaPlayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayServiceActivity extends AppCompatActivity {
    ImageView ivCover, ivSkipPrev, ivBack15, ivPlay, ivPause, ivNext15, ivSkipNext, ivSpeed, ivLoopOne, ivLoopAll, ivStopLoop, ivStop;
    SeekBar seekBar;
    TextView tvCurrent, tvAll;
    
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
}