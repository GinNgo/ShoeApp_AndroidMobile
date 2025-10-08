package ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import adapter.ProductAdapter
import model.Product
import model.ProductImage
import service.ProductService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ui.BaseActivity
import ui.auth.ProfileActivity
import ui.product.ProductDetailActivity
import android.util.Log

class HomeActivity : BaseActivity() {

    private lateinit var recyclerProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private var productList = ArrayList<Product>() // ✅ init luôn
    private val productService = ProductService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Padding cho status bar/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerProducts = findViewById(R.id.recyclerProducts)
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)

        // Setup Adapter
        productAdapter = ProductAdapter(productList) { product ->
            // 🔹 Khi click 1 sản phẩm → chuyển qua màn chi tiết
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product) // Product cần Parcelable
            startActivity(intent)
        }
        recyclerProducts.adapter = productAdapter

        // 🔹 Gọi Firestore để load danh sách sản phẩm
        lifecycleScope.launch {
            try {
                val products = productService.getAllProducts().map { p ->
                    Product(
                        name = p.name,
                        description = p.description,
                        price = p.price,
                        brand = p.brand,
                        images = p.images // danh sách ảnh của fen
                    )
                }
                productList.clear()
                productList.addAll(products)
                productAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("FirestoreError", "Lỗi load products", e)
                Toast.makeText(this@HomeActivity, "Lỗi tải sản phẩm: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        onProfile()
        handleNavigation(R.id.nav_home)
    }

    private fun onProfile() {
        val profile = findViewById<ImageView>(R.id.profile_form)
        profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
