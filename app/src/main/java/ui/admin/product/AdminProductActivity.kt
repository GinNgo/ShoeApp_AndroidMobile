package ui.admin.product

import adapter.AdminProductAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import ui.admin.product.ProductFormActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import model.Product
import service.ProductService

class AdminProductActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminProductAdapter
    private val productService = ProductService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_product)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProduct)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerProduct)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 🔹 Adapter AdminProductAdapter
        adapter = AdminProductAdapter(
            onEdit = { product ->
                val intent = Intent(this, ProductFormActivity::class.java)
                intent.putExtra("product", product)
                startActivity(intent)
            },
            onDelete = { product ->
                confirmDeleteProduct(product)
            }
        )
        recyclerView.adapter = adapter

        // 🔹 Nút thêm sản phẩm
        findViewById<FloatingActionButton>(R.id.fabAddProduct).setOnClickListener {
            startActivity(Intent(this, ProductFormActivity::class.java))
        }

        // 🔹 Load danh sách ban đầu
        loadProducts()
    }

    private fun confirmDeleteProduct(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Xóa sản phẩm")
            .setMessage("Bạn có chắc chắn muốn xóa \"${product.name}\" không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        val result = productService.deleteProduct(product.id)
                        if (result) {
                            Snackbar.make(recyclerView, "✅ Đã xóa sản phẩm", Snackbar.LENGTH_SHORT).show()
                            loadProducts()
                        } else {
                            Snackbar.make(recyclerView, "❌ Xóa thất bại", Snackbar.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Snackbar.make(
                            recyclerView,
                            "Lỗi khi xóa: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                val products = productService.getAllProducts()
                adapter.submitList(products)
            } catch (e: Exception) {
                Snackbar.make(
                    recyclerView,
                    "Không thể tải sản phẩm: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }
}
