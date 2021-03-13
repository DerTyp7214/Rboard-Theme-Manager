package de.dertyp7214.rboardthememanager.component

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.DialogPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.preferencesplus.components.ColorSeekBar
import de.dertyp7214.rboardthememanager.R

class CustomDialogPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {

    constructor(context: Context?): this(context, null)

    private var defaultValue: Int = 95

    override fun onClick() {
        MaterialDialog(context).show {
            setContentView(R.layout.keyboard_height)

            val default = findViewById<Button>(R.id.default_button)
            val cancel = findViewById<Button>(R.id.cancel_button)
            val ok = findViewById<Button>(R.id.ok_button)

            val seekBar = findViewById<ColorSeekBar>(R.id.seekBar)

            val title = findViewById<TextView>(R.id.title)
            val value = findViewById<TextView>(R.id.value)

            seekBar.setColor(context.getColor(R.color.colorAccent))

            title.text = this@CustomDialogPreference.title

            val progress = try {
                sharedPreferences.getInt("${key}_pref", defaultValue)
            } catch (e: Exception) {
                defaultValue
            }

            value.text = (progress.toDouble() / 100).toString()
            seekBar.progress = progress

            default.setOnClickListener {
                seekBar.progress = defaultValue
            }

            cancel.setOnClickListener { dismiss() }
            ok.setOnClickListener {
                onPreferenceChangeListener.onPreferenceChange(
                    this@CustomDialogPreference,
                    seekBar.progress
                )
                dismiss()
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    value.text = "${progress.toDouble() / 100}"
                }
            })
        }
    }

    fun setDefaultValue(defaultValue: Int) {
        super.setDefaultValue(defaultValue)
        this.defaultValue = defaultValue
    }
}