package com.example.dotstatus

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.WindowManager

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: StatusIconsView
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() { refresh(); handler.postDelayed(this, 3000) }
    }

    override fun onCreate() {
        super.onCreate()
        val ch = "dotstatus"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(ch, "Dot Status", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, ch)
            .setContentTitle("Dot Status running")
            .setSmallIcon(android.R.drawable.stat_notify_more).build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = StatusIconsView(this)
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        val h = if (id > 0) resources.getDimensionPixelSize(id) else 80
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END }
        wm.addView(view, lp)
        handler.post(tick)
    }

    private fun refresh() {
        // Battery
        val b = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (b != null) {
            val lvl = b.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
            val scale = b.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            view.batteryPct = lvl * 100 / scale
            val st = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            view.charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
        }
        // WiFi
        val cm = getSystemService(ConnectivityManager::class.java)
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        view.wifiLevel = if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            @Suppress("DEPRECATION")
            val w = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            w.calculateSignalLevel(w.connectionInfo.rssi).coerceIn(0, 4)
        } else -1
        // Mobile signal
        view.signalLevel = try {
            (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).signalStrength?.level ?: 0
        } catch (e: SecurityException) { 0 }
        view.invalidate()
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        runCatching { wm.removeView(view) }
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null
}
