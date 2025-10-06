package ui.home

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import model.Product
import adapter.GridProductAdapter
import service.ProductService
import ui.BaseActivity

class CartActivity : BaseActivity() {

    private lateinit var productList: ArrayList<Product>
    private lateinit var gridAdapter: GridProductAdapter
    private lateinit var productService: ProductService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cart)

        productList = ArrayList()
        productService = ProductService() // khởi tạo service

        val gridView = findViewById<GridView>(R.id.grid_view)
        gridAdapter = GridProductAdapter(this, productList)
        gridView.adapter = gridAdapter

        // 🔹 Gọi Firestore để load dữ liệu giày
        lifecycleScope.launch {
            val products = productService.getAllProducts() // lấy từ Firestore
            productList.clear()
            productList.addAll(products)
            gridAdapter.notifyDataSetChanged() // update giao diện
        }

        handleNavigation(R.id.nav_cart)
    }
}
