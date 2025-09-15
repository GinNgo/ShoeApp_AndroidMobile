package ui.product


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.shoesapp.R
import com.example.shoesapp.model.Product
import ui.home.HomeActivity

@Suppress("DEPRECATION")
class ProductDetailActivity : AppCompatActivity() {
    private var imgProduct: ImageView? = null
//    private var txtTitle: TextView? = null
//    private var txtSold: TextView? = null
//    private var txtRating: TextView? = null
//    private var txtDescription: TextView? = null
    private var txtQuantity: TextView? = null
    private var txtPrice: TextView? = null
    private var btnFavorite: ImageButton? = null
    private var btnMinus: ImageButton? = null
    private var btnPlus: ImageButton? = null
    private var btnAddToCart: Button? = null

    private var quantity = 1
    private val unitPrice = 750.00 // giá mặc định
    private var totalPrice = unitPrice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)
        val product = intent.getSerializableExtra("product") as? Product

        product?.let {
            findViewById<TextView>(R.id.txtTitle).text = it.name
            findViewById<TextView>(R.id.txtPrice).text = it.price
            findViewById<TextView>(R.id.txtRating).text = buildString {
                append("⭐")
                append(it.rating)
            }
            findViewById<TextView>(R.id.txtSold).text = it.sold
            findViewById<ImageView>(R.id.imgProduct).setImageResource(it.imageResId)
            findViewById<TextView>(R.id.txtDescription).text = it.description

        }

        // Ánh xạ view
        imgProduct = findViewById(R.id.imgProduct)
//        txtTitle = findViewById<TextView?>(R.id.txtTitle)
//        txtSold = findViewById<TextView?>(R.id.txtSold)
//        txtRating = findViewById<TextView?>(R.id.txtRating)
//        txtDescription = findViewById<TextView?>(R.id.txtDescription)
        txtQuantity = findViewById(R.id.txtQuantity)
        txtPrice = findViewById(R.id.txtPrice)

        btnFavorite = findViewById(R.id.btnFavorite)
        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        btnAddToCart = findViewById(R.id.btnAddToCart)

        // Gán giá trị ban đầu
        txtQuantity!!.text = quantity.toString()
        @SuppressLint("SetTextI18n")
        txtPrice!!.text = buildString {
            append("$")
            append(totalPrice)
        }

        // Xử lý tăng số lượng
        btnPlus!!.setOnClickListener { v: View? ->
            quantity++
            updateQuantity()
        }

        // Xử lý giảm số lượng
        btnMinus!!.setOnClickListener { v: View? ->
            if (quantity > 1) {
                quantity--
                updateQuantity()
            }
        }

        // Yêu thích (toggle icon)
        var isFavorite = false  // trạng thái ban đầu (chưa thích)

        btnFavorite!!.setOnClickListener {
            isFavorite = !isFavorite  // đảo trạng thái

            if (isFavorite) {
                btnFavorite!!.setImageResource(R.drawable.ic_favorite)   // icon trái tim đỏ
            } else {
                btnFavorite!!.setImageResource(R.drawable.ic_favorite_border) // icon trái tim rỗng
            }
        }

        // Thêm vào giỏ hàng
        btnAddToCart!!.setOnClickListener { v: View? -> }

        // Quay lại home
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
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
        txtQuantity!!.text = quantity.toString()
        totalPrice = unitPrice * quantity
        txtPrice!!.text = buildString {
            append("$")
            append(totalPrice)
        }
    }
}