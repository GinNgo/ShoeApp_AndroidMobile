package ui.admin.category

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.databinding.ActivityCategoryFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import model.Category
import service.CategoryService
import java.util.*

class CategoryFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryFormBinding
    private val categoryService = CategoryService()
    private var currentCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Nút quay lại trên toolbar
        binding.toolbarCategoryForm.setNavigationOnClickListener { finish() }

        // 🔹 Kiểm tra xem là thêm mới hay chỉnh sửa
        currentCategory = intent.getSerializableExtra("category") as? Category

        if (currentCategory != null) {
            // 👉 Chế độ chỉnh sửa
            binding.toolbarCategoryForm.title = "Chỉnh sửa danh mục"
            binding.edtCategoryName.setText(currentCategory!!.name)
            binding.edtCategoryDescription.setText(currentCategory!!.description)
            binding.btnDeleteCategory.visibility = android.view.View.VISIBLE
        } else {
            // 👉 Chế độ thêm mới
            binding.toolbarCategoryForm.title = "Thêm danh mục"
            binding.btnDeleteCategory.visibility = android.view.View.GONE
        }

        // 🔹 Nút lưu
        binding.btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        // 🔹 Nút xóa (chỉ khi chỉnh sửa)
        binding.btnDeleteCategory.setOnClickListener {
            showDeleteConfirmDialog()
        }
        binding.btnBackToList.setOnClickListener {
            finish() // quay lại danh sách
        }
    }

    private fun saveCategory() {
        val name = binding.edtCategoryName.text.toString().trim()
        val desc = binding.edtCategoryDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.layoutCategoryName.error = "Vui lòng nhập tên danh mục"
            return
        }

        val category = Category(
            id = currentCategory?.id ?: UUID.randomUUID().toString(),
            name = name,
            description = desc,
            createdAt = currentCategory?.createdAt ?: Date()
        )

        lifecycleScope.launch {
            try {
                if (currentCategory == null) {
                    categoryService.addCategory(category)
                    Snackbar.make(binding.root, "Đã thêm danh mục mới", Snackbar.LENGTH_SHORT).show()
                } else {
                    categoryService.updateCategory(category)
                    Snackbar.make(binding.root, "Đã cập nhật danh mục", Snackbar.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Log.e("CategoryFormActivity", "❌ Lỗi lưu danh mục: ${e.message}", e)
                Snackbar.make(binding.root, "Lỗi khi lưu danh mục!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        val category = currentCategory ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa danh mục \"${category.name}\" không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        categoryService.deleteCategory(category.id)
                        Snackbar.make(binding.root, "Đã xóa danh mục", Snackbar.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Log.e("CategoryFormActivity", "❌ Lỗi khi xóa danh mục: ${e.message}", e)
                        Snackbar.make(binding.root, "Không thể xóa danh mục", Snackbar.LENGTH_LONG).show()
                    } finally {
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
