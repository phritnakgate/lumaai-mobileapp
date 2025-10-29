package org.bkkz.lumaapp.presentation.main.setting

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
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
import org.bkkz.lumaapp.util.dialog.OneActionDialog
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
    private lateinit var constraintLanguage: ConstraintLayout
    private lateinit var loadingDialog: LoadingDialog

    private val requestCalendarPermissionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val authorizationResult = Identity.getAuthorizationClient(this@SettingsActivity)
                .getAuthorizationResultFromIntent(result.data)

            val authCode = authorizationResult.serverAuthCode

            if (!authCode.isNullOrEmpty()) {
                viewModel.saveCalendarRefreshToken(this@SettingsActivity, authCode, "")
            } else {
                Log.e("SettingsActivity", "Google Calendar authorization failed: authCode(authCode=$authCode)")
            }

        } catch (e: ApiException) {
            OneActionDialog(this@SettingsActivity).show(
                drawable = R.drawable.ic_dialog_no,
                title = getString(R.string.login_ggc_failed_dialog_title),
                message = getString(R.string.login_ggc_failed_dialog_desc),
            )
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
        constraintLanguage = findViewById(R.id.constraintlayout_settings_language)
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
                        //loadingDialog.show()
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
                            txtEmail.text = getString(R.string.setting_google_calendar_email, state.googleCalendarEmail)
                            val sharedPref = getSharedPreferences("userSession", MODE_PRIVATE)
                            sharedPref.edit {
                                putString("googleCalendarEmail", state.googleCalendarEmail)
                                apply()
                            }
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
            viewModel.revokeCalendarConnection(this@SettingsActivity)
        }
        connectAccountBtn.setOnClickListener {
            viewModel.clearCredentials(this@SettingsActivity)
            requestCalendarPermission()
        }
        constraintLanguage.setOnClickListener {
            selectLanguageDialog()
        }
    }

    private fun createAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.builder()
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
    }

    private fun requestCalendarPermission() {

        lifecycleScope.launch {
            try {
                val authorizationRequest = createAuthorizationRequest()
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

    private fun selectLanguageDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_language, null)
        val langSelector = dialogView.findViewById<RadioGroup>(R.id.radiogroup_dialog_change_language_options)
        val radioEn = dialogView.findViewById<RadioButton>(R.id.radiobtn_dialog_change_language_en)
        val radioTh = dialogView.findViewById<RadioButton>(R.id.radiobtn_dialog_change_language_th)
        when(viewModel.state.value.currentLanguage){
            "en-US" -> {
                radioEn.background = AppCompatResources.getDrawable(this, R.drawable.lang_radio_selected)
                langSelector.check(R.id.radiobtn_dialog_change_language_en)
            }
            "th" -> {
                radioTh.background = AppCompatResources.getDrawable(this, R.drawable.lang_radio_selected)
                langSelector.check(R.id.radiobtn_dialog_change_language_th)
            }
            else -> {
                radioEn.background = AppCompatResources.getDrawable(this, R.drawable.lang_radio_selected)
                langSelector.check(R.id.radiobtn_dialog_change_language_en)
            }
        }
        radioEn.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                viewModel.changeAppLanguage("en-US")
                radioTh.background = AppCompatResources.getDrawable(this, R.drawable.edit_text_bg)
                radioEn.background = AppCompatResources.getDrawable(this, R.drawable.lang_radio_selected)
            }
        }

        radioTh.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                viewModel.changeAppLanguage("th")
                radioTh.background = AppCompatResources.getDrawable(this, R.drawable.lang_radio_selected)
                radioEn.background = AppCompatResources.getDrawable(this, R.drawable.edit_text_bg)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_primary)) { dialog, _ ->
                val newLocaleList = LocaleListCompat.forLanguageTags(viewModel.state.value.currentLanguage)
                AppCompatDelegate.setApplicationLocales(newLocaleList)
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setTextAppearance(R.style.LumaAI_TextAppearance_BodyMedium)
            positiveBtn.setTextColor(getColor(R.color.primary))
        }

        dialog.show()
    }

}