package org.bkkz.lumaapp.util.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import org.bkkz.lumaapp.R

class LoadingDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }
}