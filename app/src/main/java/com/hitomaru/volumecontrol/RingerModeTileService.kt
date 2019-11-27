package com.hitomaru.volumecontrol

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log

class RingerModeTileService : TileService() {

    private val tileData: Map<String, Any>
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

    override fun onCreate() {
        super.onCreate()
        Log.d("onCreate", "")
        updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d("onTileAdded", "")
        updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d("onTileRemoved", "")
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d("onStartListening", "")
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d("onStopListening", "")
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        updateRingerMode()
        updateTile()
    }

    private fun updateTile() {
        try {
            val data = tileData
            // Data
            qsTile.icon = Icon.createWithResource(this, data["icon"] as Int)
            qsTile.label = getString(data["label"] as Int)
            // Update looks
            qsTile.updateTile()
        } catch (ex: Exception) {
            Log.e("Tile", ex.message)
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
                when (audio.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL ->
                        audio.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    AudioManager.RINGER_MODE_VIBRATE,
                    AudioManager.RINGER_MODE_SILENT -> {
                        audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("RingerMode", ex.message)
        }
    }

}