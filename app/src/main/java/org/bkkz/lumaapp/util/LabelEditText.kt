package org.bkkz.lumaapp.util

import android.content.Context
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.bkkz.lumaapp.R
import androidx.core.content.withStyledAttributes

class LabelEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr){

    private val txtViewLabel : TextView
    private val editText : EditText
    private val btnShowPassword : ImageView

    private var isError = false
    private var isPasswordVisible = false
    private val STATE_ERROR = intArrayOf(R.attr.isError)

    var text: String
        get() = editText.text.toString()
        set(value) {
            editText.setText(value)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.label_edit_text, this, true)
        txtViewLabel = findViewById(R.id.txtview_label_edit_text)
        editText = findViewById(R.id.edttxt_label_edit_text)
        btnShowPassword = findViewById(R.id.imgview_label_edit_text)

        //Read value from XML
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.LabelEditText, 0, 0) {

                val label = getString(R.styleable.LabelEditText_labelText) ?: ""
                val hint = getString(R.styleable.LabelEditText_hintText) ?: ""
                val isRequired = getBoolean(R.styleable.LabelEditText_isRequired, false)
                val isPassword = getBoolean(R.styleable.LabelEditText_isPasswordField, false)

                setLabelText(label, isRequired)
                setHintText(hint)
                if (isPassword) {
                    setPasswordInput()
                }
            }
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isError) {
            mergeDrawableStates(drawableState, STATE_ERROR)
        }
        return drawableState
    }

    private fun setLabelText(text: String, required: Boolean) {
        if(required){
            val builder = SpannableStringBuilder()
            builder.append(text)
            builder.append("*")
            val redColor = ForegroundColorSpan(ContextCompat.getColor(context, R.color.danger))
            val startIndex = builder.length - 1
            val endIndex = builder.length
            builder.setSpan(redColor, startIndex, endIndex, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
            txtViewLabel.text = builder
        }else{
            txtViewLabel.text = text
        }
    }

    private fun setHintText(text: String) {
        editText.hint = text
    }

    fun setError(error: Boolean) {
        if (isError != error) {
            isError = error
            refreshDrawableState()
        }
    }

    private fun setPasswordInput(){
        btnShowPassword.visibility = VISIBLE // แสดงไอคอน
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        editText.transformationMethod = PasswordTransformationMethod.getInstance()
        isPasswordVisible = false
        btnShowPassword.setImageResource(R.drawable.ic_eye_hidden)

        btnShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // แสดงรหัสผ่าน
                editText.transformationMethod = null
                btnShowPassword.setImageResource(R.drawable.ic_eye)
            } else {
                // ซ่อนรหัสผ่าน
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                btnShowPassword.setImageResource(R.drawable.ic_eye_hidden)
            }
            editText.setSelection(editText.text.length)
        }
    }
}