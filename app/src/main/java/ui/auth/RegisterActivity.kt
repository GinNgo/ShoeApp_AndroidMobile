package ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button // ⭐️ (SỬA)
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
import model.User
import service.serviceImplement.UserService
// Bỏ import kotlin.jvm.java (không cần thiết)

class RegisterActivity : AppCompatActivity() {
    private val userService = UserService()

    // ⭐️ (THÊM) Khai báo View
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ⭐️ (SỬA) Ánh xạ View
        btnSignUp = findViewById(R.id.tvSignUp) // ID 'tvSignUp' đang là của Button
        tvSignIn = findViewById(R.id.tvSignIn)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword) // ⭐️ (THÊM)


        btnSignUp.setOnClickListener {
            lifecycleScope.launch {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim() // ⭐️ (THÊM)

                // --- ⭐️ (THÊM) Logic Validation ---
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this@RegisterActivity, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (password != confirmPassword) {
                    Toast.makeText(this@RegisterActivity, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (password.length < 6) {
                    Toast.makeText(this@RegisterActivity, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                // --- Hết Logic Validation ---

                val user = User(
                    email = email,
                    passwordHash = password, // (Lưu ý: nên hash mật khẩu này)
                    // role = 0 (đã là mặc định)
                )

                // ⭐️ (SỬA) Kiểm tra đăng ký thành công
                val success = userService.addUser(user)
                if (success) {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Đóng RegisterActivity
                } else {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại (Email có thể đã tồn tại)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}