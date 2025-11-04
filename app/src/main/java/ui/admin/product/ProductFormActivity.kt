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
import model.Brand // ⭐️ (THÊM) Import Brand
import model.Category
import model.Product
import model.ProductColor
import model.ProductImage
import model.ProductSize
import service.serviceImplement.BrandService // ⭐️ (THÊM) Import BrandService
import service.serviceImplement.CategoryService
import service.serviceImplement.ProductService

class ProductFormActivity : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var edtName: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtPrice: TextInputEditText
    private lateinit var edtSalePrice: TextInputEditText // ⭐️ (THÊM)
    private lateinit var edtMaterial: TextInputEditText
    private lateinit var edtSizeChart: TextInputEditText

    private lateinit var btnSelectCategories: MaterialButton
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var spinnerBrand: AutoCompleteTextView // ⭐️ (THAY ĐỔI)

    private lateinit var btnAddImage: MaterialButton
    private lateinit var layoutImages: LinearLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var btnPickColors: MaterialButton
    private lateinit var layoutSelectedColors: LinearLayout
    private lateinit var toolbar: MaterialToolbar

    // --- Khai báo Data ---
    private val productService = ProductService()
    private val categoryService = CategoryService()
    private val brandService = BrandService() // ⭐️ (THÊM)
    private var currentProduct: Product? = null

    private var allCategoriesList = listOf<Category>()
    private var allBrandsList = listOf<Brand>() // ⭐️ (THÊM)
    private var selectedCategories = mutableListOf<Category>()
    private var selectedColorsAndSizes = mutableListOf<ProductColor>()
    private val selectedImages = mutableListOf<ProductImage>()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            addImage(result.data!!.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        initViews()
        setupListeners()
        loadInitialData()
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            // Tải song song Category và Brand
            val categoriesJob = launch { allCategoriesList = categoryService.getAllCategories() }
            val brandsJob = launch { allBrandsList = brandService.getAllBrands() } // ⭐️ (THÊM)

            // Chờ cả hai hoàn thành
            categoriesJob.join()
            brandsJob.join()

            // ⭐️ (THÊM) Cài đặt spinner cho Brand
            setupBrandSpinner()

            currentProduct = intent.getSerializableExtra("product") as? Product

            if (currentProduct != null) {
                toolbar.title = "Chỉnh sửa sản phẩm"
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
        edtSalePrice = findViewById(R.id.edtSalePrice) // ⭐️ (THÊM)
//        edtMaterial = findViewById(R.id.edtMaterial)
        spinnerBrand = findViewById(R.id.spinnerBrand) // ⭐️ (THAY ĐỔI)
//        edtSizeChart = findViewById(R.id.edtSizeChart)
        btnSelectCategories = findViewById(R.id.btnSelectCategories)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        btnAddImage = findViewById(R.id.btnAddImage)
        layoutImages = findViewById(R.id.layoutImages)
        btnSave = findViewById(R.id.btnSave)
        btnPickColors = findViewById(R.id.btnPickColors)
        layoutSelectedColors = findViewById(R.id.layoutSelectedColors)
    }

    private fun setupListeners() {
        // ... (Giữ nguyên các listener cho toolbar, btnAddImage, btnPickColors) ...
        toolbar.setNavigationOnClickListener { finish() }
        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }
        btnSelectCategories.setOnClickListener { showCategoryMultiSelectDialog() }
        btnPickColors.setOnClickListener { showColorPickerDialog() }

        btnSave.setOnClickListener {
            val product = collectProductData()
            if (product == null) return@setOnClickListener

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

    // ⭐️ (MỚI) Hàm cài đặt Brand Spinner
    private fun setupBrandSpinner() {
        if (allBrandsList.isNotEmpty()) {
            val brandNames = allBrandsList.map { it.name }
            val adapter = ArrayAdapter(
                this@ProductFormActivity,
                R.layout.list_item_dropdown, // Dùng chung layout
                brandNames
            )
            spinnerBrand.setAdapter(adapter)
        }
    }


    private fun fillFormWithProduct(product: Product) {
        edtName.setText(product.name)
        edtDescription.setText(product.description)
        edtPrice.setText(product.price.toString())
        edtSalePrice.setText(product.salePrice?.toString() ?: "") // ⭐️ (THÊM)
//        edtMaterial.setText(product.material)
//        edtSizeChart.setText(product.sizeChartUrl)

        // ⭐️ (SỬA) Hiển thị Brand đã chọn
        val selectedBrand = allBrandsList.find { it.id == product.brandId }
        spinnerBrand.setText(selectedBrand?.name ?: "", false)

        // ... (Giữ nguyên logic fill Category, Color, Image) ...
        selectedCategories.clear()
        product.categoryIds.forEach { catId ->
            allCategoriesList.find { it.id == catId }?.let { selectedCategories.add(it) }
        }
        updateCategoryChipsUI()

        selectedColorsAndSizes.clear()
        product.colors.forEach { colorData ->
            when (colorData) {
                is ProductColor -> {
                    selectedColorsAndSizes.add(colorData)
                }
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
                        selectedColorsAndSizes.add(ProductColor(hex, name, sizesList))
                    } catch (e: Exception) {
                        Log.e("ProductForm", "Lỗi parse màu từ Map: $e")
                    }
                }
            }
        }
        updateSelectedColorsUI()

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

        // ⭐️ (SỬA) Lấy Brand ID
        val selectedBrandName = spinnerBrand.text.toString()
        val selectedBrand = allBrandsList.find { it.name == selectedBrandName }
        if (selectedBrand == null) {
            Toast.makeText(this, "Vui lòng chọn thương hiệu", Toast.LENGTH_SHORT).show()
            return null
        }

        // ⭐️ (SỬA) Lấy Sale Price
        val price = edtPrice.text.toString().toDoubleOrNull() ?: 0.0
        val salePrice = edtSalePrice.text.toString().toDoubleOrNull() // 👈 Lấy giá trị

        // ... (Giữ nguyên logic lấy Category, Color) ...
        val categoryIds = selectedCategories.map { it.id }
        if (categoryIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show()
            return null
        }
        if (selectedColorsAndSizes.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 màu", Toast.LENGTH_SHORT).show()
            return null
        }

        return Product(
            id = currentProduct?.id ?: "",
            name = name,
            description = edtDescription.text.toString(),
            price = price,
            salePrice = salePrice, // ⭐️ (THÊM)
            colors = selectedColorsAndSizes,
//            material = edtMaterial.text.toString(),
            brandId = selectedBrand.id, // ⭐️ (THAY ĐỔI)
//            sizeChartUrl = edtSizeChart.text.toString(),
            categoryIds = categoryIds,
            images = selectedImages
        )
    }

    //
    // --- (TẤT CẢ CÁC HÀM BÊN DƯỚI GIỮ NGUYÊN) ---
    // (showCategoryMultiSelectDialog, updateCategoryChipsUI, showColorPickerDialog,
    //  updateSelectedColorsUI, showAddSizeDialog, updateSizeChipsUI,
    //  addImage, refreshImageUI, updateImageBorders)
    //
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

    private fun showColorPickerDialog() {
        val availableColors = listOf(
            ProductColor(hexCode = "#FF0000", name = "Đỏ"),
            ProductColor(hexCode = "#0000FF", name = "Xanh dương"),
            ProductColor(hexCode = "#00FF00", name = "Xanh lá"),
            ProductColor(hexCode = "#FFFF00", name = "Vàng"),
            ProductColor(hexCode = "#000000", name = "Đen"),
            ProductColor(hexCode = "#FFFFFF", name = "Trắng")
        )

        val colorNames = availableColors.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(availableColors.size) { i ->
            selectedColorsAndSizes.any { it.hexCode == availableColors[i].hexCode }
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn màu cho sản phẩm")
            .setMultiChoiceItems(colorNames, checkedItems) { _, which, isChecked ->
                val color = availableColors[which]
                if (isChecked) {
                    if (selectedColorsAndSizes.none { it.hexCode == color.hexCode }) {
                        selectedColorsAndSizes.add(ProductColor(color.hexCode, color.name, emptyList()))
                    }
                } else {
                    selectedColorsAndSizes.removeIf { it.hexCode == color.hexCode }
                }
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                updateSelectedColorsUI()
            }
            .show()
    }

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
            val inflater = LayoutInflater.from(this)
            val colorVariantView = inflater.inflate(R.layout.item_color_variant, layoutSelectedColors, false)
            val tvColorName = colorVariantView.findViewById<TextView>(R.id.tvColorName)
            val viewColorOval = colorVariantView.findViewById<View>(R.id.viewColorOval)
            val btnRemoveColor = colorVariantView.findViewById<ImageButton>(R.id.btnRemoveColor)
            val btnAddSize = colorVariantView.findViewById<MaterialButton>(R.id.btnAddSize)
            val chipGroupSizes = colorVariantView.findViewById<ChipGroup>(R.id.chipGroupSizes)

            tvColorName.text = "${color.name} (${color.hexCode})"

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(color.hexCode))
                setStroke(2, Color.DKGRAY)
            }
            viewColorOval.background = drawable

            btnRemoveColor.setOnClickListener {
                selectedColorsAndSizes.remove(color)
                updateSelectedColorsUI()
            }

            btnAddSize.setOnClickListener {
                showAddSizeDialog(color)
            }

            updateSizeChipsUI(chipGroupSizes, color)
            layoutSelectedColors.addView(colorVariantView)
        }
    }

    private fun showAddSizeDialog(colorToEdit: ProductColor) {
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

                val newSize = ProductSize(sizeName, stock)
                val currentSizes = colorToEdit.sizes.toMutableList()
                currentSizes.add(newSize)

                val index = selectedColorsAndSizes.indexOf(colorToEdit)
                if (index != -1) {
                    selectedColorsAndSizes[index] = colorToEdit.copy(sizes = currentSizes)
                }

                updateSelectedColorsUI()
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateSizeChipsUI(chipGroup: ChipGroup, color: ProductColor) {
        chipGroup.removeAllViews()
        color.sizes.forEach { size ->
            val chip = Chip(this).apply {
                text = "Size ${size.size}: ${size.stockQuantity} tồn"
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    val currentSizes = color.sizes.toMutableList()
                    currentSizes.remove(size)

                    val index = selectedColorsAndSizes.indexOf(color)
                    if (index != -1) {
                        selectedColorsAndSizes[index] = color.copy(sizes = currentSizes)
                    }
                    updateSelectedColorsUI()
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun addImage(uri: Uri) {
        val newImage = ProductImage(uri.toString(), isPrimary = selectedImages.isEmpty())
        selectedImages.add(newImage)
        refreshImageUI()
    }

    private fun refreshImageUI() {
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
        val border = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setStroke(if (isPrimary) 6 else 2,
                if (isPrimary) 0xFFFF9800.toInt() else 0xFF888888.toInt())
        }
        imageView.foreground = border
    }
}