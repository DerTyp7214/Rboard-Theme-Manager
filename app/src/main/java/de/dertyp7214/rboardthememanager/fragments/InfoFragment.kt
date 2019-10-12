package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.MagiskInfo
import de.dertyp7214.rboardthememanager.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_info, container, false)
        binding = FragmentInfoBinding.bind(v)

        binding.magisk = MagiskInfo("WUUUUT", 1337, "JA LOL")

        return v
    }
}
