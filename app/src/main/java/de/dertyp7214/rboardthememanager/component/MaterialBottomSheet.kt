package de.dertyp7214.rboardthememanager.component

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.dertyp7214.preferencesplus.core.setMargins
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class MaterialBottomSheet : RoundedBottomSheetDialogFragment() {

    private var func: (MaterialBottomSheet.() -> Unit)? = null
    private var resourceId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(resourceId!!, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        func?.invoke(this)
    }

    fun show(
        fragmentManager: FragmentManager,
        resourceId: Int,
        func: MaterialBottomSheet.() -> Unit
    ): MaterialBottomSheet {
        this.func = func
        this.resourceId = resourceId
        show(fragmentManager, "")
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
            bottomSheet.setMargins(0, 120, 0, 0)
            BottomSheetBehavior.from(bottomSheet).isHideable = true
        }
        return bottomSheetDialog
    }

    fun <T : View?> findViewById(id: Int): T? {
        return view?.findViewById(id)
    }

    fun setOnCancelListener(runnable: DialogInterface.OnCancelListener) =
        dialog?.setOnCancelListener(
            runnable
        )
}