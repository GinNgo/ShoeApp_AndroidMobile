package ui.admin.brand // 👈 Đổi package

import adapter.BrandAdapter // 👈 Đổi Adapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import service.BrandService // 👈 Đổi Service

class AdminBrandActivity : AppCompatActivity() { // 👈 Đổi tên Class
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BrandAdapter // 👈 Đổi Adapter
    private lateinit var brandService: BrandService // 👈 Đổi Service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 💡 Bạn cần tạo file layout activity_admin_brand.xml (xem ở mục 7)
        setContentView(R.layout.activity_admin_brand)

        brandService = BrandService()

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarBrand) // 👈 Đổi ID
        toolbar.setNavigationOnClickListener { finish() }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerBrand) // 👈 Đổi ID
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BrandAdapter(
            onEdit = { brand ->
                val intent = Intent(this, BrandFormActivity::class.java) // 👈 Đổi Form
                intent.putExtra("brand", brand) // 👈 Đổi key
                startActivity(intent)
            },
            onDelete = { brand ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Xóa thương hiệu") // 👈 Đổi text
                    .setMessage("Bạn có chắc chắn muốn xóa thương hiệu \"${brand.name}\" không?") // 👈 Đổi text
                    .setPositiveButton("Xóa") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                brandService.deleteBrand(brand.id) // 👈 Đổi hàm
                                Snackbar.make(recyclerView, "Đã xóa thương hiệu", Snackbar.LENGTH_SHORT).show()
                                loadBrands() // 👈 Đổi hàm
                            } catch (e: Exception) {
                                Snackbar.make(recyclerView, "Lỗi khi xóa: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Hủy") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Nút thêm
        findViewById<FloatingActionButton>(R.id.fabAddBrand).setOnClickListener { // 👈 Đổi ID
            startActivity(Intent(this, BrandFormActivity::class.java)) // 👈 Đổi Form
        }

        // Load data
        loadBrands()
    }

    private fun loadBrands() { // 👈 Đổi hàm
        lifecycleScope.launch {
            val brands = brandService.getAllBrands() // 👈 Đổi hàm
            adapter.submitList(brands)
        }
    }

    override fun onResume() {
        super.onResume()
        loadBrands() // 👈 Đổi hàm
    }
}