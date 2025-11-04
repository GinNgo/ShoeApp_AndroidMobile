package ui.auth

import android.app.Activity
import android.content.Context // ⭐️ (THÊM)
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.shoesapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView // ⭐️ (SỬA)
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers // ⭐️ (THÊM)
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // ⭐️ (THÊM)
import model.CustomBottomSheetDialog
import service.serviceImplement.UserService
import ui.address.AddressListActivity
import ui.home.HomeActivity
import utils.SessionManager
import java.io.File // ⭐️ (THÊM)
import java.io.FileOutputStream // ⭐️ (THÊM)

class ProfileActivity : AppCompatActivity() {

    private val userService = UserService()
    private lateinit var sessionManager: SessionManager

    // --- Khai báo Views ---
    private lateinit var edtFirstName: TextInputEditText
    private lateinit var edtLastName: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var edtPhone: TextInputEditText
    private lateinit var spinnerGender: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var btnManageAddress: MaterialButton
    private lateinit var imgAvatar: ShapeableImageView // ⭐️ (SỬA)
    private lateinit var fabChangeAvatar: FloatingActionButton

    // --- Data ---
    // ⭐️ (SỬA) Biến này sẽ lưu ĐƯỜNG DẪN FILE (vd: /data/.../avatar.jpg)
    private var currentAvatarUrl: String? = null

    // --- Activity Result Launcher (DÙNG LẠI) ---
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val imageUri = result.data?.data
            imageUri?.let {
                // ⭐️ (SỬA) Thay vì chỉ hiển thị, chúng ta LƯU file
                lifecycleScope.launch {
                    val filePath = saveAvatarToInternalStorage(it)
                    if (filePath != null) {
                        currentAvatarUrl = filePath // 1. Lưu đường dẫn mới
                        // 2. Tải ảnh từ file vừa lưu
                        Glide.with(this@ProfileActivity)
                            .load(File(filePath))
                            .into(imgAvatar)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Lưu ảnh tạm thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * ⭐️ (MỚI) Hàm copy ảnh từ Uri vào bộ nhớ trong
     */
    private suspend fun saveAvatarToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = contentResolver.openInputStream(uri)
            // Tạo một file mới tên là "avatar.jpg" trong thư mục 'files' của app
            val file = File(filesDir, "avatar.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream) // Copy

            inputStream?.close()
            outputStream.close()

            file.absolutePath // 👈 Trả về đường dẫn tuyệt đối (vd: /data/...)
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Lỗi lưu file: ${e.message}", e)
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_form)

        sessionManager = SessionManager(this)

        initViews()
        setupListeners()
        setupGenderDropdown()
        loadUserProfile()
    }

    private fun initViews() {
        edtFirstName = findViewById(R.id.first_name)
        edtLastName = findViewById(R.id.last_name)
        edtEmail = findViewById(R.id.mail)
        edtPhone = findViewById(R.id.phone)
        spinnerGender = findViewById(R.id.spinnerGender)
        btnSave = findViewById(R.id.submit_btn)
        btnBack = findViewById(R.id.back_home)
        btnManageAddress = findViewById(R.id.btnManageAddress)
        imgAvatar = findViewById(R.id.imgAvatar)
        fabChangeAvatar = findViewById(R.id.fabChangeAvatar)
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        spinnerGender.setAdapter(adapter)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnSave.setOnClickListener {
            CustomBottomSheetDialog.show(
                context = this,
                title = "Lưu hồ sơ",
                message = "Bạn có chắc muốn lưu thay đổi này?",
                positiveText = "Có, Lưu",
                negativeText = "Hủy",
                onConfirm = {
                    performSaveProfile()
                }
            )
        }

        btnManageAddress.setOnClickListener {
            val intent = Intent(this, AddressListActivity::class.java)
            startActivity(intent)
        }

        // ⭐️ (SỬA) Mở thư viện ảnh của điện thoại
        fabChangeAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            val email = sessionManager.getUserSession()?.first ?: run {
                Toast.makeText(this@ProfileActivity, "Không tìm thấy phiên người dùng", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val user = userService.getUserByEmail(email)

            if (user != null) {
                edtFirstName.setText(user.firstName)
                edtLastName.setText(user.lastName)
                edtPhone.setText(user.phoneNumber)
                edtEmail.setText(email)

                user.gender?.let {
                    spinnerGender.setText(it, false)
                }

                // ⭐️ (SỬA) Tải ảnh đại diện từ ĐƯỜNG DẪN FILE
                user.avatarUrl?.let { path ->
                    currentAvatarUrl = path // Lưu đường dẫn
                    val avatarFile = File(path)

                    if (avatarFile.exists()) {
                        Glide.with(this@ProfileActivity)
                            .load(avatarFile) // 👈 Tải từ File
                            .placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar)
                            .into(imgAvatar)
                    } else {
                        // Nếu file không tồn tại (lạ), dùng ảnh mặc định
                        imgAvatar.setImageResource(R.drawable.avatar)
                    }
                } ?: run {
                    imgAvatar.setImageResource(R.drawable.avatar)
                }
            } else {
                Toast.makeText(this@ProfileActivity, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSaveProfile() {
        lifecycleScope.launch {
            val gender = spinnerGender.text.toString()

            val email = sessionManager.getUserSession()?.first ?: run {
                Toast.makeText(this@ProfileActivity, "Lỗi: Không tìm thấy phiên người dùng.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val user = userService.getUserByEmail(email)

            if (user != null) {
                // ⭐️ (SỬA) Bỏ logic upload
                // 'currentAvatarUrl' đã được cập nhật khi chọn ảnh

                // ✅ Tạo map dữ liệu (lưu ĐƯỜNG DẪN FILE)
                val profileData: Map<String, Any?> = mapOf(
                    "firstName" to edtFirstName.text.toString(),
                    "lastName" to edtLastName.text.toString(),
                    "phoneNumber" to edtPhone.text.toString(),
                    "gender" to gender,
                    "avatarUrl" to currentAvatarUrl // ⭐️ Lưu đường dẫn (vd: /data/.../avatar.jpg)
                )

                val success = userService.updateUser(user.id.toString(), profileData)
                if (success) {
                    Toast.makeText(this@ProfileActivity, "Lưu hồ sơ thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Lưu hồ sơ thất bại", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ProfileActivity, "Lỗi: Không tìm thấy người dùng để cập nhật.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}