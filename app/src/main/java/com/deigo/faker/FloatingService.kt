package com.deigo.faker

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import kotlin.math.abs


class FloatingService : Service() {

    companion object{
        var image: Drawable? = null
    }

    var viewRoot: View? = null
    var layoutView: View? = null
    var windowManager: WindowManager? = null
    var rootParams: WindowManager.LayoutParams? = null
    var imageView: ImageView? = null
    var width = 0
    private lateinit var displayMetrics: DisplayMetrics

    val MAX_CLICKS = 4
    val TIME_INTERVAL_FOR_CLICKS = 2000L
    var clicks = 0
    var lastClickTime = 0L
    
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        when (intent.action){
            "STOP" -> stopService()
            "CLOSE" -> closeBlackLayout()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        displayMetrics = resources.displayMetrics
        layoutView = LayoutInflater.from(this@FloatingService).inflate(R.layout.overlay_layout, null)

        layoutView!!.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if(clicks == 0 || (currentTime - lastClickTime <= TIME_INTERVAL_FOR_CLICKS)){
                clicks++
                lastClickTime = currentTime

                if(clicks == MAX_CLICKS){
                    closeBlackLayout()
                    clicks = 0
                }
            }
            else{
                clicks = 1
                lastClickTime = currentTime
            }
        }

        if (rootParams == null) {
            val LAYOUT_FLAG: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            rootParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "com.deigo.faker",
                    "Faker",
                    NotificationManager.IMPORTANCE_HIGH
                )
                
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                
                val notificationManager =
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                notificationManager.createNotificationChannel(channel)
                
                val builder = NotificationCompat.Builder(this, "com.deigo.faker")
                
                val notification: Notification = builder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Floating Layout Service is Running")
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .addAction(R.drawable.close_icon,"Close", PendingIntent.getService(this, 0, Intent(this, FloatingService::class.java).apply {
                        action = "STOP"
                    }, PendingIntent.FLAG_IMMUTABLE))
                    .addAction(R.drawable.close_icon, "Close Black Screen", PendingIntent.getService(this, 0, Intent(this, FloatingService::class.java).apply {
                        action = "CLOSE"
                    }, PendingIntent.FLAG_IMMUTABLE))
                    .build()
                startForeground(2, notification)
            }
            
            if (viewRoot == null) {
                viewRoot = LayoutInflater.from(this).inflate(R.layout.floating_layout, null)
                rootParams!!.gravity = Gravity.CENTER_HORIZONTAL or Gravity.START
                rootParams!!.x = 0
                rootParams!!.y = 0
                windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager!!.addView(viewRoot, rootParams)
                imageView = viewRoot!!.findViewById(R.id.imageView)
                //   close = viewRoot!!.findViewById<ImageView>(R.id.close_button)
                // viewRoot!!.findViewById<View>(R.id.root)

                viewRoot!!.findViewById<ImageView>(R.id.imageView).setOnTouchListener(object : OnTouchListener {
                    private var initialX = 0
                    private var initialY = 0
                    private var initialTouchX = 0
                    private var initialTouchY = 0
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                initialX = rootParams!!.x
                                initialY = rootParams!!.y
                                initialTouchX = motionEvent.rawX.toInt()
                                initialTouchY = motionEvent.rawY.toInt()

                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                rootParams!!.x = initialX + (motionEvent.rawX - initialTouchX).toInt()
                                rootParams!!.y = initialY + (motionEvent.rawY - initialTouchY).toInt()
                                windowManager!!.updateViewLayout(viewRoot, rootParams)
                                val xDiff = (motionEvent.rawX - initialTouchX).toInt()
                                Log.e("EE", xDiff.toString())
                                val yDiff = (motionEvent.rawY - initialTouchY).toInt()
                                Log.e("EE", yDiff.toString())
                                if (abs(xDiff) < 10 && abs(yDiff) < 10) {
                                    val overlayLayout: ConstraintLayout? = viewRoot!!.findViewById(R.id.overlay_layout)
                                    overlayLayout?.background = image

                                    val layoutParamsForFullScreen = WindowManager.LayoutParams(
                                        displayMetrics.widthPixels + 2 * 800,
                                        displayMetrics.heightPixels + 2 * 800,
                                        LAYOUT_FLAG,
                                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                                                or WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                                        /* or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                         or View.SYSTEM_UI_FLAG_FULLSCREEN
                                         or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                         or View.SYSTEM_UI_FLAG_IMMERSIVE,*/
                                        PixelFormat.TRANSLUCENT
                                    )
                                    layoutParamsForFullScreen.gravity = Gravity.TOP

                                    layoutView!!.visibility = View.VISIBLE
                                   windowManager!!.addView(layoutView, layoutParamsForFullScreen)
                                }
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                rootParams!!.x = initialX + (motionEvent.rawX - initialTouchX).toInt()
                                rootParams!!.y = initialY + (motionEvent.rawY - initialTouchY).toInt()
                                windowManager!!.updateViewLayout(viewRoot, rootParams)
                                return true
                            }
                        }
                        return false
                    }
                })

                imageView!!.findViewById<ImageView>(R.id.imageView).setOnClickListener {

                }
            }
        }
    }

    private fun stopService() {
        try {
            stopForeground(true)
            stopSelf()
            windowManager!!.removeViewImmediate(viewRoot)
            windowManager!!.removeViewImmediate(layoutView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeBlackLayout(){
        try{
            windowManager!!.removeViewImmediate(layoutView)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}