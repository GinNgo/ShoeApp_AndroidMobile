package ui.home

import adapter.GridCartAdapter
import android.os.Bundle
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import model.CartItem
import model.CustomBottomSheetDialog
import service.CartServiceImpl
import service.ProductService
import ui.BaseActivity

class CartActivity : BaseActivity() {

    private lateinit var cartItems: MutableList<CartItem>
    private lateinit var gridAdapter: GridCartAdapter
    private lateinit var productService: ProductService
    private lateinit var cartService: CartServiceImpl
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cart)

        // ✅ Khởi tạo
        cartItems = mutableListOf()
        productService = ProductService()
        cartService = CartServiceImpl()

        val gridView = findViewById<GridView>(R.id.grid_view)

        // ⚡ Adapter với callback tăng/giảm quantity
        gridAdapter = GridCartAdapter(
            this, cartItems,
            onQuantityChanged = { cartItem, delta ->
                userId?.let { uid ->
                    lifecycleScope.launch {
                        if (delta > 0) {
                            cartService.addProductToCart(uid, cartItem.product.id)
                        } else {
                            cartService.removeProductFromCart(uid, cartItem.product.id)
                        }

                        // Cập nhật UI
                        val index = cartItems.indexOf(cartItem)
                        if (index != -1) {
                            val newQty = cartItems[index].quantity + delta
                            if (newQty <= 0) cartItems.removeAt(index)
                            else cartItems[index] = cartItems[index].copy(quantity = newQty)
                            gridAdapter.notifyDataSetChanged()
                            updateTotalPrice()
                        }
                    }
                }
            },
            onDeleteItem = { cartItem ->
                CustomBottomSheetDialog.show(
                    context = this,
                    title="Remove From Cart :${cartItem.product.name}?",
                    message = "Are you sure you want to remove item?",
                    positiveText = "Yes, Remove",
                    negativeText = "Cancel",
                    onConfirm = {
                        lifecycleScope.launch {
                            cartService.removeProductFromCart(userId!!, cartItem.product.id)
                            cartItems.remove(cartItem)
                            gridAdapter.notifyDataSetChanged()
                            updateTotalPrice()
                        }
                    }
                )
            }
        )

        gridView.adapter = gridAdapter

        // 🔹 Load cart từ Firestore
        lifecycleScope.launch {
            userId = getUserIdFromSession()
            if (userId == null) {
                Toast.makeText(this@CartActivity, "Chưa đăng nhập", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            val cart = cartService.getCartByUserId(userId!!)
            if (cart == null || cart.products.isEmpty()) {
                Toast.makeText(this@CartActivity, "Giỏ hàng trống", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            // Chuyển Map<productId, quantity> → List<CartItem>
            val items = mutableListOf<CartItem>()
            for ((productId, qty) in cart.products) {
                val product = productService.getProductById(productId)
                if (product != null) {
                    items.add(CartItem(product, qty))
                }
            }

            cartItems.clear()
            cartItems.addAll(items)
            gridAdapter.notifyDataSetChanged()
            updateTotalPrice()
        }

        handleNavigation(R.id.nav_cart)
    }

    private fun updateTotalPrice() {
        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
        val total = cartItems.sumOf { it.product.price * it.quantity }
        tvTotalPrice.text = "$total $"
    }
}
