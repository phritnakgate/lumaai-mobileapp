package org.bkkz.lumaapp.presentation.main.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.presentation.auth.LandingActivity
import org.bkkz.lumaapp.presentation.main.home.HomeActivity
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val tokenManager: TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val token = tokenManager.getAccessToken()

            if (token.isNullOrEmpty()) {
                startActivity(Intent(this@SplashActivity, LandingActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            }
            finish()
        }, 1500)

    }
}