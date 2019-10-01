package de.dertyp7214.rboardthememanager.core

import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

fun View.setMargin(
    leftMargin: Int? = null, topMargin: Int? = null,
    rightMargin: Int? = null, bottomMargin: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(
            leftMargin ?: params.leftMargin,
            topMargin ?: params.topMargin,
            rightMargin ?: params.rightMargin,
            bottomMargin ?: params.bottomMargin
        )
        layoutParams = params
    }
}

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
    val initialPadding = recordInitialPaddingForView(this)

    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding)
        insets
    }

    requestApplyInsetsWhenAttached()
}

data class InitialPadding(
    val left: Int, val top: Int,
    val right: Int, val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}