package de.dertyp7214.rboardthememanager.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.MaskedImageView
import de.dertyp7214.rboardthememanager.data.ClickListener
import de.dertyp7214.rboardthememanager.data.Paypal
import de.dertyp7214.rboardthememanager.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    private val typImage = "https://avatars.githubusercontent.com/u/37804065"
    private val rkImage = "https://avatars.githubusercontent.com/u/22264125"
    private val nylonImage = "https://avatars.githubusercontent.com/u/18296061"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        binding = FragmentAboutBinding.bind(v)

        binding.clickTyp = ClickListener {
            userPopUp(
                requireContext(),
                typImage,
                getString(R.string.typ_title),
                getString(R.string.typ_twitter),
                getString(R.string.typ_github),
                Paypal(getString(R.string.typ_paypal))
            )
        }

        binding.clickRk = ClickListener {
            userPopUp(
                requireContext(),
                rkImage,
                getString(R.string.rk_title),
                getString(R.string.rk_twitter),
                getString(R.string.rk_github),
                Paypal(getString(R.string.rk_paypal))
            )
        }

        binding.clickNylon = ClickListener {
            userPopUp(
                requireContext(),
                nylonImage,
                getString(R.string.nylon_title),
                getString(R.string.nylon_twitter),
                getString(R.string.nylon_github),
                Paypal(getString(R.string.nylon_paypal))
            )
        }

        val users = ArrayList<User>()

        users.add(
            User.gen(
                requireContext(),
                R.string.akos_title,
                R.string.akos_summary,
                R.string.akos_image,
                R.string.akos_github,
                R.string.akos_paypal,
                R.string.akos_twitter
            )
        )
        users.add(
            User.gen(
                requireContext(),
                R.string.pandan_title,
                R.string.pandan_summary,
                R.string.pandan_image,
                R.string.pandan_github,
                R.string.pandan_paypal,
                R.string.pandan_twitter
            )
        )

        binding.users.layoutManager = LinearLayoutManager(requireContext())
        binding.users.adapter = Adapter(requireActivity(), users)
        binding.users.setHasFixedSize(true)

        Picasso.get().load(typImage).resizeDimen(R.dimen.imageSize, R.dimen.imageSize)
            .placeholder(R.drawable.ic_person)
            .into(v.findViewById<ImageView>(R.id.typ_image))
        Picasso.get().load(rkImage).resizeDimen(R.dimen.imageSize, R.dimen.imageSize)
            .placeholder(R.drawable.ic_person)
            .into(v.findViewById<ImageView>(R.id.rk_image))
        Picasso.get().load(nylonImage).resizeDimen(R.dimen.imageSize, R.dimen.imageSize)
            .placeholder(R.drawable.ic_person)
            .into(v.findViewById<ImageView>(R.id.nylon_image))

        return v
    }

    private class Adapter(val activity: FragmentActivity, val users: List<User>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.userName)
            val summary: TextView = v.findViewById(R.id.userSummary)
            val image: ImageView = v.findViewById(R.id.userPicture)
            val root: LinearLayout = v.findViewById(R.id.root)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(activity).inflate(R.layout.user_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = users[position]

            holder.name.text = user.name
            holder.summary.text = user.summary

            Picasso.get().load(user.image)
                .resizeDimen(R.dimen.imageSize, R.dimen.imageSize).placeholder(R.drawable.ic_person)
                .into(holder.image)

            holder.root.setOnClickListener {
                userPopUp(
                    activity,
                    user.image,
                    user.name,
                    user.twitter,
                    user.github,
                    Paypal(user.paypal)
                )
            }
        }

        override fun getItemCount(): Int = users.size
    }

    private data class User(
        val name: String,
        val summary: String,
        val image: String,
        val github: String,
        val paypal: String,
        val twitter: String
    ) {
        companion object {
            fun gen(
                context: Context,
                name: Int,
                summary: Int,
                image: Int,
                github: Int,
                paypal: Int,
                twitter: Int
            ): User {
                return User(
                    context.getString(name),
                    context.getString(summary),
                    context.getString(image),
                    context.getString(github),
                    context.getString(paypal),
                    context.getString(twitter)
                )
            }
        }
    }

    companion object {
        private fun userPopUp(
            context: Context,
            imageUrl: String,
            name: String,
            twitter: String,
            github: String,
            paypal: Paypal
        ) {
            MaterialDialog(context).show {
                setContentView(R.layout.user_popup)
                Picasso.get().load(imageUrl).fit().placeholder(R.drawable.ic_person)
                    .into(findViewById<MaskedImageView>(R.id.userProfileImage))
                findViewById<TextView>(R.id.userName).text = name
                findViewById<LinearLayout>(R.id.clickTwitter).setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(twitter))
                    context.startActivity(browserIntent)
                    dismiss()
                }
                findViewById<LinearLayout>(R.id.clickGithub).setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(github))
                    context.startActivity(browserIntent)
                    dismiss()
                }
                findViewById<LinearLayout>(R.id.clickPaypal).setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paypal.me))
                    context.startActivity(browserIntent)
                    dismiss()
                }
            }
        }
    }
}
