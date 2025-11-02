package ui.product

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import model.Product
import service.CartServiceImpl
import ui.BaseActivity
import ui.home.HomeActivity

@Suppress("DEPRECATION")
class ProductDetailActivity : BaseActivity() {

    private lateinit var imgProduct: ImageView
    private lateinit var txtQuantity: TextView
    private lateinit var txtPrice: TextView
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnMinus: ImageButton
    private lateinit var btnPlus: ImageButton
    private lateinit var btnAddToCart: Button
    private lateinit var cartService: CartServiceImpl


    private var quantity = 1
    private var unitPrice = 0.0
    private var totalPrice = 0.0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Ánh xạ view
        imgProduct = findViewById(R.id.imgProduct)
        txtQuantity = findViewById(R.id.txtQuantity)
        txtPrice = findViewById(R.id.txtPrice)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        cartService = CartServiceImpl()

        // Nhận product từ Intent
        val product = intent.getSerializableExtra("product") as? Product
        product?.let { p ->
            findViewById<TextView>(R.id.txtTitle).text = p.name
            findViewById<TextView>(R.id.txtDescription).text = p.description
            findViewById<TextView>(R.id.txtPrice).text = "$${p.price}"

            // Gán hình ảnh
            val resId = p.getPrimaryImageResId(this)
            if (resId != 0) {
                imgProduct.setImageResource(resId)
            } else {
                imgProduct.setImageResource(R.drawable.no_image)
            }

            // Gán giá mặc định
            unitPrice = p.price
            totalPrice = p.price
        }

        // Giá trị ban đầu
        txtQuantity.text = quantity.toString()
        txtPrice.text = "$$totalPrice"

        // Tăng số lượng
        btnPlus.setOnClickListener {
            quantity++
            updateQuantity()
        }

        // Giảm số lượng
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantity()
            }
        }

        // Toggle yêu thích
        var isFavorite = false
        btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            btnFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
        }

        // Thêm vào giỏ hàng
        btnAddToCart.setOnClickListener {
            lifecycleScope.launch {
                val userId = getUserIdFromSession()

                if (userId == null) {
                    Toast.makeText(this@ProductDetailActivity, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val productId = product?.id;
                if (productId == null) {
                    Toast.makeText(this@ProductDetailActivity, "Sản phẩm không tồn tại", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                Log.i("CartActivity", "Product: $product")

                // 3️⃣ Thêm sản phẩm vào giỏ hàng
                cartService.addProductToCart(userId, productId, quantity)

                // 4️⃣ Thông báo cho người dùng
                Toast.makeText(this@ProductDetailActivity, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        }

        // Nút quay lại Home
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Xử lý chọn size
        val btnSize39 = findViewById<TextView>(R.id.btnSize39)
        val btnSize40 = findViewById<TextView>(R.id.btnSize40)
        val btnSize41 = findViewById<TextView>(R.id.btnSize41)
        val btnSize42 = findViewById<TextView>(R.id.btnSize42)

        val sizeViews = listOf(btnSize39, btnSize40, btnSize41, btnSize42)
        btnSize40.isSelected = true
        btnSize40.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        sizeViews.forEach { view ->
            view.setOnClickListener {
                sizeViews.forEach {
                    it.isSelected = false
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                view.isSelected = true
                view.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateQuantity() {
        txtQuantity.text = quantity.toString()
        totalPrice = unitPrice * quantity
        txtPrice.text = "$$totalPrice"
    }
}
