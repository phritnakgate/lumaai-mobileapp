package org.bkkz.lumaapp.presentation.auth.register

import android.content.Intent
import android.os.Bundle
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
    }
    private fun setupViews(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.state.collect { state ->
                    when(state) {
                        is RegisterState.Loading -> {
                            LoadingDialog(this@RegisterActivity).show()
                            btnSignUp.isEnabled = false
                            Toast.makeText(this@RegisterActivity, "Registering...", Toast.LENGTH_SHORT).show()
                        }
                        is RegisterState.Idle -> {
                            btnSignUp.isEnabled = true
                        }
                        is RegisterState.Error -> {
                            //TODO: Add popup
                            btnSignUp.isEnabled = true
                            Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()

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

        btnSignUp.setOnClickListener {

            if(lblEmail.text.isBlank() || lblEmail.text.isEmpty() ){
                lblEmail.setError(true)
                return@setOnClickListener
            }
            if(lblPassword.text.isBlank() || lblPassword.text.isEmpty() ){
                lblPassword.setError(true)
                return@setOnClickListener
            }
            if(lblCfPassword.text.isBlank() || lblCfPassword.text.isEmpty() || lblCfPassword.text != lblPassword.text){
                lblPassword.setError(true)
                return@setOnClickListener
            }
            if(lblName.text.isBlank() || lblName.text.isEmpty() ){
                lblName.setError(true)
                return@setOnClickListener
            }
            viewModel.register(lblEmail.text, lblPassword.text, lblName.text)
        }
        txtViewHaveAccount.setOnClickListener {
            finish()
        }
    }
}