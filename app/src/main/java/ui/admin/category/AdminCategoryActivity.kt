package ui.admin.category

import adapter.CategoryAdapter
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
import service.CategoryService

class AdminCategoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var categoryService: CategoryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_category)

        categoryService = CategoryService()

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCategory)
        toolbar.setNavigationOnClickListener { finish() }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerCategory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(
            onEdit = { category ->
                val intent = Intent(this, CategoryFormActivity::class.java)
                intent.putExtra("category", category)
                startActivity(intent)
            },
            onDelete = { category ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Xóa danh mục")
                    .setMessage("Bạn có chắc chắn muốn xóa danh mục \"${category.name}\" không?")
                    .setPositiveButton("Xóa") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                categoryService.deleteCategory(category.id)
                                Snackbar.make(recyclerView, "Đã xóa danh mục", Snackbar.LENGTH_SHORT).show()
                                loadCategories()
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
        findViewById<FloatingActionButton>(R.id.fabAddCategory).setOnClickListener {
            startActivity(Intent(this, CategoryFormActivity::class.java))
        }

        // Load data
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = categoryService.getAllCategories()
            adapter.submitList(categories)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }
}