package com.hitomaru.volumecontrol

import android.content.Context
import android.database.ContentObserver
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

class RingerModeListAdapter(
    private val _context: Context,
    private val _list: List<RingerMode>
) : BaseAdapter() {
    internal class ViewHolder {
        var icon: ImageView? = null
        var label: TextView? = null
        var bar: SeekBar? = null
    }

    private val _activity = _context as MainActivity
    private val _audio = _context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _seekBarList = listOf<SeekBar?>().toMutableList()

    init {
        _context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    updateSeekBars()
                }
            })
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(_context).inflate(R.layout.ringer_mode_item, null)
            viewHolder = ViewHolder()
            viewHolder.icon = view.findViewById(R.id.ringer_mode_icon)
            viewHolder.label = view.findViewById(R.id.ringer_mode_label)
            viewHolder.bar = view.findViewById(R.id.ringer_mode_bar)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        updateViewHolder(viewHolder, position)
        _seekBarList.add(viewHolder.bar)

        return view as View
    }

    override fun getItem(position: Int): Any {
        return _list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return _list.size
    }

    fun updateSeekBars() {
        _seekBarList.forEachIndexed { i, seekBar ->
            seekBar?.progress = _audio.getStreamVolume(_list[i].id)
        }
    }

    private fun updateViewHolder(viewHolder: ViewHolder, position: Int) {
        val mode = _list[position]

        viewHolder.icon?.setImageIcon(Icon.createWithResource(_context, mode.icon))

        viewHolder.label?.text = _context.getString(mode.label)
        viewHolder.label?.textSize = 20f

        viewHolder.bar?.max = _audio.getStreamMaxVolume(mode.id)
        viewHolder.bar?.min = _audio.getStreamMinVolume(mode.id)
        viewHolder.bar?.progress = _audio.getStreamVolume(mode.id)

        viewHolder.bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
                _activity.ringtone?.stop()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
                _audio.setStreamVolume(mode.id, seekBar.progress, 0)
                vibrate()
                playSound(mode.id)
            }
        })
    }

    private fun vibrate() {
        val vibrator = _context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                100,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    private fun playSound(streamType: Int) {
        // Only play the sound on normal mode
        if (_audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            return
        }

        val volumeNow = _audio.getStreamVolume(streamType)
        val volumeMax = _audio.getStreamMaxVolume(streamType)
        val volumeValue = (volumeNow.toDouble() / volumeMax).toFloat()
        val ringtoneUri = getRingtoneUri(streamType)
        val ringtone = RingtoneManager.getRingtone(_context, ringtoneUri)
        ringtone?.volume = volumeValue
        ringtone?.play()

        _activity.ringtone = ringtone
    }

    private fun getRingtoneUri(streamType: Int): Uri {
        when (streamType) {
            AudioManager.STREAM_RING ->
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            AudioManager.STREAM_NOTIFICATION ->
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AudioManager.STREAM_ALARM ->
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        return Uri.parse("content://media/internal/audio/media/122")
    }
}
