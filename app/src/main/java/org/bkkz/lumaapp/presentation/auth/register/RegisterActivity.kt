package org.bkkz.lumaapp.presentation.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.register.state.RegisterState
import org.bkkz.lumaapp.presentation.main.home.HomeActivity
import org.bkkz.lumaapp.util.LabelEditText
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel : RegisterViewModel by viewModel()

    //UI
    private lateinit var lblEmail : LabelEditText
    private lateinit var lblPassword : LabelEditText
    private lateinit var lblCfPassword : LabelEditText
    private lateinit var lblName : LabelEditText
    private lateinit var btnSignUp: AppCompatButton
    private lateinit var txtViewHaveAccount : TextView
    private lateinit var loadingDialog : LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        findViews()
        setupViews()
        setupEvents()

    }

    private fun findViews(){
        lblEmail = findViewById(R.id.lbledt_register_email)
        lblPassword = findViewById(R.id.lbledt_register_password)
        lblCfPassword = findViewById(R.id.lbledt_register_confirm_password)
        lblName = findViewById(R.id.lbledt_register_name)
        btnSignUp = findViewById(R.id.compatbtn_register_register)
        txtViewHaveAccount = findViewById(R.id.txtview_register_have_account)
        loadingDialog = LoadingDialog(this@RegisterActivity)
    }
    private fun setupViews(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.state.collect { state ->
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    when(state) {
                        is RegisterState.Loading -> {
                            loadingDialog.show()
                            btnSignUp.isEnabled = false
                        }
                        is RegisterState.Idle -> {
                            btnSignUp.isEnabled = true
                        }
                        is RegisterState.Error -> {
                            btnSignUp.isEnabled = true
                            OneActionDialog(this@RegisterActivity).show(
                                drawable = R.drawable.ic_dialog_no,
                                title = getString(R.string.register_failed_dialog_title),
                                message = state.message,
                            )

                        }
                        is RegisterState.Success -> {
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
    private fun setupEvents(){
        setupSignUpBtnEvent()
        setupEditTextEvents()
        txtViewHaveAccount.setOnClickListener {
            finish()
        }
    }

    private fun setupEditTextEvents(){
        lblEmail.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank() || !Patterns.EMAIL_ADDRESS.matcher(text).matches()){
                lblEmail.setError(true)
            }else{
                lblEmail.setError(false)
            }
        }
        lblPassword.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank()){
                lblPassword.setError(true)
            }else{
                lblPassword.setError(false)
            }
        }
        lblCfPassword.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank() || text.toString() != lblPassword.text){
                lblCfPassword.setError(true)
            }else{
                lblCfPassword.setError(false)
            }
        }
        lblName.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank()){
                lblName.setError(true)
            }else{
                lblName.setError(false)
            }
        }
    }

    private fun setupSignUpBtnEvent(){
        btnSignUp.setOnClickListener {
            var errorFlag = false
            if(lblEmail.text.isBlank() || lblEmail.text.isEmpty() ){
                lblEmail.setError(true)
                errorFlag = true
            }
            if(lblPassword.text.isBlank() || lblPassword.text.isEmpty() ){
                lblPassword.setError(true)
                errorFlag = true
            }
            if(lblCfPassword.text.isBlank() || lblCfPassword.text.isEmpty() || lblCfPassword.text != lblPassword.text){
                lblCfPassword.setError(true)
                errorFlag = true
            }
            if(lblName.text.isBlank() || lblName.text.isEmpty() ){
                lblName.setError(true)
                errorFlag = true
            }
            if(!errorFlag){
                viewModel.register(lblEmail.text, lblPassword.text, lblName.text)
            }

        }
    }
}