package org.bkkz.lumaapp.presentation.main.setting

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.BuildConfig
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.setting.state.SettingsEvent
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel: SettingsViewModel by viewModel()

    //UI
    private lateinit var backBtn: ImageView
    private lateinit var connectAccountBtn: TextView
    private lateinit var disconnectAccountBtn: ImageView
    private lateinit var txtEmail: TextView
    private lateinit var loadingDialog: LoadingDialog

    private val requestCalendarPermissionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val authorizationResult = Identity.getAuthorizationClient(this@SettingsActivity)
                .getAuthorizationResultFromIntent(result.data)

            val authCode = authorizationResult.serverAuthCode

            if (!authCode.isNullOrEmpty()) {
                viewModel.saveCalendarRefreshToken(authCode, "")
            } else {
                Log.e("SettingsActivity", "Google Calendar authorization failed: authCode(authCode=$authCode)")
            }

        } catch (e: ApiException) {
            Log.e("SettingsActivity", "Google Calendar authorization failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        findViews()
        setupViews()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findViews() {
        backBtn = findViewById(R.id.imgview_settings_back)
        connectAccountBtn = findViewById(R.id.txtview_settings_google_calendar_connect)
        disconnectAccountBtn = findViewById(R.id.imgview_settings_google_calendar_disconnect)
        txtEmail = findViewById(R.id.txtview_settings_google_calendar_email)
        loadingDialog = LoadingDialog(this@SettingsActivity)
    }

    private fun setupViews() {
        viewModel.onEvent(SettingsEvent.OnLoadServiceStatus)
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                Log.d("SettingsActivity", "State: ${state.serviceState}, isConnected: ${state.isConnectedToCalendar}, isLoginViaGoogle: ${state.isLoginViaGoogle}, email: ${state.googleCalendarEmail}")
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                when (state.serviceState) {
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> {
                        loadingDialog.show()
                    }
                    ServiceState.SUCCESS -> {
                        loadingDialog.dismiss()
                        if (!state.isConnectedToCalendar && !state.isLoginViaGoogle) {
                            connectAccountBtn.visibility = View.VISIBLE
                            txtEmail.visibility = View.GONE
                            disconnectAccountBtn.visibility = View.GONE
                        } else {
                            connectAccountBtn.visibility = View.GONE
                            txtEmail.visibility = View.VISIBLE
                            txtEmail.text = "Signed in as ${state.googleCalendarEmail}"
                            if (state.isLoginViaGoogle) {
                                disconnectAccountBtn.visibility = View.GONE
                            } else {
                                disconnectAccountBtn.visibility = View.VISIBLE
                            }
                        }
                    }
                    ServiceState.FAILED -> {
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun setupEvents() {
        backBtn.setOnClickListener { finish() }
        disconnectAccountBtn.setOnClickListener {
            viewModel.revokeCalendarConnection()
        }
        connectAccountBtn.setOnClickListener {
            requestCalendarPermission()
        }
    }

    private fun requestCalendarPermission() {
        val authorizationRequest = AuthorizationRequest.builder()
            .requestOfflineAccess(BuildConfig.FIREBASE_WEB_CLIENT_ID)
            .setRequestedScopes(
                mutableListOf(
                    Scope(CalendarScopes.CALENDAR_READONLY),
                    Scope(CalendarScopes.CALENDAR),
                    Scope("email"),
                    Scope("profile")
                )
            )
            .build()

        lifecycleScope.launch {
            try {
                Identity.getAuthorizationClient(this@SettingsActivity)
                    .authorize(authorizationRequest)
                    .addOnSuccessListener { result ->
                        if (result.pendingIntent != null) {
                            requestCalendarPermissionForResult.launch(IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build())
                        }else{
                            Log.e("SettingsActivity", "Google Calendar authorization failed: pendingIntent is null")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsActivity", "Google Calendar authorization failed", e)
                    }

            } catch (e: Exception) {
                Log.e("SettingsActivity", "Google Calendar authorization failed", e)
            }

        }

    }
}