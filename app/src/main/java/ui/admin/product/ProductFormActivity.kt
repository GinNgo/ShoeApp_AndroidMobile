package ui.admin.product

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.shoesapp.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import model.Product
import model.ProductImage
import service.CategoryService
import service.ProductService

class ProductFormActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtPrice: EditText
    private lateinit var edtQuantity: EditText
    private lateinit var edtMaterial: EditText
    private lateinit var edtBrand: EditText
    private lateinit var edtSizeChart: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAddImage: MaterialButton
    private lateinit var layoutImages: LinearLayout
    private lateinit var btnSave: MaterialButton
    private lateinit var btnPickColors: MaterialButton
    private lateinit var layoutSelectedColors: LinearLayout
    private lateinit var toolbar: MaterialToolbar

    private val selectedColors = mutableListOf<Product.ProductColor>()
    private val selectedImages = mutableListOf<ProductImage>()

    private val productService = ProductService()
    private val categoryService = CategoryService()
    private var currentProduct: Product? = null

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

        currentProduct = intent.getSerializableExtra("product") as? Product
        if (currentProduct != null) {
            fillFormWithProduct(currentProduct!!)
            toolbar.title = "Chỉnh sửa sản phẩm"
        } else {
            toolbar.title = "Thêm sản phẩm"
        }

        setupCategorySpinner()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarProductForm)
        edtName = findViewById(R.id.edtName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        edtQuantity = findViewById(R.id.edtQuantity)
        edtMaterial = findViewById(R.id.edtMaterial)
        edtBrand = findViewById(R.id.edtBrand)
        edtSizeChart = findViewById(R.id.edtSizeChart)
        spinnerCategory = findViewById(R.id.spinnerCategory)
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

        btnPickColors.setOnClickListener { showColorPickerDialog() }

        btnSave.setOnClickListener {
            val product = collectProductData()
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

    private fun fillFormWithProduct(product: Product) {
        edtName.setText(product.name)
        edtDescription.setText(product.description)
        edtPrice.setText(product.price.toString())
        edtQuantity.setText(product.stockQuantity.toString())
        edtMaterial.setText(product.material)
        edtBrand.setText(product.brand)
        edtSizeChart.setText(product.sizeChartUrl)

        // 💡 Đảm bảo load danh sách màu nếu có
        selectedColors.clear()
        android.util.Log.d("COLOR_DEBUG", "Show color: ${product}")

        product.colors?.forEach { color ->
            when (color) {
                is Map<*, *> -> {
                    val hex = color["hexCode"] as? String ?: "#000000"
                    val name = color["name"] as? String ?: "Không rõ"
                    val outOfStock = color["outOfStock"] as? Boolean ?: false
                    selectedColors.add(Product.ProductColor(hex, name, outOfStock))
                }
                is Product.ProductColor -> {
                    selectedColors.add(color)
                    Log.w("ProductForm","⚠️ Đã có màu: $color")
                }
                else -> {
                    Log.w("ProductForm", "⚠️ Không xác định kiểu màu: $color")
                }
            }
        }

        updateSelectedColorsUI()

        // 💡 Load danh sách ảnh
        selectedImages.clear()
        product.images?.let { selectedImages.addAll(it) }
        refreshImageUI()
    }

    private fun collectProductData(): Product {
        val category = spinnerCategory.selectedItem?.toString() ?: ""
        return Product(
            id = currentProduct?.id ?: "",
            name = edtName.text.toString(),
            description = edtDescription.text.toString(),
            price = edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
            stockQuantity = edtQuantity.text.toString().toIntOrNull() ?: 0,
            colors = selectedColors,
            material = edtMaterial.text.toString(),
            brand = edtBrand.text.toString(),
            sizeChartUrl = edtSizeChart.text.toString(),
            categoryId = category,
            images = selectedImages
        )
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

            // Hiển thị từ URL (Firebase) hoặc Drawable
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

    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            val categories = categoryService.getAllCategories()
            if (categories.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this@ProductFormActivity,
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter

                currentProduct?.let { p ->
                    val pos = categories.indexOfFirst { it.id == p.categoryId }
                    if (pos >= 0) spinnerCategory.setSelection(pos)
                }
            }
        }
    }

    private fun showColorPickerDialog() {
        val availableColors = listOf(
            Product.ProductColor("#FF0000", "Đỏ"),
            Product.ProductColor("#0000FF", "Xanh dương"),
            Product.ProductColor("#00FF00", "Xanh lá"),
            Product.ProductColor("#FFFF00", "Vàng"),
            Product.ProductColor("#000000", "Đen"),
            Product.ProductColor("#FFFFFF", "Trắng")
        )

        val colorNames = availableColors.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(availableColors.size) { i ->
            selectedColors.any { it.hexCode == availableColors[i].hexCode }
        }

        AlertDialog.Builder(this)
            .setTitle("Chọn màu cho sản phẩm")
            .setMultiChoiceItems(colorNames, checkedItems) { _, which, isChecked ->
                val color = availableColors[which]
                if (isChecked) selectedColors.add(color)
                else selectedColors.removeIf { it.hexCode == color.hexCode }
            }
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                updateSelectedColorsUI()
            }
            .show()
    }

    private fun updateSelectedColorsUI() {
        val colorContainer = findViewById<LinearLayout>(R.id.layoutSelectedColors)
        colorContainer.removeAllViews()

        if (selectedColors.isEmpty()) {
            // Hiển thị gợi ý nếu chưa có màu
            val tv = TextView(this).apply {
                text = "Chưa chọn màu nào"
                setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            colorContainer.addView(tv)
            return
        }

        selectedColors.forEachIndexed { index, color ->
            android.util.Log.d("COLOR_DEBUG", "Show color: ${color.name} - ${color.hexCode}")

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(android.graphics.Color.parseColor(color.hexCode))
                setStroke(3, android.graphics.Color.DKGRAY)
            }

            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    setMargins(12, 8, 12, 8)
                }
                background = drawable
                alpha = if (color.isOutOfStock) 0.35f else 1f

                setOnClickListener {
                    selectedColors[index] = color.copy(isOutOfStock = !color.isOutOfStock)
                    updateSelectedColorsUI()
                }

                setOnLongClickListener {
                    selectedColors.removeAt(index)
                    updateSelectedColorsUI()
                    true
                }
            }

            colorContainer.addView(colorView)
        }
    }

}
