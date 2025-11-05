package ui.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
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

    // --- Khai báo Views ---
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRememberMe: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView

    // --- SharedPreferences ---
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "LoginPrefs"
    private val PREF_EMAIL = "REMEMBER_EMAIL"
    private val PREF_PASSWORD = "REMEMBER_PASSWORD"
    private val PREF_CHECKED = "REMEMBER_CHECKED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // 1. Kiểm tra session (Giữ nguyên)
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

        // ⭐️ (SỬA) Bước 2: Ánh xạ TẤT CẢ các view NGAY LẬP TỨC
        initViews()

        // ⭐️ (SỬA) Bước 3: Tải thông tin "Remember Me" (Bây giờ đã an toàn)
        loadRememberedCredentials()

        // ⭐️ (SỬA) Bước 4: Cài đặt Listeners
        setupListeners()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * ⭐️ (MỚI) Hàm ánh xạ view
     */
    private fun initViews() {
        tvSignUp = findViewById(R.id.tvSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
    }

    /**
     * ⭐️ (MỚI) Hàm cài đặt listener
     */
    private fun setupListeners() {
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

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
                    // Xử lý "Remember Me"
                    handleRememberMe(email, password, cbRememberMe.isChecked)

                    // Lưu session
                    sessionManager.saveUserSession(user.email, user.role)

                    // Chuyển Activity
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
    }

    private fun handleRememberMe(email: String, pass: String, isChecked: Boolean) {
        val editor = prefs.edit()
        if (isChecked) {
            editor.putString(PREF_EMAIL, email)
            editor.putString(PREF_PASSWORD, pass)
            editor.putBoolean(PREF_CHECKED, true)
        } else {
            editor.remove(PREF_EMAIL)
            editor.remove(PREF_PASSWORD)
            editor.remove(PREF_CHECKED)
        }
        editor.apply()
    }

    private fun loadRememberedCredentials() {
        val isChecked = prefs.getBoolean(PREF_CHECKED, false)
        if (isChecked) {
            val email = prefs.getString(PREF_EMAIL, "")
            val pass = prefs.getString(PREF_PASSWORD, "")

            // ⭐️ Hàm này bây giờ đã an toàn vì 'etEmail' đã được khởi tạo
            etEmail.setText(email)
            etPassword.setText(pass)
            cbRememberMe.isChecked = true
        }
    }
}