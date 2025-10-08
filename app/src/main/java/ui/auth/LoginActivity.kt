package ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        // 🔹 Thêm user mới
//        lifecycleScope.launch() {
//            try {
//                val user = User(
//                    username = "tuanan",
//                    email = "ada@gmail.com",
//                    passwordHash = "12345hash",
//                    firstName = "Ada",
//                    lastName = "Lovelace",
//                    role = 1
//                )
//
//                // 🟢 Gọi service để thêm user
//                userService.addUser(user)
//
//                println("✅ User thêm thành công!")
//            } catch (e: Exception) {
//                println("❌ Lỗi khi thêm user: ${e.message}")
//            }
//        }
        var tvSignIn = findViewById<Button>(R.id.btnLogin)
        tvSignIn.setOnClickListener {
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        // Căn lề cho status/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}
