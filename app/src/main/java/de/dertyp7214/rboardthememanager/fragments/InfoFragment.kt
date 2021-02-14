package de.dertyp7214.rboardthememanager.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.Config.themeCount
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.MagiskInfo
import de.dertyp7214.rboardthememanager.data.Themes
import de.dertyp7214.rboardthememanager.data.Versions
import de.dertyp7214.rboardthememanager.databinding.FragmentInfoBinding
import de.dertyp7214.rboardthememanager.utils.GboardUtils.getGboardVersion
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import de.dertyp7214.rboardthememanager.utils.ThemeUtils.loadThemes

class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        binding = FragmentInfoBinding.bind(v)

        val fab = v.findViewById<FloatingActionButton>(R.id.installModule)
        val usingModule = MagiskUtils.getModules().any { it.id == Config.MODULE_ID }

        binding.magisk = MagiskInfo(
            MagiskUtils.getMagiskVersionString().removeSuffix(":MAGISK"),
            MagiskUtils.getMagiskVersionNumber().toInt(),
            MagiskUtils.getMagiskVersionFullString()
        )

        binding.versions = Versions(
            getGboardVersion(requireContext()).split("-")[0],
            Build.VERSION.RELEASE,
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )

        binding.themes = Themes(
            if (usingModule) getString(R.string.magisk) else getString(
                R.string.root
            ),
            themeCount?.toString() ?: getString(R.string.loading),
            THEME_LOCATION
        )

        if (themeCount === null) Thread {
            themeCount = loadThemes().size
            activity?.runOnUiThread {
                binding.themes = Themes(
                    if (usingModule) getString(R.string.magisk) else getString(
                        R.string.root
                    ),
                    themeCount!!.toString(),
                    THEME_LOCATION
                )
            }
        }.start()

        fab.visibility = if (usingModule) GONE else VISIBLE

        return v
    }
}
