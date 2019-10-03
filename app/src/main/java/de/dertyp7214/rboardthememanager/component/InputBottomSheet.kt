package de.dertyp7214.rboardthememanager.component

import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputLayout
import de.dertyp7214.rboardthememanager.R

class InputBottomSheet(
    private val keyListener: View.OnKeyListener,
    private val onSubmit: (input: Editable?, bottomSheet: InputBottomSheet) -> Unit,
    private val onMenu: (input: View, bottomSheet: InputBottomSheet) -> Unit = { _, _ -> }
) :
    RoundedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.input_bottom_sheet, container, false)

        val input = v.findViewById<CustomTextInputEditText>(R.id.editText)
        val inputLayout = v.findViewById<TextInputLayout>(R.id.inputLayout)

        inputLayout.setStartIconOnClickListener { onSubmit(input.text, this) }
        inputLayout.setEndIconOnClickListener { onMenu(it, this) }

        input.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH)
        input.setOnKeyListener(keyListener)
        input.setOnEditorActionListener { _, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH
                || i == EditorInfo.IME_ACTION_DONE
                || (keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                onSubmit(input.text, this)
                true
            } else false
        }

        return v
    }
}