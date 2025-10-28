package ui.admin.product

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.shoesapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import model.Category
import model.Product
import model.ProductImage
import model.ProductSize // ⭐️ (THÊM) Import model mới
import service.CategoryService
import service.ProductService

class ProductFormActivity : AppCompatActivity() {

    // --- Khai báo View (Đã cập nhật) ---
    private lateinit var edtName: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtPrice: TextInputEditText
    // ⭐️ (ĐÃ XÓA) private lateinit var edtQuantity: TextInputEditText
    private lateinit var edtMaterial: TextInputEditText
    private lateinit var edtBrand: TextInputEditText
    private lateinit var edtSizeChart: TextInputEditText

    // ⭐️ (ĐÃ SỬA) Thay Spinner bằng Nút và ChipGroup
    private lateinit var btnSelectCategories: MaterialButton
    private lateinit var chipGroupCategories: ChipGroup

    private lateinit var btnAddImage: MaterialButton
    private lateinit var layoutImages: LinearLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var btnPickColors: MaterialButton
    private lateinit var layoutSelectedColors: LinearLayout // ⭐️ Đây là (LinearLayout vertical)
    private lateinit var toolbar: MaterialToolbar

    // --- Khai báo Data ---
    private val productService = ProductService()
    private val categoryService = CategoryService()
    private var currentProduct: Product? = null

    // ⭐️ (ĐÃ SỬA) Quản lý data cho UI mới
    private var allCategoriesList = listOf<Category>() // Danh sách tất cả category từ Firestore
    private var selectedCategories = mutableListOf<Category>() // Danh sách category ĐÃ CHỌN
    private var selectedColorsAndSizes = mutableListOf<Product.ProductColor>() // ⭐️ Tên mới
    private val selectedImages = mutableListOf<ProductImage>()

    // --- Activity Result Launchers (Giữ nguyên) ---
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            addImage(result.data!!.data!!)
        }
    }

    // --- Vòng đời Activity ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        initViews()
        setupListeners()
        loadInitialData()
//        loadCategoriesData() // ⭐️ Tải category để chuẩn bị cho Dialog
//
//        currentProduct = intent.getSerializableExtra("product") as? Product
//        if (currentProduct != null) {
//            toolbar.title = "Chỉnh sửa sản phẩm"
//            fillFormWithProduct(currentProduct!!)
//        } else {
//            toolbar.title = "Thêm sản phẩm"
//        }
    }
    // --- Tải và Hiển thị Data (Đã viết lại) ---

    // ⭐️ (SỬA) Gộp logic tải Category và lấp đầy Form vào 1 hàm
    private fun loadInitialData() {
        lifecycleScope.launch { // Chạy trên Main thread

            // 1. Tải danh sách category (Hàm này nên dùng Dispatchers.IO bên trong Service)
            allCategoriesList = categoryService.getAllCategories()

            // 2. Lấy sản phẩm
            currentProduct = intent.getSerializableExtra("product") as? Product

            // 3. Cập nhật UI (vẫn trên Main thread)
            if (currentProduct != null) {
                toolbar.title = "Chỉnh sửa sản phẩm"
                // 4. ⭐️ GỌI FILLFORM Ở ĐÂY:
                // Bây giờ, allCategoriesList chắc chắn đã có dữ liệu
                fillFormWithProduct(currentProduct!!)
            } else {
                toolbar.title = "Thêm sản phẩm"
            }
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarProductForm)
        edtName = findViewById(R.id.edtName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        // ⭐️ (ĐÃ XÓA) edtQuantity
        edtMaterial = findViewById(R.id.edtMaterial)
        edtBrand = findViewById(R.id.edtBrand)
        edtSizeChart = findViewById(R.id.edtSizeChart)

        // ⭐️ (ĐÃ SỬA) Ánh xạ view cho Category
        btnSelectCategories = findViewById(R.id.btnSelectCategories)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)

        btnAddImage = findViewById(R.id.btnAddImage)
        layoutImages = findViewById(R.id.layoutImages)
        btnSave = findViewById(R.id.btnSave)
        btnPickColors = findViewById(R.id.btnPickColors)
        layoutSelectedColors = findViewById(R.id.layoutSelectedColors)
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        // ⭐️ (ĐÃ SỬA) Listener cho 2 nút mới
        btnSelectCategories.setOnClickListener { showCategoryMultiSelectDialog() }
        btnPickColors.setOnClickListener { showColorPickerDialog() } // ⭐️ Vẫn dùng dialog cũ để chọn màu

        btnSave.setOnClickListener {
            val product = collectProductData()
            if (product == null) return@setOnClickListener // ⭐️ Thêm kiểm tra validation

            lifecycleScope.launch {
                val success = if (currentProduct == null) {
                    productService.addProduct(product)
                } else {
                    productService.updateProduct(product.copy(id = currentProduct!!.id))
                }

                Toast.makeText(
                    this@ProductFormActivity,
                    if (success) "Lưu sản phẩm thành công!" else "Thao tác thất bại!",
                    Toast.LENGTH_SHORT
                ).show()

                if (success) finish()
            }
        }
    }

    // --- Tải và Hiển thị Data (Đã viết lại) ---

    private fun loadCategoriesData() {
        lifecycleScope.launch {
            allCategoriesList = categoryService.getAllCategories()
            // Tải xong, sẵn sàng để mở dialog
        }
    }

    private fun fillFormWithProduct(product: Product) {
        edtName.setText(product.name)
        edtDescription.setText(product.description)
        edtPrice.setText(product.price.toString())
        edtMaterial.setText(product.material)
        edtBrand.setText(product.brand)
        edtSizeChart.setText(product.sizeChartUrl)

        // ⭐️ (SỬA) Hiển thị Category đã chọn
        selectedCategories.clear()
        product.categoryIds.forEach { catId ->
            allCategoriesList.find { it.id == catId }?.let { selectedCategories.add(it) }
        }
        updateCategoryChipsUI()

        // ⭐️ (SỬA) Hiển thị Màu và Size đã chọn
        selectedColorsAndSizes.clear()
        // ⭐️ Cần xử lý data từ Firestore (có thể là Map) về data class
        product.colors.forEach { colorData ->
            when (colorData) {
                is Product.ProductColor -> {
                    // Nếu data đã là ProductColor (chứa List<ProductSize>)
                    selectedColorsAndSizes.add(colorData)
                }
                // ⭐️ Xử lý nếu data từ Firestore là Map (quan trọng)
                is Map<*, *> -> {
                    try {
                        val hex = colorData["hexCode"] as? String ?: "#000000"
                        val name = colorData["name"] as? String ?: "Không rõ"
                        val sizesList = (colorData["sizes"] as? List<Map<String, Any>> ?: emptyList())
                            .map { sizeMap ->
                                ProductSize(
                                    size = sizeMap["size"] as? String ?: "N/A",
                                    stockQuantity = (sizeMap["stockQuantity"] as? Long)?.toInt() ?: 0
                                )
                            }
                        selectedColorsAndSizes.add(Product.ProductColor(hex, name, sizesList))
                    } catch (e: Exception) {
                        Log.e("ProductForm", "Lỗi parse màu từ Map: $e")
                    }
                }
            }
        }
        updateSelectedColorsUI() // ⭐️ Hàm này đã được viết lại hoàn toàn

        // Hiển thị ảnh (Giữ nguyên)
        selectedImages.clear()
        product.images?.let { selectedImages.addAll(it) }
        refreshImageUI()
    }

    private fun collectProductData(): Product? {
        val name = edtName.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show()
            return null
        }

        // ⭐️ (SỬA) Lấy danh sách ID category
        val categoryIds = selectedCategories.map { it.id }
        if (categoryIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show()
            return null
        }

        // ⭐️ (SỬA) Lấy danh sách màu và size
        if (selectedColorsAndSizes.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 màu", Toast.LENGTH_SHORT).show()
            return null
        }

        return Product(
            id = currentProduct?.id ?: "",
            name = name,
            description = edtDescription.text.toString(),
            price = edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
            // ⭐️ (ĐÃ XÓA) stockQuantity
            colors = selectedColorsAndSizes, // ⭐️ Data mới
            material = edtMaterial.text.toString(),
            brand = edtBrand.text.toString(),
            sizeChartUrl = edtSizeChart.text.toString(),
            categoryIds = categoryIds, // ⭐️ Data mới
            images = selectedImages
        )
    }

    // --- Logic Quản lý Category (Mới) ---

    private fun showCategoryMultiSelectDialog() {
        val categoryNames = allCategoriesList.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(allCategoriesList.size) { i ->
            selectedCategories.any { it.id == allCategoriesList[i].id }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Chọn danh mục")
            .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                val category = allCategoriesList[which]
                if (isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.removeIf { it.id == category.id }
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                updateCategoryChipsUI()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateCategoryChipsUI() {
        chipGroupCategories.removeAllViews()
        selectedCategories.forEach { category ->
            val chip = Chip(this).apply {
                text = category.name
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    selectedCategories.remove(category)
                    updateCategoryChipsUI()
                }
            }
            chipGroupCategories.addView(chip)
        }
    }

    // --- Logic Quản lý Màu & Size (Viết lại hoàn toàn) ---

    private fun showColorPickerDialog() {
        // 1. Định nghĩa các màu cơ bản
        val availableColors = listOf(
            Product.ProductColor(hexCode = "#FF0000", name = "Đỏ"),
            Product.ProductColor(hexCode = "#0000FF", name = "Xanh dương"),
            Product.ProductColor(hexCode = "#00FF00", name = "Xanh lá"),
            Product.ProductColor(hexCode = "#FFFF00", name = "Vàng"),
            Product.ProductColor(hexCode = "#000000", name = "Đen"),
            Product.ProductColor(hexCode = "#FFFFFF", name = "Trắng")
            // Bạn có thể thêm nhiều màu hơn
        )

        val colorNames = availableColors.map { it.name }.toTypedArray()
        // 2. Kiểm tra xem màu nào đã được chọn (chỉ để tick)
        val checkedItems = BooleanArray(availableColors.size) { i ->
            selectedColorsAndSizes.any { it.hexCode == availableColors[i].hexCode }
        }

        // 3. Hiển thị dialog chọn MÀU (chưa chọn size)
        AlertDialog.Builder(this)
            .setTitle("Chọn màu cho sản phẩm")
            .setMultiChoiceItems(colorNames, checkedItems) { _, which, isChecked ->
                val color = availableColors[which]
                if (isChecked) {
                    // Nếu thêm màu mới, tạo 1 đối tượng mới với danh sách size rỗng
                    if (selectedColorsAndSizes.none { it.hexCode == color.hexCode }) {
                        selectedColorsAndSizes.add(Product.ProductColor(color.hexCode, color.name, emptyList()))
                    }
                } else {
                    // Nếu bỏ chọn, xóa màu khỏi danh sách
                    selectedColorsAndSizes.removeIf { it.hexCode == color.hexCode }
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                updateSelectedColorsUI() // ⭐️ Cập nhật UI hiển thị các card quản lý size
            }
            .show()
    }

    // ⭐️ (SỬA) Hàm này giờ sẽ inflate layout item_color_variant
    private fun updateSelectedColorsUI() {
        layoutSelectedColors.removeAllViews()

        if (selectedColorsAndSizes.isEmpty()) {
            val tv = TextView(this).apply {
                text = "Chưa chọn màu nào"
                setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            layoutSelectedColors.addView(tv)
            return
        }

        selectedColorsAndSizes.forEach { color ->
            // 1. Inflate layout item_color_variant.xml
            val inflater = LayoutInflater.from(this)
            val colorVariantView = inflater.inflate(R.layout.item_color_variant, layoutSelectedColors, false)

            // 2. Ánh xạ các view bên trong item
            val tvColorName = colorVariantView.findViewById<TextView>(R.id.tvColorName)
            val viewColorOval = colorVariantView.findViewById<View>(R.id.viewColorOval)
            val btnRemoveColor = colorVariantView.findViewById<ImageButton>(R.id.btnRemoveColor)
            val btnAddSize = colorVariantView.findViewById<MaterialButton>(R.id.btnAddSize)
            val chipGroupSizes = colorVariantView.findViewById<ChipGroup>(R.id.chipGroupSizes)

            // 3. Set data
            tvColorName.text = "${color.name} (${color.hexCode})"

            // 3.1. Set màu cho hình tròn
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(color.hexCode))
                setStroke(2, Color.DKGRAY)
            }
            viewColorOval.background = drawable

            // 4. Set Listeners
            btnRemoveColor.setOnClickListener {
                selectedColorsAndSizes.remove(color)
                updateSelectedColorsUI() // Tải lại toàn bộ UI
            }

            btnAddSize.setOnClickListener {
                showAddSizeDialog(color) // ⭐️ Hiển thị dialog thêm size cho MÀU NÀY
            }

            // 5. Hiển thị các Size đã có của màu này (dạng Chip)
            updateSizeChipsUI(chipGroupSizes, color)

            // 6. Thêm card này vào LinearLayout
            layoutSelectedColors.addView(colorVariantView)
        }
    }

    // ⭐️ (MỚI) Hiển thị dialog để thêm size/tồn kho
    private fun showAddSizeDialog(colorToEdit: Product.ProductColor) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_add_size, null)
        val edtSizeName = dialogView.findViewById<TextInputEditText>(R.id.edtDialogSizeName)
        val edtStock = dialogView.findViewById<TextInputEditText>(R.id.edtDialogStock)

        MaterialAlertDialogBuilder(this)
            .setTitle("Thêm Size cho màu ${colorToEdit.name}")
            .setView(dialogView)
            .setPositiveButton("Thêm") { dialog, _ ->
                val sizeName = edtSizeName.text.toString().trim()
                val stock = edtStock.text.toString().toIntOrNull() ?: 0

                if (sizeName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên size", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // 1. Tạo đối tượng ProductSize
                val newSize = ProductSize(sizeName, stock)

                // 2. Cập nhật danh sách sizes bên trong đối tượng ProductColor
                // ⭐️ Đây là phần magic: vì colorToEdit là 1 object reference,
                // việc cập nhật nó cũng là cập nhật trong list selectedColorsAndSizes
                val currentSizes = colorToEdit.sizes.toMutableList()
                currentSizes.add(newSize)

                // 3. Tìm index của màu và thay thế nó bằng 1 bản copy mới
                val index = selectedColorsAndSizes.indexOf(colorToEdit)
                if (index != -1) {
                    selectedColorsAndSizes[index] = colorToEdit.copy(sizes = currentSizes)
                }

                // 4. Cập nhật lại UI
                updateSelectedColorsUI()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // ⭐️ (MỚI) Cập nhật các chip size cho 1 màu cụ thể
    private fun updateSizeChipsUI(chipGroup: ChipGroup, color: Product.ProductColor) {
        chipGroup.removeAllViews()
        color.sizes.forEach { size ->
            val chip = Chip(this).apply {
                text = "Size ${size.size}: ${size.stockQuantity} tồn"
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    // Xóa size này khỏi list
                    val currentSizes = color.sizes.toMutableList()
                    currentSizes.remove(size)

                    // Cập nhật lại object color
                    val index = selectedColorsAndSizes.indexOf(color)
                    if (index != -1) {
                        selectedColorsAndSizes[index] = color.copy(sizes = currentSizes)
                    }
                    // Tải lại toàn bộ UI
                    updateSelectedColorsUI()
                }
            }
            chipGroup.addView(chip)
        }
    }


    // --- Logic Quản lý Ảnh (Giữ nguyên) ---
    private fun addImage(uri: Uri) {
        // (Giữ nguyên code)
        val newImage = ProductImage(uri.toString(), isPrimary = selectedImages.isEmpty())
        selectedImages.add(newImage)
        refreshImageUI()
    }

    private fun refreshImageUI() {
        // (Giữ nguyên code)
        layoutImages.removeAllViews()
        selectedImages.forEach { img ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            if (img.imageUrl.startsWith("http") || img.imageUrl.startsWith("content")) {
                Glide.with(this).load(img.imageUrl).into(imageView)
            } else {
                val resId = resources.getIdentifier(img.imageUrl, "drawable", packageName)
                if (resId != 0) imageView.setImageResource(resId)
                else imageView.setImageResource(R.drawable.no_image)
            }
            updateImageBorders(imageView, img.isPrimary)
            imageView.setOnClickListener {
                selectedImages.replaceAll {
                    it.copy(isPrimary = it.imageUrl == img.imageUrl)
                }
                refreshImageUI()
                Toast.makeText(this, "Đặt làm hình chính", Toast.LENGTH_SHORT).show()
            }
            imageView.setOnLongClickListener {
                selectedImages.remove(img)
                refreshImageUI()
                true
            }
            layoutImages.addView(imageView)
        }
    }

    private fun updateImageBorders(imageView: ImageView, isPrimary: Boolean) {
        // (Giữ nguyên code)
        val border = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setStroke(if (isPrimary) 6 else 2,
                if (isPrimary) 0xFFFF9800.toInt() else 0xFF888888.toInt())
        }
        imageView.foreground = border
    }
}