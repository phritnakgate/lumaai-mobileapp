package org.bkkz.lumaapp.presentation.main.splash

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.presentation.auth.LandingActivity
import org.bkkz.lumaapp.presentation.main.home.HomeActivity
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val tokenManager: TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        if(!isNetworkAvailable()){
            OneActionDialog(this).show(
                drawable = R.drawable.ic_dialog_no,
                title = getString(R.string.no_internet_title),
                message = getString(R.string.no_internet_desc),
                onConfirmClickListener = {
                    finish()
                }
            )
        }else{
            Handler(Looper.getMainLooper()).postDelayed({
                val token = tokenManager.getAccessToken()
                if (token.isNullOrEmpty() || tokenManager.isRefreshTokenExpired()) {
                    tokenManager.clearTokens()
                    startActivity(Intent(this@SplashActivity, LandingActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                }
                finish()
            }, 1500)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}