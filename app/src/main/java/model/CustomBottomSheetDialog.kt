package model

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.shoesapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog

object CustomBottomSheetDialog {
    fun show(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "OK",
        negativeText: String = "Cancel",
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.popup, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = title
        tvMessage.text = message
        btnConfirm.text = positiveText
        btnCancel.text = negativeText

        btnConfirm.setOnClickListener {
            onConfirm?.invoke()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
}