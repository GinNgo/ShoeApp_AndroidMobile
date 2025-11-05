package ui.home

import adapter.ProductAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button // ⭐️ (THÊM)
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout // ⭐️ (THÊM)
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoesapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Brand
import model.CustomBottomSheetDialog
import model.Product
import model.User
import service.serviceImplement.BrandService
import service.serviceImplement.ProductService
import service.serviceImplement.UserService
import ui.BaseActivity
import ui.auth.LoginActivity
import ui.auth.ProfileActivity
import ui.favorite.FavoriteActivity
import ui.product.ProductDetailActivity
import utils.SessionManager
import java.io.File
import java.util.Calendar

class HomeActivity : BaseActivity() {

    // --- Services ---
    private val productService = ProductService()
    private val brandService = BrandService()
    private val userService = UserService()
    private lateinit var sessionManager: SessionManager

    // --- Views ---
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var chipGroupBrands: ChipGroup
    private lateinit var etSearch: EditText
    private lateinit var tvName: TextView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var logoutButton: ImageView
    private lateinit var tvGreeting: TextView
    private lateinit var tvEmptyView: TextView
    private lateinit var layoutUserInfo: LinearLayout // ⭐️ (THÊM)
    private lateinit var btnLogin: Button // ⭐️ (THÊM)
    private lateinit var btnWishlist: ImageView
    // --- Adapters & Data ---
    private lateinit var productAdapter: ProductAdapter
    private var allProductsList = ArrayList<Product>()
    private var displayedProductList = ArrayList<Product>()

    // --- Trạng thái ---
    private var currentBrandId: String? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        initViews()
        sessionManager = SessionManager(this)

        setupListeners()
        setupRecyclerView()
        loadInitialData()

        handleNavigation(R.id.nav_home)
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Tải lại thông tin user khi quay lại
    }

    private fun initViews() {
        // Ánh xạ View
        recyclerProducts = findViewById(R.id.recyclerProducts)
        chipGroupBrands = findViewById(R.id.chipGroupBrands)
        etSearch = findViewById(R.id.etSearch)
        tvName = findViewById(R.id.tvName)
        profileImage = findViewById(R.id.profile_form)
        logoutButton = findViewById(R.id.ic_logout)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvEmptyView = findViewById(R.id.tvEmptyView)
        btnWishlist = findViewById(R.id.btnWishlist)
        // ⭐️ (THÊM) Ánh xạ các view mới
        layoutUserInfo = findViewById(R.id.layoutUserInfo)
        btnLogin = findViewById(R.id.btnLogin)

        // Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Tải thông tin user (gọi trong onResume)
     */
    private fun loadUserProfile() {
        tvGreeting.text = getGreetingMessage()

        lifecycleScope.launch {
            val email = sessionManager.getUserSession()?.first
            if (email == null) {
                // --- CHƯA ĐĂNG NHẬP ---
                tvName.text = "Guest"
                profileImage.setImageResource(R.drawable.ic_user)

                // ⭐️ Ẩn thông tin user, HIỆN nút Đăng nhập
                layoutUserInfo.visibility = View.GONE
                logoutButton.visibility = View.GONE
                btnLogin.visibility = View.VISIBLE

                return@launch
            }

            // --- ĐÃ ĐĂNG NHẬP ---
            // ⭐️ HIỆN thông tin user, ẨN nút Đăng nhập
            layoutUserInfo.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            btnLogin.visibility = View.GONE

            val user = userService.getUserByEmail(email)
            if (user != null) {
                currentUser = user
                tvName.text = user.firstName ?: email // Hiển thị Tên

                // Tải Avatar
                user.avatarUrl?.let { pathOrUrl ->
                    val avatarFile = File(pathOrUrl)
                    val loadTarget: Any = if (avatarFile.exists()) {
                        avatarFile
                    } else {
                        pathOrUrl
                    }

                    Glide.with(this@HomeActivity)
                        .load(loadTarget)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(profileImage)

                } ?: run {
                    profileImage.setImageResource(R.drawable.avatar)
                }

            } else {
                tvName.text = "Guest (Error)"
                profileImage.setImageResource(R.drawable.avatar)
            }
        }
    }

    private fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> "Chào buổi sáng"
            in 12..17 -> "Chào buổi chiều"
            in 18..21 -> "Chào buổi tối"
            else -> "Khuya rồi, chào bạn"
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(displayedProductList) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            startActivity(intent)
        }
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)
        recyclerProducts.adapter = productAdapter
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val productsJob = launch {
                    val products = productService.getAllProducts()
                    Log.d("HomeActivity", "Đã tải ${products.size} sản phẩm")
                    allProductsList.clear()
                    allProductsList.addAll(products)
                }

                val brandsJob = launch {
                    val brands = brandService.getAllBrands()
                    Log.d("HomeActivity", "Đã tải ${brands.size} thương hiệu")
                    runOnUiThread {
                        setupBrandChips(brands)
                    }
                }

                productsJob.join()
                brandsJob.join()

                applyFilters()

            } catch (e: Exception) {
                Log.e("FirestoreError", "Lỗi load data", e)
                Toast.makeText(this@HomeActivity, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBrandChips(brands: List<Brand>) {
        chipGroupBrands.removeAllViews()
        val allChip = createFilterChip("Tất cả", "ALL_ID", true)
        chipGroupBrands.addView(allChip)
        for (brand in brands) {
            val brandChip = createFilterChip(brand.name, brand.id, false)
            chipGroupBrands.addView(brandChip)
        }
        chipGroupBrands.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == View.NO_ID) {
                allChip.isChecked = true
                return@setOnCheckedChangeListener
            }
            val selectedChip = group.findViewById<Chip>(checkedId)
            val selectedTag = selectedChip.tag as String
            currentBrandId = if (selectedTag == "ALL_ID") null else selectedTag
            applyFilters()
        }
    }

    private fun createFilterChip(name: String, tagId: String, isChecked: Boolean): Chip {
        return Chip(this).apply {
            text = name
            tag = tagId
            isCheckable = true
            this.isChecked = isChecked
        }
    }

    private fun applyFilters() {
        val searchQuery = etSearch.text.toString().trim().lowercase()

        val filteredByBrand = if (currentBrandId == null) {
            allProductsList
        } else {
            allProductsList.filter { it.brandId == currentBrandId }
        }

        val finalFilteredList = if (searchQuery.isEmpty()) {
            filteredByBrand
        } else {
            filteredByBrand.filter {
                it.name.lowercase().contains(searchQuery)
            }
        }

        displayedProductList.clear()
        displayedProductList.addAll(finalFilteredList)
        productAdapter.notifyDataSetChanged()

        if (displayedProductList.isEmpty()) {
            recyclerProducts.visibility = View.GONE
            tvEmptyView.visibility = View.VISIBLE
        } else {
            recyclerProducts.visibility = View.VISIBLE
            tvEmptyView.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener {
            lifecycleScope.launch {
                delay(300)
                applyFilters()
            }
        }

        // ⭐️ (THÊM) Listener cho nút Đăng nhập mới
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        btnWishlist.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java))
        }
        profileImage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            CustomBottomSheetDialog.show(
                context = this,
                title="Logout",
                message = "Are you sure you want to log out?",
                positiveText = "Yes, Logout",
                negativeText = "Cancel",
                onConfirm = {
                    onLogout()
                }
            )
        }
    }

    private fun onLogout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}