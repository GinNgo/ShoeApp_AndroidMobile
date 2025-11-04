package ui.admin.voucher

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import com.example.shoesapp.databinding.ActivityVoucherFormBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import model.DiscountType
import model.Voucher
import service.serviceImplement.VoucherService
import java.text.SimpleDateFormat
import java.util.*

class VoucherFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoucherFormBinding
    private val voucherService = VoucherService()
    private var currentVoucher: Voucher? = null

    // Biến để lưu ngày hết hạn
    private var expiryDate: Date? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoucherFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy voucher (nếu là edit)
        currentVoucher = intent.getSerializableExtra("voucher") as? Voucher

        setupViews()
        setupListeners()

        if (currentVoucher != null) {
            fillForm(currentVoucher!!)
        }
    }

    private fun setupViews() {
        binding.toolbarVoucherForm.setNavigationOnClickListener { finish() }

        if (currentVoucher != null) {
            binding.toolbarVoucherForm.title = "Chỉnh sửa Voucher"
            binding.btnDeleteVoucher.visibility = View.VISIBLE
        } else {
            binding.toolbarVoucherForm.title = "Thêm Voucher"
            binding.btnDeleteVoucher.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // Ẩn/hiện ô "Giảm tối đa"
        binding.rgDiscountType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbPercentage) {
                binding.layoutMaxDiscount.visibility = View.VISIBLE
            } else {
                binding.layoutMaxDiscount.visibility = View.GONE
            }
        }

        // Dialog chọn ngày
        binding.btnSelectExpiryDate.setOnClickListener {
            showDatePicker()
        }

        // Nút Lưu
        binding.btnSaveVoucher.setOnClickListener {
            saveVoucher()
        }

        // Nút Xóa
        binding.btnDeleteVoucher.setOnClickListener {
            // (Code xóa tương tự như AdminBrandActivity...)
        }

        // Nút Quay lại
        binding.btnBackToList.setOnClickListener { finish() }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            expiryDate = calendar.time
            binding.tvSelectedExpiryDate.text = "Đã chọn: ${dateFormatter.format(expiryDate!!)}"
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun fillForm(voucher: Voucher) {
        binding.edtVoucherCode.setText(voucher.code)
        binding.edtDescription.setText(voucher.description)

        if (voucher.discountType == DiscountType.PERCENTAGE) {
            binding.rbPercentage.isChecked = true
            binding.layoutMaxDiscount.visibility = View.VISIBLE
            binding.edtMaxDiscount.setText(voucher.maxDiscountAmount?.toString() ?: "")
        } else {
            binding.rbFixedAmount.isChecked = true
            binding.layoutMaxDiscount.visibility = View.GONE
        }

        binding.edtDiscountValue.setText(voucher.discountValue.toString())
        binding.edtMinOrder.setText(voucher.minOrderValue.toString())
        binding.edtUsageLimit.setText(voucher.usageLimit.toString())

        voucher.expirationDate?.let {
            expiryDate = it
            binding.tvSelectedExpiryDate.text = "Đã chọn: ${dateFormatter.format(it)}"
        }

        binding.switchIsActive.isChecked = voucher.isActive
    }

    private fun saveVoucher() {
        val code = binding.edtVoucherCode.text.toString().trim().uppercase()
        val description = binding.edtDescription.text.toString().trim()
        val discountValue = binding.edtDiscountValue.text.toString().toDoubleOrNull()

        if (code.isEmpty() || discountValue == null) {
            Toast.makeText(this, "Mã voucher và Giá trị giảm là bắt buộc", Toast.LENGTH_SHORT).show()
            return
        }

        val discountType = if (binding.rbPercentage.isChecked) DiscountType.PERCENTAGE else DiscountType.FIXED_AMOUNT
        val maxDiscount = if (discountType == DiscountType.PERCENTAGE) {
            binding.edtMaxDiscount.text.toString().toDoubleOrNull()
        } else null
        val minOrder = binding.edtMinOrder.text.toString().toDoubleOrNull() ?: 0.0
        val usageLimit = binding.edtUsageLimit.text.toString().toIntOrNull() ?: 0
        val isActive = binding.switchIsActive.isChecked

        val voucher = Voucher(
            id = currentVoucher?.id ?: UUID.randomUUID().toString(),
            code = code,
            description = description,
            discountType = discountType,
            discountValue = discountValue,
            maxDiscountAmount = maxDiscount,
            minOrderValue = minOrder,
            expirationDate = expiryDate,
            usageLimit = usageLimit,
            usageCount = currentVoucher?.usageCount ?: 0, // Giữ nguyên số lần đã dùng
            isActive = isActive,
            createdAt = currentVoucher?.createdAt ?: Date()
        )

        lifecycleScope.launch {
            val success = if (currentVoucher == null) {
                voucherService.addVoucher(voucher)
            } else {
                voucherService.updateVoucher(voucher)
            }

            if (success) {
                Snackbar.make(binding.root, "Đã lưu voucher!", Snackbar.LENGTH_SHORT).show()
                finish()
            } else {
                Snackbar.make(binding.root, "Lỗi khi lưu voucher!", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}