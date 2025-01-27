package com.hitomaru.volumecontrol

import android.media.AudioManager
import android.media.Ringtone
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var _adapter: RingerModeListAdapter
    private var _ringtone: Ringtone? = null

    var ringtone: Ringtone?
        get() = _ringtone
        set(value) {
            _ringtone = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        initListView()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("MainActivity", "onWindowFocusChanged")
        _ringtone?.stop()
        if (hasFocus) {
            _adapter.updateSeekBars()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initListView() {
        val lv = findViewById<ListView>(R.id.ringer_mode_list)
        val modes = listOf(
            RingerMode(
                R.drawable.ic_audio_stream_music,
                R.string.stream_music,
                AudioManager.STREAM_MUSIC
            ),
            RingerMode(
                R.drawable.ic_audio_stream_call,
                R.string.stream_call,
                AudioManager.STREAM_VOICE_CALL
            ),
            RingerMode(
                R.drawable.ic_audio_stream_ring,
                R.string.stream_ring,
                AudioManager.STREAM_RING
            ),
            RingerMode(
                R.drawable.ic_audio_stream_notifications,
                R.string.stream_notification,
                AudioManager.STREAM_NOTIFICATION
            ),
            RingerMode(
                R.drawable.ic_audio_stream_alarm,
                R.string.stream_alarm,
                AudioManager.STREAM_ALARM
            )
        )

        _adapter = RingerModeListAdapter(this, modes)
        lv.adapter = _adapter
    }
}
