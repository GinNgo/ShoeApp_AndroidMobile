package ui.admin.brand // 👈 Đổi package

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// 💡 Bạn cần tạo file layout activity_brand_form.xml (xem ở mục 7)
import com.example.shoesapp.databinding.ActivityBrandFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import model.Brand // 👈 Đổi Model
import service.serviceImplement.BrandService // 👈 Đổi Service
import java.util.*

class BrandFormActivity : AppCompatActivity() { // 👈 Đổi tên Class

    private lateinit var binding: ActivityBrandFormBinding // 👈 Đổi Binding
    private val brandService = BrandService() // 👈 Đổi Service
    private var currentBrand: Brand? = null // 👈 Đổi Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrandFormBinding.inflate(layoutInflater) // 👈 Đổi Binding
        setContentView(binding.root)

        // 🔹 Nút quay lại trên toolbar
        binding.toolbarBrandForm.setNavigationOnClickListener { finish() } // 👈 Đổi ID

        // 🔹 Kiểm tra xem là thêm mới hay chỉnh sửa
        currentBrand = intent.getSerializableExtra("brand") as? Brand // 👈 Đổi key và Model

        if (currentBrand != null) {
            // 👉 Chế độ chỉnh sửa
            binding.toolbarBrandForm.title = "Chỉnh sửa thương hiệu" // 👈 Đổi text
            binding.edtBrandName.setText(currentBrand!!.name) // 👈 Đổi ID
            binding.edtBrandDescription.setText(currentBrand!!.description) // 👈 Đổi ID
            binding.btnDeleteBrand.visibility = android.view.View.VISIBLE // 👈 Đổi ID
        } else {
            // 👉 Chế độ thêm mới
            binding.toolbarBrandForm.title = "Thêm thương hiệu" // 👈 Đổi text
            binding.btnDeleteBrand.visibility = android.view.View.GONE // 👈 Đổi ID
        }

        // 🔹 Nút lưu
        binding.btnSaveBrand.setOnClickListener { // 👈 Đổi ID
            saveBrand()
        }

        // 🔹 Nút xóa (chỉ khi chỉnh sửa)
        binding.btnDeleteBrand.setOnClickListener { // 👈 Đổi ID
            showDeleteConfirmDialog()
        }
        binding.btnBackToList.setOnClickListener {
            finish() // quay lại danh sách
        }
    }

    private fun saveBrand() {
        val name = binding.edtBrandName.text.toString().trim() // 👈 Đổi ID
        val desc = binding.edtBrandDescription.text.toString().trim() // 👈 Đổi ID

        if (name.isEmpty()) {
            binding.layoutBrandName.error = "Vui lòng nhập tên thương hiệu" // 👈 Đổi text
            return
        }

        val brand = Brand( // 👈 Đổi Model
            id = currentBrand?.id ?: UUID.randomUUID().toString(),
            name = name,
            description = desc,
            createdAt = currentBrand?.createdAt ?: Date()
        )

        lifecycleScope.launch {
            try {
                if (currentBrand == null) {
                    brandService.addBrand(brand) // 👈 Đổi Service
                    Snackbar.make(binding.root, "Đã thêm thương hiệu mới", Snackbar.LENGTH_SHORT).show()
                } else {
                    brandService.updateBrand(brand) // 👈 Đổi Service
                    Snackbar.make(binding.root, "Đã cập nhật thương hiệu", Snackbar.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Log.e("BrandFormActivity", "❌ Lỗi lưu thương hiệu: ${e.message}", e)
                Snackbar.make(binding.root, "Lỗi khi lưu thương hiệu!", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        val brand = currentBrand ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa thương hiệu \"${brand.name}\" không?") // 👈 Đổi text
            .setPositiveButton("Xóa") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        brandService.deleteBrand(brand.id) // 👈 Đổi Service
                        Snackbar.make(binding.root, "Đã xóa thương hiệu", Snackbar.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Log.e("BrandFormActivity", "❌ Lỗi khi xóa thương hiệu: ${e.message}", e)
                        Snackbar.make(binding.root, "Không thể xóa thương hiệu", Snackbar.LENGTH_LONG).show()
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