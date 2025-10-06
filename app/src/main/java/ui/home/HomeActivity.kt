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
        // Fake data sản phẩm
        fun seedProducts() {
            val products = listOf(
                Product(
                    name = "Puma Suede Classic",
                    description = "Giày sneaker cổ điển với chất liệu da lộn mềm mại, phong cách retro.",
                    price = 120.0, brand = "Puma",
                    images = listOf(ProductImage("ic_shoe_puma", true)) ),
                Product( name = "Nike Air Max 270",
                    description = "Nike Air Max 270 với đế Air lớn, êm ái và phong cách thể thao hiện đại.",
                    price = 150.0, brand = "Nike",
                    images = listOf(ProductImage("ic_shoe", true)) ),
                Product( name = "Adidas Ultraboost",
                    description = "Ultraboost mang lại sự thoải mái tối đa, thiết kế ôm chân và đế Boost đàn hồi.",
                    price = 180.0, brand = "Adidas",
                    images = listOf(ProductImage("ic_shoe_adidas", true)) ),
                Product( name = "Converse Chuck Taylor",
                    description = "Giày Converse Chuck Taylor cổ điển, phù hợp với mọi phong cách thời trang.",
                    price = 90.0, brand = "Converse",
                    images = listOf(ProductImage("ic_shoe2", true)) ),
                Product( name = "New Balance 574",
                    description = "Thiết kế retro pha chút hiện đại, êm ái khi đi bộ và phù hợp đi hàng ngày.",
                    price = 110.0, brand = "New Balance",
                    images = listOf(ProductImage("ic_shoe3", true)) ),
                Product( name = "Vans Old Skool",
                    description = "Vans Old Skool biểu tượng với đường sọc side stripe, phong cách streetwear.",
                    price = 85.0, brand = "Vans",
                    images = listOf(ProductImage("ic_shoe5", true)) ),
                Product( name = "Nike Air Force 1",
                    description = "Air Force 1 huyền thoại, thiết kế da trắng đơn giản nhưng cực kỳ phong cách.",
                    price = 100.0, brand = "Nike",
                    images = listOf(ProductImage("ic_shoe6", true)) ),
                Product( name = "Adidas Stan Smith",
                    description = "Stan Smith tối giản, dễ phối đồ, một trong những đôi giày nổi tiếng nhất của Adidas.",
                    price = 95.0, brand = "Adidas",
                    images = listOf(ProductImage("ic_shoe_adidas16", true)) ),
                Product( name = "Reebok Classic Leather",
                    description = "Phong cách retro, chất liệu da mềm, thích hợp mang cả ngày.",
                    price = 80.0, brand = "Reebok",
                    images = listOf(ProductImage("ic_shoe8", true)) ),
                Product( name = "Asics Gel-Kayano 27",
                    description = "Dòng giày chạy bộ cao cấp với công nghệ Gel giảm chấn đặc trưng.",
                    price = 160.0, brand = "Asics", images = listOf(ProductImage("ic_shoe9", true)) ),
                Product( name = "Nike Dunk Low",
                    description = "Nike Dunk Low với phối màu đa dạng, cực hot trong giới trẻ streetwear.",
                    price = 140.0, brand = "Nike", images = listOf(ProductImage("ic_shoe10", true)) ),
                Product( name = "Adidas Yeezy Boost 350",
                    description = "Thiết kế độc đáo bởi Kanye West, cực kỳ êm ái và cá tính.",
                    price = 220.0, brand = "Adidas", images = listOf(ProductImage("ic_shoe_adidas10", true)) ),
                Product( name = "Fila Disruptor II", description = "Phong cách chunky sneaker với đế răng cưa nổi bật.",
                    price = 75.0, brand = "Fila", images = listOf(ProductImage("ic_shoe12", true)) ),
                Product( name = "Jordan 1 Retro High",
                    description = "Huyền thoại Jordan 1, đôi giày gắn liền với lịch sử bóng rổ và sneakerhead.",
                    price = 200.0, brand = "Nike Jordan",
                    images = listOf(ProductImage("ic_shoe_adidas9", true)) ) )
        // 🔹 Gọi coroutine để upload từng sản phẩm
        lifecycleScope.launch {
            try {
                for (product in products) {
                    productService.addProduct(product)
                }
                Toast.makeText(this@HomeActivity, "Thêm thành công", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Log.e("FirestoreError", "Lỗi khi thêm product", e)
                Toast.makeText(this@HomeActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
//     seedProducts();

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
                val products = productService.getAllProducts()
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
