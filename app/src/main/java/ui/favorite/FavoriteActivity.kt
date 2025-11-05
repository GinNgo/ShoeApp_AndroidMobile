package ui.favorite // ⭐️ Tạo package mới

import adapter.ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import model.Product
import service.IFavoriteService
import service.serviceImplement.FavoriteService
import service.serviceImplement.UserService
import ui.BaseActivity
import ui.product.ProductDetailActivity

class FavoriteActivity : BaseActivity() {

    // --- Services ---
    private val favoriteService: IFavoriteService = FavoriteService()

    // --- Views ---
    private lateinit var recyclerFavorite: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout

    // --- Data ---
    private lateinit var productAdapter: ProductAdapter
    private var favoriteProducts = mutableListOf<Product>()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        initViews()
        setupRecyclerView()

        // ⭐️ Cần thêm ID 'nav_wishlist' vào bottom_nav_menu.xml
        handleNavigation(R.id.nav_wishlist)
    }

    /**
     * ⭐️ Tải lại mỗi khi quay về (để cập nhật nếu user vừa bỏ thích)
     */
    override fun onResume() {
        super.onResume()
        loadFavoriteData()
    }

    private fun initViews() {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarFavorite)
            .setNavigationOnClickListener { finish() }

        recyclerFavorite = findViewById(R.id.recyclerFavorite)
        emptyStateLayout = findViewById(R.id.emptyStateLayoutFavorite)
    }

    private fun setupRecyclerView() {
        // ⭐️ Dùng lại ProductAdapter của HomeActivity
        productAdapter = ProductAdapter(favoriteProducts) { product ->
            // Khi click 1 sản phẩm → chuyển qua màn chi tiết
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            startActivity(intent)
        }
        recyclerFavorite.layoutManager = GridLayoutManager(this, 2)
        recyclerFavorite.adapter = productAdapter
    }

    private fun loadFavoriteData() {
        lifecycleScope.launch {
            if (currentUserId == null) {
                currentUserId = getUserIdFromSession()
            }
            if (currentUserId == null) {
                Toast.makeText(this@FavoriteActivity, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
                toggleEmptyState(true)
                return@launch
            }

            // ⭐️ Gọi service (hàm này trả về List<Product>)
            val products = favoriteService.getAllFavorites(currentUserId!!)

            favoriteProducts.clear()
            favoriteProducts.addAll(products)
            productAdapter.notifyDataSetChanged()

            toggleEmptyState(favoriteProducts.isEmpty())
        }
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerFavorite.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerFavorite.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }

}