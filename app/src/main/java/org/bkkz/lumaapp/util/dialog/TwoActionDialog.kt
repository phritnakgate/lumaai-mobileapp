package org.bkkz.lumaapp.util.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import org.bkkz.lumaapp.R

class TwoActionDialog(
    context: Context,
    ) : Dialog(context){
    private lateinit var imgViewIcon : ImageView
    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var confirmButton: AppCompatButton
    private lateinit var abortButton : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_two_choice)

        imgViewIcon = findViewById(R.id.imgview_dialog_two_action)
        titleTextView = findViewById(R.id.txtview_dialog_two_action_title)
        messageTextView = findViewById(R.id.txtview_dialog_two_action_desc)
        confirmButton = findViewById(R.id.compatbtn_dialog_two_action_left)
        abortButton = findViewById(R.id.compatbtn_dialog_two_action_right)

        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    fun show(
        drawable: Int,
        title: String,
        message: String,
        onConfirmClickListener: (() -> Unit)? = null,
        onAbortClickListener: (() -> Unit)? = null
    ) {
        super.show()
        imgViewIcon.setImageResource(drawable)
        titleTextView.text = title
        if(message.isEmpty()){
            messageTextView.visibility = android.view.View.GONE
        } else {
            messageTextView.visibility = android.view.View.VISIBLE
            messageTextView.text = message
        }
        confirmButton.setOnClickListener {
            onConfirmClickListener?.invoke()
            dismiss()
        }
        abortButton.setOnClickListener {
            onAbortClickListener?.invoke()
            dismiss()
        }

    }
}