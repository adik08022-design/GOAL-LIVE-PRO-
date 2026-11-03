package com.goallive.pro

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView

class FloatingScoreService : Service() {
    private var windowManager: WindowManager? = null
    private var floating: View? = null
    override fun onCreate() {
        super.onCreate()
        val channel = "goal_live_overlay"
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(channel, "GOAL LIVE", NotificationManager.IMPORTANCE_LOW))
        }
        val notification = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(this, channel).setContentTitle("GOAL LIVE PRO").setContentText("Floating scoreboard berjalan").setSmallIcon(android.R.drawable.ic_media_play).build()
        else Notification.Builder(this).setContentTitle("GOAL LIVE PRO").setSmallIcon(android.R.drawable.ic_media_play).build()
        startForeground(7, notification)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val text = TextView(this).apply {
            text = "⚽ GOAL LIVE PRO\nSkor live tampil di aplikasi"
            setTextColor(Color.WHITE); setBackgroundColor(0xEE101B2D.toInt())
            textSize = 14f; setPadding(18,12,18,12)
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x=20; y=140 }
        text.setOnTouchListener(object : View.OnTouchListener {
            var sx=0f; var sy=0f; var px=0; var py=0
            override fun onTouch(v: View, e: android.view.MotionEvent): Boolean {
                when(e.action) {
                    MotionEvent.ACTION_DOWN -> { sx=e.rawX; sy=e.rawY; px=params.x; py=params.y; return true }
                    MotionEvent.ACTION_MOVE -> { params.x=px-(e.rawX-sx).toInt(); params.y=py+(e.rawY-sy).toInt(); windowManager?.updateViewLayout(text,params); return true }
                }; return true
            }
        })
        floating = text
        windowManager?.addView(text, params)
    }
    override fun onDestroy() { floating?.let { windowManager?.removeView(it) }; floating=null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
