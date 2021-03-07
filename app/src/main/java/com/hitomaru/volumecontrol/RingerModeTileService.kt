package com.hitomaru.volumecontrol

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log

class RingerModeTileService : TileService() {
    private val _tileData: Map<String, Any>
        get() {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (audio.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> return mapOf(
                    "icon" to R.drawable.ic_audio_ringer_mode_normal,
                    "label" to R.string.ringer_mode_normal
                )
                AudioManager.RINGER_MODE_VIBRATE -> return mapOf(
                    "icon" to R.drawable.ic_audio_ringer_mode_vibrate,
                    "label" to R.string.ringer_mode_vibrate
                )
                AudioManager.RINGER_MODE_SILENT -> return mapOf(
                    "icon" to R.drawable.ic_audio_ringer_mode_silent,
                    "label" to R.string.ringer_mode_silent
                )
            }
            return mapOf()
        }

    private lateinit var _receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        Log.d("RingerModeTileService", "onCreate")

        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateTile()
            }
        }
        val filter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        registerReceiver(receiver, filter)
        _receiver = receiver
    }

    override fun onClick() {
        super.onClick()
        Log.d("RingerModeTileService", "onClick")
        updateRingerMode()
    }

    override fun onDestroy() {
        super.onClick()
        Log.d("RingerModeTileService", "onDestroy")
        unregisterReceiver(_receiver)
    }

    private fun updateTile() {
        try {
            val data = _tileData
            // Data
            qsTile.icon = Icon.createWithResource(this, data["icon"] as Int)
            qsTile.label = getString(data["label"] as Int)
            // Update looks
            qsTile.updateTile()
        } catch (ex: Exception) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            Log.e("RingerModeTileService", ex.message)
        }
    }

    private fun updateRingerMode() {
        try {
            val notification = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Require DND permission
            if (!notification.isNotificationPolicyAccessGranted) {
                val intentSystemDialog = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                this.sendBroadcast(intentSystemDialog)
                val intentPolicyAccess = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                intentPolicyAccess.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intentPolicyAccess)
            }
            // Change ringer mode
            if (notification.isNotificationPolicyAccessGranted) {
                val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val nextRingerMode = getNextRingerMode(audio.ringerMode)
                audio.ringerMode = nextRingerMode
            }
        } catch (ex: Exception) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            Log.e("RingerModeTileService", ex.message)
        }
    }

    private fun getNextRingerMode(curRingerMode: Int): Int {
        when (curRingerMode) {
            AudioManager.RINGER_MODE_NORMAL ->
                return AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE ->
                return AudioManager.RINGER_MODE_SILENT
            AudioManager.RINGER_MODE_SILENT ->
                return AudioManager.RINGER_MODE_NORMAL
        }
        return AudioManager.RINGER_MODE_NORMAL
    }
}
