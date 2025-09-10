package org.bkkz.lumaapp.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.login.state.LoginState
import org.bkkz.lumaapp.presentation.main.HomeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel: LoginViewModel by viewModel()

    //UI
    private lateinit var edtEmail : EditText
    private lateinit var edtPassword : EditText
    private lateinit var emailSignInBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        findViews()
        setupViews()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findViews(){
        edtEmail = findViewById(R.id.edttxt_login_email)
        edtPassword = findViewById(R.id.edttxt_login_password)
        emailSignInBtn = findViewById(R.id.compatbtn_login_login)
    }
    private fun setupViews(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.state.collect { state ->
                    when(state) {
                        is LoginState.Loading -> {
                           //TODO: Decorate loading state
                            emailSignInBtn.isEnabled = false
                            Toast.makeText(this@LoginActivity, "Logging in...", Toast.LENGTH_SHORT).show()
                        }
                        is LoginState.Idle -> {
                            emailSignInBtn.isEnabled = true
                        }
                        is LoginState.Error -> {
                            //TODO: Add popup
                            emailSignInBtn.isEnabled = true
                            Toast.makeText(this@LoginActivity, "Invalid Email or Password!", Toast.LENGTH_SHORT).show()

                        }
                        is LoginState.Success -> {
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
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
        setupEmailSignInBtn()
    }
    private fun setupEmailSignInBtn(){
        emailSignInBtn.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            if(email.isBlank() || email.isEmpty()){
                edtEmail.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.danger))
                return@setOnClickListener
            }
            if(password.isBlank() || password.isEmpty()){
                edtPassword.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.danger))
                return@setOnClickListener
            }
            viewModel.loginWithEmail(email, password)
        }
    }
}