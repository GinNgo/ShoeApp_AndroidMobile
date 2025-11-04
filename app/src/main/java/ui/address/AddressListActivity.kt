package ui.address

import adapter.AddressAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast // ⭐️ (THÊM) Import
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
import service.serviceImplement.AddressService
import service.serviceImplement.UserService // ⭐️ (THÊM) Import UserService
import utils.SessionManager

class AddressListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AddressAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fab: FloatingActionButton

    private val addressService = AddressService()
    private val userService = UserService() // ⭐️ (THÊM) Khởi tạo UserService
    private lateinit var sessionManager: SessionManager
    private var currentUserId: String? = null // Giữ nguyên là nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)

        sessionManager = SessionManager(this)

        // ⭐️ (SỬA) Ánh xạ view và setup UI trước
        toolbar = findViewById(R.id.toolbarAddressList)
        fab = findViewById(R.id.fabAddAddress)
        recyclerView = findViewById(R.id.recyclerAddress)

        setupToolbar()
        setupRecyclerView()
        setupListeners()

        // ⭐️ (SỬA) Bắt đầu tải ID người dùng và sau đó tải địa chỉ
        // Hàm này sẽ tự gọi loadAddresses() sau khi có ID
        loadUserIdAndAddresses()
    }

    override fun onResume() {
        super.onResume()
        // ⭐️ (SỬA) Chỉ tải lại địa chỉ nếu UserID đã được tải thành công
        // (Không gọi loadAddresses() trực tiếp nữa để tránh lỗi)
        if (currentUserId != null) {
            loadAddresses()
        }
    }

    /**
     * ⭐️ (MỚI) Hàm này lấy UserID từ email
     * và CHỈ TẢI địa chỉ SAU KHI có UserID
     */
    private fun loadUserIdAndAddresses() {
        // Nếu đã có ID (ví dụ: trong onResume), chỉ cần tải lại địa chỉ
        if (currentUserId != null) {
            loadAddresses()
            return
        }

        // Nếu là lần tải đầu tiên (trong onCreate)
        lifecycleScope.launch {
            val email = sessionManager.getUserSession()?.first
            if (email == null) {
                Toast.makeText(this@AddressListActivity, "Lỗi: Không tìm thấy phiên đăng nhập", Toast.LENGTH_LONG).show()
                finish()
                setContentView(R.layout.activity_login)
                return@launch
            }

            val user = userService.getUserByEmail(email)
            if (user == null || user.id == null) {
                Toast.makeText(this@AddressListActivity, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            // ⭐️ Lưu ID thành công
            currentUserId = user.id

            // ⭐️ Bây giờ mới tải địa chỉ
            loadAddresses()
        }
    }


    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        fab.setOnClickListener {
            val intent = Intent(this, AddressFormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = AddressAdapter(
            onEdit = { address ->
                val intent = Intent(this, AddressFormActivity::class.java)
                intent.putExtra("address_to_edit", address)
                startActivity(intent)
            },
            onDelete = { address ->
                showDeleteConfirmDialog(address)
            },
            onSetPrimary = { address ->
                setPrimaryAddress(address.id)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadAddresses() {
        // ⭐️ (SỬA) Thêm kiểm tra 'currentUserId' an toàn
        currentUserId?.let { userId ->
            lifecycleScope.launch {
                val addresses = addressService.getAllAddresses(userId)
                // Sắp xếp cho địa chỉ mặc định lên đầu
                adapter.submitList(addresses.sortedByDescending { it.isPrimaryShipping })
            }
        }
    }

    private fun setPrimaryAddress(addressId: String) {
        // 1. Kiểm tra UserId (Giữ nguyên)
        currentUserId?.let { userId ->
            lifecycleScope.launch {

                // 2. Vẫn gọi Service để cập nhật database
                val success = addressService.setPrimaryAddress(userId, addressId)

                if (success) {
                    Snackbar.make(
                        recyclerView,
                        "Đã đặt làm địa chỉ mặc định",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // ⭐️ BẮT ĐẦU SỬA LỖI UI
                    // Thay vì gọi loadAddresses(), chúng ta sẽ tự cập nhật danh sách
                    // đang hiển thị trong adapter.

                    // 2a. Lấy danh sách hiện tại từ adapter
                    val currentList = adapter.currentList.toMutableList()

                    // 2b. Tìm vị trí (index) của địa chỉ MỚI (vừa chọn) và CŨ (trước đó)
                    val newPrimaryIndex = currentList.indexOfFirst { it.id == addressId }
                    val oldPrimaryIndex = currentList.indexOfFirst { it.isPrimaryShipping == true && it.id != addressId }

                    // 2c. Cập nhật thủ công 2 item đó
                    if (oldPrimaryIndex != -1) {
                        // Bỏ 'true' ở item cũ
                        currentList[oldPrimaryIndex] = currentList[oldPrimaryIndex].copy(isPrimaryShipping = false)
                    }
                    if (newPrimaryIndex != -1) {
                        // Thêm 'true' ở item mới
                        currentList[newPrimaryIndex] = currentList[newPrimaryIndex].copy(isPrimaryShipping = true)
                    }

                    // 2d. Submit danh sách đã thay đổi.
                    // ListAdapter (DiffUtil) sẽ tự động chỉ cập nhật 2 item đó.
                    // Chúng ta cũng sắp xếp lại để đảm bảo item mới lên đầu.
                    adapter.submitList(currentList.sortedByDescending { it.isPrimaryShipping })

                    // ⭐️ (XÓA) Bỏ dòng này, nó không cần thiết và gây ra lỗi
                    // loadAddresses()

                } else {
                    Snackbar.make(recyclerView, "Có lỗi xảy ra", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

        private fun showDeleteConfirmDialog(address: model.Address) {
            // ⭐️ (SỬA) Thêm kiểm tra 'currentUserId' an toàn
            currentUserId?.let { userId ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa địa chỉ này không?")
                    .setPositiveButton("Xóa") { dialog, _ ->
                        lifecycleScope.launch {
                            val success = addressService.deleteAddress(userId, address.id)
                            if (success) {
                                Snackbar.make(recyclerView, "Đã xóa địa chỉ", Snackbar.LENGTH_SHORT).show()
                                loadAddresses()
                            } else {
                                Snackbar.make(recyclerView, "Lỗi khi xóa", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        }
    }
