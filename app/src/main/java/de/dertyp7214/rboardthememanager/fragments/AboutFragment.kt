package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.ClickListener
import de.dertyp7214.rboardthememanager.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        binding = FragmentAboutBinding.bind(v)

        binding.clickTyp = ClickListener {
            Toast.makeText(context, "TYP", Toast.LENGTH_LONG).show()
        }

        binding.clickRk = ClickListener {
            Toast.makeText(context, "RK", Toast.LENGTH_LONG).show()
        }

        Picasso.get().load("https://avatars0.githubusercontent.com/u/37804065")
            .into(v.findViewById<ImageView>(R.id.typ_image))
        Picasso.get().load("https://avatars1.githubusercontent.com/u/22264125")
            .into(v.findViewById<ImageView>(R.id.rk_image))

        return v
    }
}
