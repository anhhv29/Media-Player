package ex.media_player.bubbles

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import ex.MediaPlayer.R
import ex.media_player.MyApp
import java.io.IOException
import java.util.concurrent.TimeUnit

class BubblesService : Service() {
    private val binder: BubblesServiceBinder = BubblesServiceBinder()
    private val bubbles: MutableList<BubbleLayout> = ArrayList()
    private var bubblesTrash: BubbleTrashLayout? = null
    private var windowManager: WindowManager? = null
    private var layoutCoordinator: BubblesLayoutCoordinator? = null
    var mediaPlayer: MediaPlayer? = null
    var url: String? = null
    var startTime = 0.0
    var finalTime = 0.0
    val myHandler = Handler()
    private val forwardTime = 15000
    private val backwardTime = 15000
    var oneTimeOnly = 0
    private var currentPosition = 1
    var callSound = false
    var isComplete = false
    private var checkLoop = 3
    private val loopOne = 1
    private val loopAll = 2
    private val stopLoop = 3
    private var seekBar: SeekBar? = null
    private var tvCurrent: TextView? = null
    private var tvAll: TextView? = null

    @Override
    override fun onCreate() {
        super.onCreate()
    }

    @Override
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
    }

    @Override
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        for (bubble in bubbles) {
            recycleBubble(bubble)
        }
        bubbles.clear()
        return super.onUnbind(intent)
    }

    private fun recycleBubble(bubble: BubbleLayout) {
        Handler(Looper.getMainLooper()).post {
            getWindowManager()!!.removeView(bubble)
            for (cachedBubble in bubbles) {
                if (cachedBubble === bubble) {
                    bubble.notifyBubbleRemoved()
                    bubbles.remove(cachedBubble)
                    break
                }
            }
        }
    }

    private fun getWindowManager(): WindowManager? {
        if (windowManager == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        }
        return windowManager
    }

    @SuppressLint("SetTextI18n")
    fun addBubble(bubble: BubbleLayout, x: Int, y: Int) {
        val layoutParams = buildLayoutParamsForBubble(x, y)
        bubble.windowManager = getWindowManager()
        bubble.viewParams = layoutParams
        bubble.layoutCoordinator = layoutCoordinator
        bubbles.add(bubble)
        addViewToWindow(bubble)
        seekBar = bubble.findViewById(R.id.seekBar)
        tvCurrent = bubble.findViewById(R.id.tvCurrent)
        tvAll = bubble.findViewById(R.id.tvAll)
        val ivSkipPrev = bubble.findViewById<ImageView>(R.id.ivSkipPrev)
        val ivSkipNext = bubble.findViewById<ImageView>(R.id.ivSkipNext)
        val ivBack15 = bubble.findViewById<ImageView>(R.id.ivBack15)
        val ivNext15 = bubble.findViewById<ImageView>(R.id.ivNext15)
        val ivPlay = bubble.findViewById<ImageView>(R.id.ivPlay)
        val ivPause = bubble.findViewById<ImageView>(R.id.ivPause)
        val ivSpeed = bubble.findViewById<ImageView>(R.id.ivSpeed)
        val ivLoopOne = bubble.findViewById<ImageView>(R.id.ivLoopOne)
        val ivLoopAll = bubble.findViewById<ImageView>(R.id.ivLoopAll)
        val ivStopLoop = bubble.findViewById<ImageView>(R.id.ivStopLoop)
        val ivStop = bubble.findViewById<ImageView>(R.id.ivStop)

        getMediaPlayer(
            ivSkipPrev,
            ivSkipNext,
            ivBack15,
            ivNext15,
            ivPlay,
            ivPause,
            ivSpeed,
            ivLoopOne,
            ivLoopAll,
            ivStopLoop,
            ivStop
        )
    }

    fun addTrash(trashLayoutResourceId: Int) {
        if (trashLayoutResourceId != 0) {
            bubblesTrash = BubbleTrashLayout(this)
            bubblesTrash?.windowManager = windowManager
            bubblesTrash?.viewParams = buildLayoutParamsForTrash()
            bubblesTrash?.visibility = View.GONE
            LayoutInflater.from(this).inflate(trashLayoutResourceId, bubblesTrash, true)
            addViewToWindow(bubblesTrash!!)
            initializeLayoutCoordinator()
        }
    }

    private fun initializeLayoutCoordinator() {
        layoutCoordinator =
            BubblesLayoutCoordinator.Builder(this).setWindowManager(getWindowManager())
                .setTrashView(bubblesTrash).build()
    }

    private fun addViewToWindow(view: BubbleBaseLayout) {
        Handler(Looper.getMainLooper()).post { getWindowManager()!!.addView(view, view.viewParams) }
    }

    private fun buildLayoutParamsForBubble(x: Int, y: Int): WindowManager.LayoutParams {
        val layoutParams: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            860,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutParams,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = x
        params.y = y
        return params
    }

    private fun buildLayoutParamsForTrash(): WindowManager.LayoutParams {
        val layoutParams: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val x = 0
        val y = 0
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParams,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT
        )
        params.x = x
        params.y = y
        return params
    }

    fun removeBubble(bubble: BubbleLayout) {
        recycleBubble(bubble)
        mediaPlayer?.stop()
        mediaPlayer?.prepareAsync()
    }

    inner class BubblesServiceBinder : Binder() {
        val service: BubblesService
            get() = this@BubblesService
    }

    @SuppressLint("SetTextI18n")
    private fun getMediaPlayer(
        ivSkipPrev: ImageView,
        ivSkipNext: ImageView,
        ivBack15: ImageView,
        ivNext15: ImageView,
        ivPlay: ImageView,
        ivPause: ImageView,
        ivSpeed: ImageView,
        ivLoopOne: ImageView,
        ivLoopAll: ImageView,
        ivStopLoop: ImageView,
        ivStop: ImageView
    ) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$currentPosition.mp3"
                try {
                    mediaPlayer?.setDataSource(url)
                    mediaPlayer?.prepare()
                    mediaPlayer?.setOnPreparedListener {
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                        callSound = true
                    }
                    mediaPlayer?.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
                        Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                        false
                    }
                    mediaPlayer?.setOnCompletionListener {
                        Toast.makeText(applicationContext, "Completed", Toast.LENGTH_SHORT).show()
                        isComplete = true
                        loopMedia()
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            ivPlay.setOnClickListener {
                Log.e("bubble", "0")
                Toast.makeText(
                    applicationContext, "Play", Toast.LENGTH_SHORT
                ).show()
                mediaPlayer?.start()
                checkTime()
            }
            ivPause.setOnClickListener {
                Toast.makeText(
                    this, "Pause", Toast.LENGTH_SHORT
                ).show()
                Toast.makeText(applicationContext, "Pausing sound", Toast.LENGTH_SHORT).show()
                mediaPlayer?.pause()
            }
            ivStop.setOnClickListener {
                Toast.makeText(
                    this, "Stop", Toast.LENGTH_SHORT
                ).show()
                mediaPlayer?.stop()
                mediaPlayer?.prepareAsync()
            }
            ivSkipNext.setOnClickListener {
                Toast.makeText(
                    MyApp.appContext, "SkipNext", Toast.LENGTH_SHORT
                ).show()
                currentPosition += 1
                if (currentPosition >= 17) {
                    currentPosition = 1
                }
                playNextBack()
                playMedia()
            }
            ivSkipPrev.setOnClickListener {
                Toast.makeText(
                    this@BubblesService, "SkipPrev", Toast.LENGTH_SHORT
                ).show()
                currentPosition -= 1
                if (currentPosition < 1) {
                    currentPosition = 17
                }
                playNextBack()
                playMedia()
            }
            ivNext15.setOnClickListener {
                val temp = startTime.toInt()
                if (temp + forwardTime <= finalTime) {
                    startTime += forwardTime
                    mediaPlayer?.seekTo(startTime.toInt())
                    Toast.makeText(
                        applicationContext,
                        "You have Jumped forward 15 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Cannot jump forward 15 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            ivBack15.setOnClickListener {
                val temp = startTime.toInt()
                if (temp - backwardTime > 0) {
                    startTime -= backwardTime
                    mediaPlayer?.seekTo(startTime.toInt())
                    Toast.makeText(
                        applicationContext,
                        "You have Jumped backward 15 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Cannot jump backward 15 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            ivSpeed.setOnClickListener {
                Toast.makeText(
                    this, "Speed", Toast.LENGTH_SHORT
                ).show()
                val speed = 2.0f
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed)!!
            }
            ivLoopOne.setOnClickListener {
                Toast.makeText(
                    this, "LoopOne", Toast.LENGTH_SHORT
                ).show()
                checkLoop = loopOne
                mediaPlayer?.isLooping = true
            }
            ivLoopAll.setOnClickListener {
                Toast.makeText(
                    this, "LoopAll", Toast.LENGTH_SHORT
                ).show()
                checkLoop = loopAll
                mediaPlayer!!.isLooping = false
            }
            ivStopLoop.setOnClickListener {
                Toast.makeText(
                    this, "StopLoop", Toast.LENGTH_SHORT
                ).show()
                checkLoop = stopLoop
                mediaPlayer?.isLooping = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loopMedia() {
        Log.d("checkLoop", checkLoop.toString() + "")
        when (checkLoop) {
            loopAll -> {
                mediaPlayer?.isLooping = false
                currentPosition += 1
                if (currentPosition >= 17) {
                    currentPosition = 1
                }
                playNextBack()
                playMedia()
            }

            loopOne -> {
                mediaPlayer?.isLooping = true
                playNextBack()
                playMedia()
            }

            else -> {
                mediaPlayer?.isLooping = false
                currentPosition += 1
                if (currentPosition >= 17) {
                    return
                }
                playNextBack()
                playMedia()
            }
        }
    }

    private fun playNextBack() {
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$currentPosition.mp3"
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepare()
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    private fun playMedia() {
        if (callSound) {
            mediaPlayer?.start()
            checkTime()
        } else {
            Toast.makeText(applicationContext, "Waiting", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun checkTime() {
        finalTime = mediaPlayer?.duration?.toDouble()!!
        startTime = mediaPlayer?.currentPosition?.toDouble()!!
        if (oneTimeOnly == 0) {
            seekBar?.max = finalTime.toInt()
            oneTimeOnly = 1
        }
        tvCurrent?.text = String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())
            )
        )
        tvAll?.text = String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())
            )
        )
        Log.d("check1", "1: $startTime")
        seekBar?.progress = startTime.toInt()
        myHandler.postDelayed(updateSongTime, 1000)
    }

    private val updateSongTime: Runnable = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            startTime = mediaPlayer?.currentPosition?.toDouble()!!
            tvCurrent?.text = String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())
                )
            )
            Log.d("check", "2: $startTime")
            seekBar?.progress = startTime.toInt()
            myHandler.postDelayed(this, 1000)
        }
    }
}