package com.goallive.pro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import android.graphics.Color
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var scores: TextView
    private val endpoint = "https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/scoreboard"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(24,32,24,24)
            setBackgroundColor(Color.rgb(10,18,32))
        }
        val title = TextView(this).apply {
            text = "⚽ GOAL LIVE PRO"; textSize = 26f; setTextColor(Color.WHITE)
        }
        scores = TextView(this).apply {
            text = "Memuat skor pertandingan..."; textSize = 16f
            setTextColor(Color.WHITE); setPadding(0,28,0,24)
        }
        val refresh = Button(this).apply { text = "Perbarui skor"; setOnClickListener { loadScores() } }
        val floating = Button(this).apply {
            text = "Aktifkan Floating Scoreboard"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    startForegroundService(Intent(this@MainActivity, FloatingScoreService::class.java))
                    Toast.makeText(this@MainActivity, "Floating scoreboard aktif", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(title); layout.addView(scores, LinearLayout.LayoutParams(-1,0,1f))
        layout.addView(refresh); layout.addView(floating)
        setContentView(layout)
        loadScores()
    }
    private fun loadScores() {
        scores.text = "Mengambil data pertandingan..."
        thread {
            val result = try {
                val conn = URL(endpoint).openConnection()
                conn.connectTimeout = 10000; conn.readTimeout = 10000
                val json = JSONObject(conn.getInputStream().bufferedReader().use { it.readText() })
                val events = json.optJSONArray("events")
                if (events == null || events.length() == 0) "Belum ada pertandingan pada kompetisi sumber data." else {
                    buildString {
                        for (i in 0 until events.length()) {
                            val event = events.getJSONObject(i)
                            val comp = event.optJSONArray("competitions")?.optJSONObject(0)
                            val competitors = comp?.optJSONArray("competitors")
                            if (competitors != null && competitors.length() >= 2) {
                                val home = competitors.getJSONObject(0); val away = competitors.getJSONObject(1)
                                append(home.optJSONObject("team")?.optString("displayName","Home"))
                                append("  ").append(home.optString("score","-"))
                                append("  -  ").append(away.optString("score","-")).append("  ")
                                append(away.optJSONObject("team")?.optString("displayName","Away"))
                                append("\n").append(comp.optJSONObject("status")?.optJSONObject("type")?.optString("shortDetail",""))
                                append("\n\n")
                            }
                        }
                    }.ifBlank { "Belum ada skor yang tersedia." }
                }
            } catch (e: Exception) { "Data belum bisa dimuat. Periksa koneksi internet.\n${e.message ?: ""}" }
            runOnUiThread { scores.text = result }
        }
    }
}
