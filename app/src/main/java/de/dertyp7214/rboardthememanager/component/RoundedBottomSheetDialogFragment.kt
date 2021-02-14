package de.dertyp7214.rboardthememanager.component

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.parent

open class RoundedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.parent()
            ?.setBackgroundColor(requireActivity().getColor(R.color.bottomSheetDimBackground))
    }
}