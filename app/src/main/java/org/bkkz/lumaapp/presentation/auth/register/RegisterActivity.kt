package org.bkkz.lumaapp.presentation.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.HomeActivity

class RegisterActivity : AppCompatActivity() {

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
        btnSignUp = findViewById(R.id.compatbtn_register_register)
        txtViewHaveAccount = findViewById(R.id.txtview_register_have_account)
    }
    private fun setupViews(){}
    private fun setupEvents(){
        btnSignUp.setOnClickListener {
            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        txtViewHaveAccount.setOnClickListener {
            finish()
        }
    }
}