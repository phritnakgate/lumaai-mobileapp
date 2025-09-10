package org.bkkz.lumaapp.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.remote.AuthRepository
import org.bkkz.lumaapp.presentation.auth.LandingActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val logoutbtn: AppCompatButton = findViewById(R.id.compatbtn_home_logout)

        logoutbtn.setOnClickListener {
            lifecycleScope.launch {
                AuthRepository(TokenManager(applicationContext)).logout()
                val intent = Intent(this@HomeActivity, LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}