package ui.admin.voucher

import adapter.VoucherAdapter
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
import service.serviceImplement.VoucherService

class AdminVoucherActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VoucherAdapter
    private lateinit var voucherService: VoucherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_voucher)

        voucherService = VoucherService()

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarVoucher)
        toolbar.setNavigationOnClickListener { finish() }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerVoucher)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VoucherAdapter(
            onEdit = { voucher ->
                val intent = Intent(this, VoucherFormActivity::class.java)
                intent.putExtra("voucher", voucher)
                startActivity(intent)
            },
            onDelete = { voucher ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Xóa Voucher")
                    .setMessage("Bạn có chắc chắn muốn xóa voucher \"${voucher.code}\" không?")
                    .setPositiveButton("Xóa") { dialog, _ ->
                        lifecycleScope.launch {
                            try {
                                voucherService.deleteVoucher(voucher.id)
                                Snackbar.make(recyclerView, "Đã xóa voucher", Snackbar.LENGTH_SHORT).show()
                                loadVouchers()
                            } catch (e: Exception) {
                                Snackbar.make(recyclerView, "Lỗi khi xóa: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Nút thêm
        findViewById<FloatingActionButton>(R.id.fabAddVoucher).setOnClickListener {
            startActivity(Intent(this, VoucherFormActivity::class.java))
        }
    }

    private fun loadVouchers() {
        lifecycleScope.launch {
            val vouchers = voucherService.getAllVouchers()
            adapter.submitList(vouchers)
        }
    }

    override fun onResume() {
        super.onResume()
        loadVouchers() // Tải lại data khi quay về
    }
}