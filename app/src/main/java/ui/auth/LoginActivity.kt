package ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shoesapp.R
import ui.home.HomeActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import model.User
import service.UserService

class LoginActivity : AppCompatActivity() {
    private val userService = UserService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        // Xử lý khi nhấn vào "Đăng ký ngay"
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        var tvSignIn = findViewById<Button>(R.id.btnLogin)
        tvSignIn.setOnClickListener {
            lifecycleScope.launch {
                val etEmail = findViewById<EditText>(R.id.etEmail)
                val etPassword = findViewById<EditText>(R.id.etPassword)

                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                val userValid = userService.getUserByEmail(email)
                if(userValid != null && userValid.passwordHash == password){
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this@LoginActivity, "Email hoặc password không đúng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Căn lề cho status/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}
