package org.bkkz.lumaapp.presentation.auth.forget

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.forget.state.ForgetPasswordEvent
import org.bkkz.lumaapp.util.LabelEditText
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgetPasswordActivity : AppCompatActivity() {

    private val viewModel : ForgetPasswordViewModel by viewModel()

    private lateinit var edtEmail : LabelEditText
    private lateinit var btnReset : AppCompatButton
    private lateinit var btnBack : TextView
    private lateinit var loadingDialog: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        findView()
        setupView()
        setupEvent()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findView() {
        edtEmail = findViewById(R.id.lbledt_forget_password_email)
        btnReset = findViewById(R.id.compatbtn_forget_password)
        btnBack = findViewById(R.id.txtview_forget_password_back)
        loadingDialog = LoadingDialog(this@ForgetPasswordActivity)
    }
    private fun setupView() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if(state.isEmailValid == true || state.isEmailValid == null){
                    edtEmail.setError(false)
                }else{
                    edtEmail.setError(true)
                }
                if(loadingDialog.isShowing){
                    loadingDialog.dismiss()
                }
                when(state.serviceState){
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> {
                        loadingDialog.show()
                    }
                    ServiceState.SUCCESS -> {
                        OneActionDialog(this@ForgetPasswordActivity).show(
                            drawable = R.drawable.ic_dialog_success,
                            title = getString(R.string.forget_password_dialog_success_title),
                            message = "",
                            onConfirmClickListener = {
                                finish()
                            }
                        )
                    }
                    ServiceState.FAILED -> {
                        OneActionDialog(this@ForgetPasswordActivity).show(
                            drawable = R.drawable.ic_dialog_no,
                            title = getString(R.string.forget_password_dialog_failed_title),
                            message = state.serviceMessage ?: "",
                            onConfirmClickListener = {
                                viewModel.setIdleState()
                            }
                        )
                    }
                }
            }
        }

    }
    private fun setupEvent() {
        edtEmail.onTextChanged { text, start, before, count ->
            viewModel.onEvent(ForgetPasswordEvent.OnEmailChange(text.toString()))
        }
        btnReset.setOnClickListener {
            viewModel.onEvent(ForgetPasswordEvent.SubmitRequest)
        }
        btnBack.setOnClickListener {
            finish()
        }
    }
}