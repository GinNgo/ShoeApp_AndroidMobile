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
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import service.serviceImplement.UserService
import ui.admin.AdminActivity
import ui.home.HomeActivity
import utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private val userService = UserService()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // 🔹 Kiểm tra nếu đã có session còn hạn thì tự động đăng nhập
        val session = sessionManager.getUserSession()
        if (session != null) {
            val (email, role) = session
            if (role == 1) {
                startActivity(Intent(this, AdminActivity::class.java).putExtra("email", email))
            } else {
                startActivity(Intent(this, HomeActivity::class.java).putExtra("email", email))
            }
            finish()
            return
        }

        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this@LoginActivity, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = userService.getUserByEmail(email)

                if (user != null && user.passwordHash == password) {
                    // ✅ Lưu session
                    sessionManager.saveUserSession(user.email, user.role)

                    // ✅ Chuyển theo role
                    if (user.role == 1) {
                        startActivity(Intent(this@LoginActivity, AdminActivity::class.java).putExtra("email", user.email))
                    } else {
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java).putExtra("email", user.email))
                    }
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Email hoặc password không đúng", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
