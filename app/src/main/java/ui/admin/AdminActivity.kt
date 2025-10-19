package ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import com.example.shoesapp.databinding.ActivityAdminBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import model.Category
import model.Product
import service.ProductService
import ui.admin.category.AdminCategoryActivity
import ui.auth.LoginActivity
import utils.SessionManager

class AdminActivity : AppCompatActivity() {

    private lateinit var cardProducts: MaterialCardView
    private lateinit var cardCategories: MaterialCardView
    private lateinit var cardRevenue: MaterialCardView
    private lateinit var cardUsers: MaterialCardView
    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalOrders: TextView
    private val productService = ProductService()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Ánh xạ view
        cardProducts = findViewById(R.id.cardProducts)
        cardCategories = findViewById(R.id.cardCategories)
        cardRevenue = findViewById(R.id.cardRevenue)
        cardUsers = findViewById(R.id.cardUsers)
        tvTotalProducts = findViewById(R.id.tvTotalProducts)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)


        // Click listeners
//        cardProducts.setOnClickListener {
//            startActivity(Intent(this, ProductsActivity::class.java))
//        }
//
        cardCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoryActivity::class.java))
        }
//
//        cardRevenue.setOnClickListener {
//            startActivity(Intent(this, RevenueActivity::class.java))
//        }

        cardUsers.setOnClickListener {
            Toast.makeText(this, "Chức năng quản lý người dùng chưa cài đặt", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, UsersActivity::class.java))
        }

        // toolbar menu - logout
        sessionManager = SessionManager(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarAdmin)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    // 🔹 Xoá session
                    sessionManager.clearSession()

                    // 🔹 Nếu dùng FirebaseAuth
                    // FirebaseAuth.getInstance().signOut()

                    // 🔹 Quay lại màn Login và xoá toàn bộ stack
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        // Load quick stats
        loadQuickStats()
    }

    private fun loadQuickStats() {
        lifecycleScope.launch {
            try {
                // Gọi Firestore qua service (giả sử bạn có hàm suspend getSizeProduct)
                val productCount = productService.getSizeProduct()

                // Cập nhật UI trong Main thread
                tvTotalProducts.text = productCount.toString()

                // Nếu chưa làm orders thì để mặc định
                tvTotalOrders.text = "—"
            } catch (e: Exception) {
                tvTotalProducts.text = "—"
                tvTotalOrders.text = "—"
            }
        }
    }
}