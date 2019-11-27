package com.hitomaru.volumecontrol

import android.content.Context
import android.database.ContentObserver
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView


class RingerModeListAdapter(private val _context: Context, private val _list: List<RingerMode>) :
    BaseAdapter() {

    internal class ViewHolder {
        var icon: ImageView? = null
        var label: TextView? = null
        var bar: SeekBar? = null
    }

    private val _listSeekBar = listOf<SeekBar?>().toMutableList()

    init {
        _context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    val audio = _context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    _listSeekBar.forEachIndexed { i, v ->
                        v?.progress = audio.getStreamVolume(_list[i].id)
                    }
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

        val audio = _context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = _list[position]

        viewHolder.icon?.setImageIcon(Icon.createWithResource(_context, mode.icon))
        viewHolder.label?.text = _context.getString(mode.label)

        viewHolder.bar?.max = audio.getStreamMaxVolume(mode.id)
        viewHolder.bar?.min = audio.getStreamMinVolume(mode.id)
        viewHolder.bar?.progress = audio.getStreamVolume(mode.id)
        viewHolder.bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Write code to perform some action when progress is changed.
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is started.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Write code to perform some action when touch is stopped.
                audio.setStreamVolume(mode.id, seekBar.progress, 0)
            }
        })

        _listSeekBar.add(viewHolder.bar)

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

}
