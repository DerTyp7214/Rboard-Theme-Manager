package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel

class WelcomeFragment : Fragment() {

    private lateinit var introViewModel: IntroViewModel
    private lateinit var ac: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ac = requireActivity()

        introViewModel = ac.run {
            ViewModelProviders.of(this)[IntroViewModel::class.java]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_welcome, container, false)

        val text: TextView = v.findViewById(R.id.textView)

        if (introViewModel.open.value == true) text.apply {
            if (layoutParams != null) layoutParams.height = WRAP_CONTENT
            else layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            requestLayout()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            ChangeBounds().apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                TransitionManager.beginDelayedTransition(text.parent as ViewGroup, this)
            }

            text.apply {
                if (layoutParams != null) layoutParams.height = WRAP_CONTENT
                else layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                requestLayout()
                introViewModel.open.postValue(true)
            }
        }, 500)

        return v
    }
}
