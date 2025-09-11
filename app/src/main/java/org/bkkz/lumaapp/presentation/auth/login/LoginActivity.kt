package org.bkkz.lumaapp.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.BuildConfig
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.login.state.LoginState
import org.bkkz.lumaapp.presentation.auth.register.RegisterActivity
import org.bkkz.lumaapp.presentation.main.HomeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel: LoginViewModel by viewModel()

    //UI
    private lateinit var edtEmail : EditText
    private lateinit var edtPassword : EditText
    private lateinit var emailSignInBtn: AppCompatButton
    private lateinit var googleSignInBtn : ConstraintLayout
    private lateinit var txtSignUp : TextView

    //Google Auth
    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager : CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        findViews()
        setupViews()
        setupEvents()
    }

    private fun findViews(){
        edtEmail = findViewById(R.id.edttxt_login_email)
        edtPassword = findViewById(R.id.edttxt_login_password)
        emailSignInBtn = findViewById(R.id.compatbtn_login_login)
        googleSignInBtn = findViewById(R.id.constraintlayout_login_google_button)
        txtSignUp = findViewById(R.id.txtview_login_register)
    }
    private fun setupViews(){

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this@LoginActivity)

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
        setupGoogleSignInBtn()
        setupSignUpBtn()
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

    private fun setupGoogleSignInBtn(){
        googleSignInBtn.setOnClickListener {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID) // local.properties
                .build()
            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        context = this@LoginActivity,
                        request = request
                    )
                    handleGoogleSignIn(result)
                } catch (e: GetCredentialException) {
                    // Handle failure
                }
            }
        }
    }

    private fun handleGoogleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        Log.d("LoginActivity", "Received google id : ${googleIdTokenCredential.idToken}")

                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val currentUser = auth.currentUser
                                    Log.d("LoginActivity", "signInWithCredential:$currentUser")
                                    Log.d("LoginActivity", "User: ${currentUser?.displayName}")
                                    Log.d("LoginActivity", "Email: ${currentUser?.email}")
                                    Log.d("LoginActivity", "UID: ${currentUser?.uid}")

                                    currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            val firebaseIdToken = tokenTask.result?.token
                                            Log.d("LoginActivity", "Got Firebase ID Token: $firebaseIdToken")
                                            if(firebaseIdToken != null){
                                                viewModel.loginWithGoogle(firebaseIdToken)
                                            }
                                        }
                                    }
                                } else {
                                    // Handle Firebase sign-in failure
                                    Log.w("LoginActivity", "Firebase sign-in failed", task.exception)
                                }
                            }



                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("LoginActivity", "Received an invalid google id token response", e)
                    }
                }
                else {
                    // Catch any unrecognized credential type here.
                    Log.e("LoginActivity", "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e("MainActivity", "Unexpected type of credential")
            }
        }
    }

    private fun setupSignUpBtn(){
        txtSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}