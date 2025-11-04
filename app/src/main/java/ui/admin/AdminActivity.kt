package ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import service.serviceImplement.BrandService // 👈 Thêm Service
import service.serviceImplement.CategoryService // 👈 Thêm Service
import service.serviceImplement.ProductService
import service.serviceImplement.VoucherService
import ui.admin.brand.AdminBrandActivity // 👈 Thêm Activity
import ui.admin.category.AdminCategoryActivity
import ui.admin.product.AdminProductActivity
import ui.admin.voucher.AdminVoucherActivity
import ui.auth.LoginActivity
import utils.SessionManager

class AdminActivity : AppCompatActivity() {

    private lateinit var cardProducts: MaterialCardView
    private lateinit var cardCategories: MaterialCardView
    private lateinit var cardBrands: MaterialCardView // 👈 Thêm Card
    private lateinit var cardRevenue: MaterialCardView
    private lateinit var cardUsers: MaterialCardView
    private lateinit var cardVouchers: MaterialCardView

    private lateinit var tvTotalProducts: TextView
    private lateinit var tvTotalCategories: TextView // 👈 Thêm TextView
    private lateinit var tvTotalBrands: TextView // 👈 Thêm TextView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalVouchers: TextView
    private val productService = ProductService()
    private val categoryService = CategoryService() // 👈 Thêm Service
    private val brandService = BrandService() // 👈 Thêm Service
    private val voucherService = VoucherService() // 👈
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Ánh xạ view
        cardProducts = findViewById(R.id.cardProducts)
        cardCategories = findViewById(R.id.cardCategories)
        cardBrands = findViewById(R.id.cardBrands) // 👈 Ánh xạ (Cần thêm ID này vào XML)
        cardRevenue = findViewById(R.id.cardRevenue)
        cardUsers = findViewById(R.id.cardUsers)
        cardVouchers = findViewById(R.id.cardVouchers)

        tvTotalProducts = findViewById(R.id.tvTotalProducts)
        tvTotalCategories = findViewById(R.id.tvTotalCategories) // 👈 Ánh xạ (Cần thêm ID này vào XML)
        tvTotalBrands = findViewById(R.id.tvTotalBrands) // 👈 Ánh xạ (Cần thêm ID này vào XML)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        tvTotalVouchers = findViewById(R.id.tvTotalVouchers)

        // Click listeners
        cardProducts.setOnClickListener {
            startActivity(Intent(this, AdminProductActivity::class.java))
        }

        cardCategories.setOnClickListener {
            startActivity(Intent(this, AdminCategoryActivity::class.java))
        }

        // 👈 Thêm click cho Card Brand
        cardBrands.setOnClickListener {
            startActivity(Intent(this, AdminBrandActivity::class.java))
        }
//
//        cardRevenue.setOnClickListener {
//            startActivity(Intent(this, RevenueActivity::class.java))
//        }
        cardVouchers.setOnClickListener { // 👈 THÊM
            startActivity(Intent(this, AdminVoucherActivity::class.java))
        }
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
                    sessionManager.clearSession()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        loadQuickStats()
    }

    private fun loadQuickStats() {
        lifecycleScope.launch {
            try {
                // Gọi song song (tối ưu hơn)
                val productCountJob = async { productService.getSizeProduct() }
                val categoryCountJob = async { categoryService.getSizeCategory() }
                val brandCountJob = async { brandService.getSizeBrand() }
                val voucherCountJob = async { voucherService.getSizeVoucher() }
                // Lấy kết quả
                val productCount = productCountJob.await()
                val categoryCount = categoryCountJob.await()
                val brandCount = brandCountJob.await()
                val voucherCount = voucherCountJob.await()

                // Cập nhật UI
                tvTotalProducts.text = productCount.toString()
                tvTotalCategories.text = categoryCount.toString()
                tvTotalBrands.text = brandCount.toString()
                tvTotalOrders.text = "—"
                tvTotalVouchers.text = voucherCount.toString()

            } catch (e: Exception) {
                tvTotalProducts.text = "—"
                tvTotalCategories.text = "—"
                tvTotalBrands.text = "—"
                tvTotalOrders.text = "—"
                tvTotalVouchers.text = "—"
            }
        }
    }
    override fun onResume() {
        super.onResume()
        loadQuickStats()
    }
}