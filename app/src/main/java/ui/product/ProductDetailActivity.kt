package ui.product

import adapter.FeedBackAdapter
import adapter.ImageSliderAdapter
import android.annotation.SuppressLint
import android.content.res.ColorStateList // ⭐️ Import
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.shoesapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import model.CartItem
import model.FeedBack
import model.Product
import model.ProductColor
import model.ProductImage
import model.ProductSize
import service.serviceImplement.CartService
import service.serviceImplement.FeedBackService
import service.serviceImplement.ProductService
import ui.BaseActivity
import java.text.NumberFormat
import java.util.Locale

@Suppress("DEPRECATION")
class ProductDetailActivity : BaseActivity() {

    // --- Views ---
    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtOriginalPrice: TextView
    private lateinit var txtSalePrice: TextView
    private lateinit var txtSold: TextView
    private lateinit var txtRating: TextView
    private lateinit var txtQuantity: TextView
    private lateinit var txtPrice: TextView
    private lateinit var txtStockInfo: TextView
    private lateinit var txtOutOfStockOverlay: TextView
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton
    private lateinit var btnAddToCart: Button
    private lateinit var btnBack: ImageButton
    private lateinit var viewPagerImageSlider: ViewPager2
    private lateinit var tabLayoutIndicator: TabLayout
    // --- Adapters ---
    private lateinit var imageAdapter: ImageSliderAdapter
    private lateinit var feedBackAdapter: FeedBackAdapter // ⭐️ (THÊM)
    private var feedbackList = mutableListOf<FeedBack>()
    private lateinit var chipGroupColor: ChipGroup
    private lateinit var chipGroupSize: ChipGroup
    private lateinit var recyclerReviews: RecyclerView // ⭐️ (THÊM)
    private lateinit var tvEmptyReviews: TextView // ⭐️ (THÊM)
    // --- Services ---
    private val cartService =  CartService()
    private val productService = ProductService()
    private val feedBackService = FeedBackService()
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // --- State (Trạng thái) ---
    private var currentProduct: Product? = null
    private var selectedColor: ProductColor? = null
    private var selectedSize: ProductSize? = null
    private var quantity = 1
    private var currentStock = 0
    private var unitPrice = 0.0
    private var totalPrice = 0.0


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        setupListeners()


        // ⭐️ (SỬA LỖI 1) Đã đổi key thành "product_id"
        val productId = intent.getStringExtra("product_id")
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "ID sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadProductFromDatabase(productId)
        loadFeedbacks(productId)
    }
    private fun loadFeedbacks(productId: String) {
        lifecycleScope.launch {
            val feedbacks = feedBackService.getFeedbacksForProduct(productId)
            feedbackList.clear()
            feedbackList.addAll(feedbacks)

            // Cập nhật UI
            setupFeedbackRecyclerView()
            updateRatingUI(feedbacks)
        }
    }
    private fun loadProductFromDatabase(productId: String) {
        lifecycleScope.launch {
            val product = productService.getProductById(productId)
            if (product == null) {
                Toast.makeText(this@ProductDetailActivity, "Không thể tải sản phẩm", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            currentProduct = product
            displayProductData(product)
        }
    }

    private fun initViews() {
        txtTitle = findViewById(R.id.txtTitle)
        txtDescription = findViewById(R.id.txtDescription)
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice)
        txtSalePrice = findViewById(R.id.txtSalePrice)
        txtSold = findViewById(R.id.txtSold)
        txtRating = findViewById(R.id.txtRating)
        txtQuantity = findViewById(R.id.txtQuantity)
        txtPrice = findViewById(R.id.txtPrice)
        txtStockInfo = findViewById(R.id.txtStockInfo)
        txtOutOfStockOverlay = findViewById(R.id.txtOutOfStockOverlay)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBack = findViewById(R.id.btnBack)
        viewPagerImageSlider = findViewById(R.id.viewPagerImageSlider)
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator)
        chipGroupColor = findViewById(R.id.chipGroupColor)
        chipGroupSize = findViewById(R.id.chipGroupSize)
        recyclerReviews = findViewById(R.id.recyclerReviews)
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews)
    }

    private fun displayProductData(product: Product) {
        txtTitle.text = product.name
        txtDescription.text = product.description
//        txtRating.text = "⭐ 4.7 (5,387 reviews)"
        txtSold.text = "Đã bán ${product.soldCount}"
        unitPrice = product.getDisplayPrice()
        updateTotalPrice()
        if (product.isOnSale()) {
            txtOriginalPrice.text = formatter.format(product.price)
            txtOriginalPrice.paintFlags = txtOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            txtOriginalPrice.visibility = View.VISIBLE
            txtSalePrice.text = formatter.format(product.salePrice!!)
        } else {
            txtOriginalPrice.visibility = View.GONE
            txtSalePrice.text = formatter.format(product.price)
        }
        setupImageSlider(product.images)
        if (product.getTotalStock() == 0) {
            txtOutOfStockOverlay.visibility = View.VISIBLE
            btnAddToCart.isEnabled = false
            btnAddToCart.text = "Hết hàng"
            findViewById<LinearLayout>(R.id.layoutQuantity).visibility = View.GONE
            txtStockInfo.text = "Sản phẩm đã hết hàng"
            return
        }
        setupColorChips(product.colors)
    }
    private fun setupFeedbackRecyclerView() {
        if (feedbackList.isEmpty()) {
            recyclerReviews.visibility = View.GONE
            tvEmptyReviews.visibility = View.VISIBLE
        } else {
            feedBackAdapter = FeedBackAdapter(feedbackList)
            recyclerReviews.adapter = feedBackAdapter
            recyclerReviews.visibility = View.VISIBLE
            tvEmptyReviews.visibility = View.GONE
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateRatingUI(feedbacks: List<FeedBack>) {
        if (feedbacks.isEmpty()) {
            txtRating.text = "⭐ – (0 đánh giá)"
        } else {
            val avgRating = feedbacks.map { it.rating }.average()
            val count = feedbacks.size
            txtRating.text = "⭐ ${"%.1f".format(avgRating)} ($count đánh giá)"
        }
    }
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        btnPlus.setOnClickListener {
            if (quantity < currentStock) {
                quantity++
                updateQuantityAndPrice()
            } else if (selectedSize == null) {
                Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Đã đạt số lượng tồn kho tối đa", Toast.LENGTH_SHORT).show()
            }
        }
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityAndPrice()
            }
        }
        var isFavorite = false
        btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            btnFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
        }

        // ⭐️⭐️ (CẬP NHẬT HOÀN TOÀN LOGIC NÀY) ⭐️⭐️
        btnAddToCart.setOnClickListener {
            if (selectedColor == null || selectedSize == null) {
                Toast.makeText(this, "Vui lòng chọn màu sắc và size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (quantity > currentStock) {
                Toast.makeText(this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val userId = getUserIdFromSession()
                if (userId == null) {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Bạn chưa đăng nhập",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                val primaryImageName = when {
                    // Ưu tiên 1: Trường primaryImageUrl (nếu có)
                    currentProduct!!.primaryImageUrl.isNotBlank() -> currentProduct!!.primaryImageUrl

                    // Ưu tiên 2: Ảnh có cờ isPrimary = true trong list images
                    else -> currentProduct!!.images.firstOrNull { it.isPrimary }?.imageUrl

                    // Ưu tiên 3: Ảnh đầu tiên trong list images
                        ?: currentProduct!!.images.firstOrNull()?.imageUrl

                        // Ưu tiên 4: Ảnh dự phòng
                        ?: "no_image"
                }
                // 1. Tạo đối tượng CartItem
                val newItem = CartItem(
                    userId = userId,
                    productId = currentProduct!!.id,
                    productName = currentProduct!!.name,
                    productImage = primaryImageName, // 👈 Lưu tên ảnh
                    selectedColor = selectedColor!!.name,
                    selectedSize = selectedSize!!.size,
                    quantity = quantity,
                    price = currentProduct!!.price,
                    salePrice = currentProduct!!.salePrice
                )

                // 2. Gọi service mới
                val success = cartService.addItemToCart(newItem)

                if (success) {
                    val toastMessage =
                        "Đã thêm: ${selectedColor!!.name} - Size ${selectedSize!!.size} (SL: $quantity)"
                    Toast.makeText(this@ProductDetailActivity, toastMessage, Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Lỗi khi thêm vào giỏ hàng",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun setupImageSlider(images: List<ProductImage>) {
        val displayImages = images.ifEmpty { listOf(ProductImage("no_image", true)) }

        imageAdapter = ImageSliderAdapter(this, displayImages)
        viewPagerImageSlider.adapter = imageAdapter

        TabLayoutMediator(tabLayoutIndicator, viewPagerImageSlider) { tab, position ->
            tab.text = null
            tab.icon = null
        }.attach()
    }

    private fun setupColorChips(colors: List<ProductColor>) {
        chipGroupColor.removeAllViews()
        var firstAvailableChip: Chip? = null
        colors.forEach { color ->
            val chip = createColorChip(color)
            chipGroupColor.addView(chip)
            if (firstAvailableChip == null && chip.isEnabled) {
                firstAvailableChip = chip
            }
        }
        firstAvailableChip?.performClick()
    }

    private fun createColorChip(color: ProductColor): Chip {
        val isColorOutOfStock = color.sizes.all { it.stockQuantity == 0 }
        val strokeColorStateList = ContextCompat.getColorStateList(this, R.color.color_chip_stroke_selector)

        val chip = Chip(this).apply {
            isCheckable = true
            isClickable = true
            isEnabled = !isColorOutOfStock
            tag = color
            val chipSize = (36 * resources.displayMetrics.density).toInt()
            chipMinHeight = chipSize.toFloat()
//            chipMinWidth = chipSize.toFloat()
            chipCornerRadius = (chipSize / 2).toFloat()
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(color.hexCode))
            chipStrokeWidth = 3f
            setChipStrokeColor(strokeColorStateList)
            if (isColorOutOfStock) {
                alpha = 0.4f
            }
        }

        chip.setOnClickListener {
            onColorSelected(it as Chip)
        }
        return chip
    }

    private fun onColorSelected(chip: Chip) {
        if (!chip.isChecked) return
        selectedColor = chip.tag as ProductColor
        updateSizeChips(selectedColor!!.sizes)
        selectedSize = null
        currentStock = 0
        quantity = 1
        updateQuantityAndPrice()
        txtStockInfo.text = "Vui lòng chọn size"
        btnAddToCart.isEnabled = false
    }

    private fun updateSizeChips(sizes: List<ProductSize>) {
        chipGroupSize.removeAllViews()
        sizes.forEach { size ->
            val chip = createSizeChip(size)
            chipGroupSize.addView(chip)
        }
    }

    // ⭐️ (ĐÂY LÀ PHIÊN BẢN ĐÚNG CỦA createSizeChip)
    private fun createSizeChip(size: ProductSize): Chip {
        val isSizeOutOfStock = size.stockQuantity == 0

        val bgStateList = ContextCompat.getColorStateList(this, R.color.size_chip_background_selector)
        val textStateList = ContextCompat.getColorStateList(this, R.color.size_chip_text_selector)

        val chip = Chip(this).apply {
            text = size.size
            tag = size
            isCheckable = true
            isClickable = true
            isEnabled = !isSizeOutOfStock
            setChipBackgroundColor(bgStateList)
            setTextColor(textStateList)
            chipStrokeWidth = 2f

            val strokeColor = if (isEnabled) Color.parseColor("#BDBDBD") else Color.parseColor("#E0E0E0")
            setChipStrokeColor(ColorStateList.valueOf(strokeColor))

            if (isSizeOutOfStock) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                onSizeSelected(buttonView.tag as ProductSize)
            }
        }
        return chip
    }

    // ⭐️ (SỬA LỖI 2) Hàm trùng lặp đã bị xóa

    private fun onSizeSelected(size: ProductSize) {
        selectedSize = size
        currentStock = size.stockQuantity
        quantity = 1
        updateQuantityAndPrice()
        txtStockInfo.text = "Chỉ còn $currentStock sản phẩm"
        btnAddToCart.isEnabled = true
    }

    @SuppressLint("SetTextI18n")
    private fun updateQuantityAndPrice() {
        txtQuantity.text = quantity.toString()
        updateTotalPrice()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalPrice() {
        totalPrice = unitPrice * quantity
        txtPrice.text = formatter.format(totalPrice)
    }
}