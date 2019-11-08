package de.dertyp7214.rboardthememanager.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.MaskedImageView
import de.dertyp7214.rboardthememanager.data.ClickListener
import de.dertyp7214.rboardthememanager.data.Paypal
import de.dertyp7214.rboardthememanager.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    private val typImage = "https://avatars0.githubusercontent.com/u/37804065"
    private val rkImage = "https://avatars1.githubusercontent.com/u/22264125"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        binding = FragmentAboutBinding.bind(v)

        binding.clickTyp = ClickListener {
            userPopUp(
                typImage,
                getString(R.string.typ_title),
                getString(R.string.typ_twitter),
                getString(R.string.typ_github),
                Paypal(getString(R.string.typ_paypal))
            )
        }

        binding.clickRk = ClickListener {
            userPopUp(
                rkImage,
                getString(R.string.rk_title),
                getString(R.string.rk_twitter),
                getString(R.string.rk_github),
                Paypal(getString(R.string.rk_paypal))
            )
        }

        Picasso.get().load(typImage).placeholder(R.drawable.ic_person)
            .into(v.findViewById<ImageView>(R.id.typ_image))
        Picasso.get().load(rkImage).placeholder(R.drawable.ic_person)
            .into(v.findViewById<ImageView>(R.id.rk_image))

        return v
    }

    private fun userPopUp(
        imageUrl: String,
        name: String,
        twitter: String,
        github: String,
        paypal: Paypal
    ) {
        MaterialDialog(context!!).show {
            setContentView(R.layout.user_popup)
            val displayMetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
            Picasso.get().load(imageUrl).fit().placeholder(R.drawable.ic_person)
                .into(findViewById<MaskedImageView>(R.id.userProfileImage))
            findViewById<TextView>(R.id.userName).text = name
            findViewById<LinearLayout>(R.id.clickTwitter).setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(twitter))
                startActivity(browserIntent)
                dismiss()
            }
            findViewById<LinearLayout>(R.id.clickGithub).setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(github))
                startActivity(browserIntent)
                dismiss()
            }
            findViewById<LinearLayout>(R.id.clickPaypal).setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paypal.me))
                startActivity(browserIntent)
                dismiss()
            }
        }
    }
}
