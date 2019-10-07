package de.dertyp7214.rboardthememanager.component

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.helper.ThemeHelper

class SelectedThemeBottomSheet(
    private val theme: ThemeDataClass,
    private val defaultImage: Bitmap,
    private val color: Int,
    private val isDark: Boolean
) : RoundedBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.selected_theme_bottomsheet, container, false)

        val applyButton = v.findViewById<LinearLayout>(R.id.apply)
        val themeName = v.findViewById<TextView>(R.id.theme_name)
        val themeIcon = v.findViewById<ImageView>(R.id.theme_image)
        val card: CardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }

        if (gradient != null) {
            val g = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            gradient.background = g
        }

        card.setCardBackgroundColor(color)

        themeName.text = theme.name
        themeName.setTextColor(if (!isDark) Color.WHITE else Color.BLACK)
        themeIcon.setImageBitmap(theme.image ?: defaultImage)

        applyButton.setOnClickListener {
            val response = ThemeHelper.applyTheme("${theme.name}.zip")
            dismiss()
            if (response) Toast.makeText(
                context,
                getString(R.string.applied),
                Toast.LENGTH_SHORT
            ).show()
            else Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
        }

        return v
    }
}