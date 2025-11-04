package ui.address

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.databinding.ActivityAddressFormBinding
import kotlinx.coroutines.launch
import model.Address
import service.serviceImplement.AddressService
import service.serviceImplement.UserService // ⭐️ (THÊM) Import UserService
import utils.SessionManager
import java.util.*

class AddressFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddressFormBinding
    private val addressService = AddressService()
    private val userService = UserService() // ⭐️ (THÊM) Khởi tạo UserService
    private lateinit var sessionManager: SessionManager
    private var currentUserId: String? = null // Giữ nguyên là nullable
    private var currentAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // ⭐️ (SỬA) Bắt đầu tải ID người dùng
        loadUserIdAndSetup()

        // Lấy địa chỉ (nếu là edit)
        currentAddress = intent.getSerializableExtra("address_to_edit") as? Address

        setupViews()
        setupListeners()
    }

    /**
     * ⭐️ (MỚI) Hàm này sẽ lấy email từ session,
     * dùng email để lấy User ID,
     * và lưu vào biến 'currentUserId'
     */
    private fun loadUserIdAndSetup() {
        lifecycleScope.launch {
            // 1. Lấy email từ session (giống như trong ProfileActivity)
            val email = sessionManager.getUserSession()?.first
            if (email == null) {
                Toast.makeText(this@AddressFormActivity, "Lỗi: Không tìm thấy phiên đăng nhập", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            // 2. Dùng email lấy User object
            val user = userService.getUserByEmail(email)
            if (user == null || user.id == null) {
                Toast.makeText(this@AddressFormActivity, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            // 3. Lưu User ID
            currentUserId = user.id
        }
    }

    private fun setupViews() {
        binding.toolbarAddressForm.setNavigationOnClickListener { finish() }

        if (currentAddress != null) {
            binding.toolbarAddressForm.title = "Chỉnh sửa địa chỉ"
            fillForm(currentAddress!!)
        } else {
            binding.toolbarAddressForm.title = "Địa chỉ mới"
        }
    }

    private fun fillForm(address: Address) {
        binding.edtFullName.setText(address.fullName)
        binding.edtPhoneNumber.setText(address.phoneNumber)
        binding.edtStreetAddress.setText(address.streetAddress)
        binding.edtCity.setText(address.city)
    }

    private fun setupListeners() {
        binding.btnSaveAddress.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val fullName = binding.edtFullName.text.toString().trim()
        val phone = binding.edtPhoneNumber.text.toString().trim()
        val street = binding.edtStreetAddress.text.toString().trim()
        val city = binding.edtCity.text.toString().trim()

        if (fullName.isEmpty() || phone.isEmpty() || street.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐️ (THÊM) Kiểm tra xem currentUserId đã được tải xong chưa
        if (currentUserId == null) {
            Toast.makeText(this, "Đang tải thông tin... Vui lòng thử lại", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUserId!! // ⭐️ Sử dụng ID đã được xác thực

        lifecycleScope.launch {
            val success: Boolean
            if (currentAddress == null) {
                // Thêm mới
                val newAddress = Address(
                    id = UUID.randomUUID().toString(),
                    fullName = fullName,
                    phoneNumber = phone,
                    streetAddress = street,
                    city = city,
                    isPrimaryShipping = false
                )
                success = addressService.addAddress(userId, newAddress) // ⭐️ Dùng userId
            } else {
                // Cập nhật
                val updatedAddress = currentAddress!!.copy(
                    fullName = fullName,
                    phoneNumber = phone,
                    streetAddress = street,
                    city = city
                )
                success = addressService.updateAddress(userId, updatedAddress) // ⭐️ Dùng userId
            }

            if (success) {
                Toast.makeText(this@AddressFormActivity, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@AddressFormActivity, "Lỗi khi lưu", Toast.LENGTH_SHORT).show()
            }
        }
    }
}