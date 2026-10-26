package com.example.dotstatus

import android.content.Context
import android.graphics.*
import android.view.View

/** Draws: WiFi arcs, dot-column signal bars, pill battery. */
class StatusIconsView(context: Context) : View(context) {
    var wifiLevel = 0      // 0..4, -1 = no wifi
    var signalLevel = 0    // 0..4
    var batteryPct = 100
    var charging = false

    private val d = resources.displayMetrics.density
    private val on = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val off = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 255, 255, 255) }
    private val arc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 1.8f * d
    }

    init { setBackgroundColor(Color.BLACK) }

    override fun onMeasure(w: Int, h: Int) {
        setMeasuredDimension((86 * d).toInt(), MeasureSpec.getSize(h).coerceAtLeast((24 * d).toInt()))
    }

    override fun onDraw(c: Canvas) {
        val cy = height / 2f
        var x = 8 * d

        // WiFi: 3 arcs + dot
        if (wifiLevel >= 0) {
            val cx = x + 8 * d; val base = cy + 5 * d
            for (i in 0..2) {
                val r = (4 + i * 4) * d
                arc.color = if (wifiLevel > i) Color.WHITE else Color.argb(70, 255, 255, 255)
                c.drawArc(RectF(cx - r, base - r, cx + r, base + r), 225f, 90f, false, arc)
            }
            c.drawCircle(cx, base, 1.4f * d, if (wifiLevel >= 0) on else off)
        }
        x += 22 * d

        // Signal: 4 dotted columns, heights 1..4 dots
        val gap = 4 * d; val r = 1.1f * d
        for (col in 0..3) {
            val p = if (signalLevel > col) on else off
            for (row in 0..col) {
                c.drawCircle(x + col * 5 * d, cy + 6 * d - row * gap, r, p)
            }
        }
        x += 26 * d

        // Battery pill
        val bw = 22 * d; val bh = 12 * d
        val body = RectF(x, cy - bh / 2, x + bw, cy + bh / 2)
        c.drawRoundRect(body, 3.5f * d, 3.5f * d, off)
        val fill = RectF(body.left, body.top, body.left + bw * batteryPct / 100f, body.bottom)
        val clip = Path().apply { addRoundRect(body, 3.5f * d, 3.5f * d, Path.Direction.CW) }
        c.save(); c.clipPath(clip)
        val low = batteryPct <= 15 && !charging
        c.drawRect(fill, if (low) Paint(on).apply { color = Color.rgb(255, 80, 80) } else on)
        c.restore()
        c.drawRoundRect(RectF(body.right + 1 * d, cy - 2.5f * d, body.right + 3 * d, cy + 2.5f * d), d, d, on)
    }
}
