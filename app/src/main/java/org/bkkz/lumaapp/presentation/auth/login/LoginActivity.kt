package org.bkkz.lumaapp.presentation.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.BuildConfig
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.forget.ForgetPasswordActivity
import org.bkkz.lumaapp.presentation.auth.login.state.LoginEvent
import org.bkkz.lumaapp.presentation.auth.register.RegisterActivity
import org.bkkz.lumaapp.presentation.main.home.HomeActivity
import org.bkkz.lumaapp.util.LabelEditText
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.isConnectedToInternet
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel: LoginViewModel by viewModel()

    //UI
    private lateinit var edtEmail : LabelEditText
    private lateinit var edtPassword : LabelEditText
    private lateinit var emailSignInBtn: AppCompatButton
    private lateinit var googleSignInBtn : ConstraintLayout
    private lateinit var txtSignUp : TextView
    private lateinit var txtForgetPassword : TextView
    private lateinit var loadingDialog: LoadingDialog

    //Google Auth
    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager : CredentialManager

    var flag = ""

    private val requestCalendarPermissionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ){ result ->
        try {
            val authorizationResult = Identity.getAuthorizationClient(this@LoginActivity)
                .getAuthorizationResultFromIntent(result.data)
            val authCode = authorizationResult.serverAuthCode
            val sharedPref = this@LoginActivity.getSharedPreferences("userSession", MODE_PRIVATE)
            val email = sharedPref.getString("googleCalendarEmail", null)
            if(authCode != null){
                viewModel.saveCalendarRefreshToken(authCode, email!!)
                Log.d("LoginActivity", "Google Calendar authorization success: $authCode with email $email")
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }else{
                Log.e("LoginActivity", "Google Calendar authorization failed: authCode is null")

            }

        } catch (e : ApiException) {
            OneActionDialog(this@LoginActivity).show(
                drawable = R.drawable.ic_dialog_no,
                title = getString(R.string.login_ggc_failed_dialog_title),
                message = getString(R.string.login_ggc_failed_dialog_desc),
                onConfirmClickListener = {
                    viewModel.googleCalendarNotAllowHandler()
                }
            )

            Log.e("LoginActivity", "Google Calendar authorization failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        findViews()
        setupViews()
        setupEvents()
    }

    private fun findViews(){
        edtEmail = findViewById(R.id.lbledt_login_email)
        edtPassword = findViewById(R.id.lbledt_login_password)
        emailSignInBtn = findViewById(R.id.compatbtn_login_login)
        googleSignInBtn = findViewById(R.id.constraintlayout_login_google_button)
        txtSignUp = findViewById(R.id.txtview_login_register)
        txtForgetPassword = findViewById(R.id.txtview_login_forget)
        loadingDialog = LoadingDialog(this@LoginActivity)
    }
    private fun setupViews(){

        auth = Firebase.auth
        credentialManager = CredentialManager.create(this@LoginActivity)
        val sharedPref = this@LoginActivity.getSharedPreferences("userSession", MODE_PRIVATE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.state.collect { state ->
                    if (loadingDialog.isShowing) {
                        loadingDialog.dismiss()
                    }
                    when(state) {
                        is LoginEvent.Loading -> {
                            loadingDialog.show()
                            emailSignInBtn.isEnabled = false
                        }
                        is LoginEvent.Idle -> {
                            emailSignInBtn.isEnabled = true
                        }
                        is LoginEvent.Error -> {
                            emailSignInBtn.isEnabled = true
                            OneActionDialog(this@LoginActivity).show(
                                drawable = R.drawable.ic_dialog_no,
                                title = getString(R.string.login_failed_dialog_title),
                                message = state.message,
                                onConfirmClickListener = { viewModel.setIdleState() }
                            )

                        }
                        is LoginEvent.Success -> {
                            if(flag == "email") {
                                sharedPref.edit().apply{
                                    putString("email", state.email)
                                    putString("googleCalendarEmail", state.googleCalendarEmail)
                                    apply()
                                }
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }else{
                                sharedPref.edit().apply{
                                    putString("email", state.email)
                                    putString("googleCalendarEmail", state.email)
                                    apply()
                                }
                            }
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
        setupEdtEmail()
        setupEdtPassword()
        txtForgetPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
        }
    }

    private fun setupEmailSignInBtn(){
        emailSignInBtn.setOnClickListener {
            flag = "email"
            val email = edtEmail.text
            val password = edtPassword.text
            var errorFlag = false

            if(email.isBlank() || email.isEmpty()){
                edtEmail.setError(true)
                errorFlag = true
            }
            if(password.isBlank() || password.isEmpty()){
                edtPassword.setError(true)
                errorFlag = true
            }
            if(!errorFlag){
                viewModel.loginWithEmail(email, password)
            }

        }
    }

    private fun setupGoogleSignInBtn(){
        googleSignInBtn.setOnClickListener {
            flag = "google"
            if(!this.isConnectedToInternet()) {
                OneActionDialog(this@LoginActivity).show(
                    drawable = R.drawable.ic_dialog_no,
                    title = getString(R.string.no_internet_title),
                    message = getString(R.string.no_internet_desc),
                )
                return@setOnClickListener
            }
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
                                                viewModel.loginWithGoogle(currentUser.email!!, firebaseIdToken)
                                                requestCalendarPermission()
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
            viewModel.setIdleState()
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupEdtEmail(){
        edtEmail.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank() || !Patterns.EMAIL_ADDRESS.matcher(text).matches()){
                edtEmail.setError(true)
            }else{
                edtEmail.setError(false)
            }
        }
    }
    private fun setupEdtPassword(){
        edtPassword.onTextChanged { text, start, before, count ->
            if(text.isNullOrBlank()){
                edtPassword.setError(true)
            }else{
                edtPassword.setError(false)
            }
        }
    }


    private fun requestCalendarPermission() {
        val authorizationRequest = AuthorizationRequest.builder()
            .requestOfflineAccess(BuildConfig.FIREBASE_WEB_CLIENT_ID)
            .setRequestedScopes(mutableListOf(Scope(CalendarScopes.CALENDAR_READONLY), Scope(CalendarScopes.CALENDAR)))
            .build()

        lifecycleScope.launch {
            try{
                Identity.getAuthorizationClient(this@LoginActivity)
                    .authorize(authorizationRequest)
                    .addOnSuccessListener { result ->
                        if(result.pendingIntent == null){
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }else{
                            requestCalendarPermissionForResult.launch(IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build())
                        }

                    }
                    .addOnFailureListener { e ->
                        Log.e("LoginActivity", "Google Calendar authorization failed", e)
                    }


            }catch (e: Exception){
                Log.e("LoginActivity", "Google Calendar authorization failed", e)
            }

        }

    }

}