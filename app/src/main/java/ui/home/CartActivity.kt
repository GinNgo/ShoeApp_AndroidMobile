package ui.cart

import adapter.CartAdapter
import android.content.Intent // ⭐️ (THÊM)
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import com.google.firebase.Timestamp // ⭐️ (THÊM)
import kotlinx.coroutines.launch
import model.CartItem
import model.CustomBottomSheetDialog
import model.Order
import model.OrderItem
import model.OrderStatus
import service.IOrderService
import service.serviceImplement.CartService
import service.serviceImplement.OrderService
import service.serviceImplement.UserService
import ui.BaseActivity
import ui.checkout.CheckoutActivity // ⭐️ (THÊM)
import ui.home.HomeActivity // ⭐️ (THÊM)
import ui.home.OrderActivity // ⭐️ (THÊM)
import java.text.NumberFormat
import java.util.Locale

class CartActivity : BaseActivity() {

    // --- Views ---
    private lateinit var recyclerCart: RecyclerView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var checkoutContainer: LinearLayout

    // --- Services ---
    private val cartService = CartService()
    private val orderServiceImpl: IOrderService = OrderService()
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // --- Adapter & Data ---
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initViews()
        setupRecyclerView()
        setupListeners()
        handleNavigation(R.id.nav_cart)
    }

    /**
     * Tải lại giỏ hàng mỗi khi quay lại màn hình
     */
    override fun onResume() {
        super.onResume()
        loadCartData()
    }

    private fun initViews() {
        recyclerCart = findViewById(R.id.recyclerCart)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnCheckout = findViewById(R.id.btnCheckout)
        emptyStateLayout = findViewById(R.id.empty_state_layout)
        checkoutContainer = findViewById(R.id.checkoutContainer)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            context = this,
            cartItems = cartItems,
            onQuantityChange = { item, newQuantity ->
                updateItemQuantity(item, newQuantity)
            },
            onDelete = { item ->
                showDeleteConfirmDialog(item)
            }
        )
        recyclerCart.adapter = cartAdapter
    }

    private fun setupListeners() {
        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentUserId == null) {
                Toast.makeText(this, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chuyển sang CheckoutActivity
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("cart_items", ArrayList(cartItems))
            startActivity(intent)
        }
    }

    /**
     * ⭐️ (SỬA) Cập nhật logic kiểm tra đăng nhập
     */
    private fun loadCartData() {
        lifecycleScope.launch {
            if (currentUserId == null) {
                currentUserId = getUserIdFromSession()
            }

            // ⭐️ Nếu sau khi kiểm tra mà vẫn null -> Chuyển về Home
            if (currentUserId == null) {
                Toast.makeText(this@CartActivity, "Bạn chưa đăng nhập. Đang chuyển về Trang chủ...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@CartActivity, HomeActivity::class.java)
                // Cờ này đảm bảo HomeActivity là Task gốc mới, xóa CartActivity khỏi stack
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Đóng CartActivity
                return@launch
            }

            // Nếu đã đăng nhập, tải giỏ hàng như bình thường
            val items = cartService.getAllItems(currentUserId!!)
            cartItems.clear()
            cartItems.addAll(items)
            cartAdapter.notifyDataSetChanged()

            updateTotalPrice()
            toggleEmptyState(items.isEmpty())
        }
    }

    private fun updateItemQuantity(item: CartItem, newQuantity: Int) {
        lifecycleScope.launch {
            val success = cartService.updateItemQuantity(currentUserId!!, item.id, newQuantity)
            if (success) {
                loadCartData()
            } else {
                Toast.makeText(this@CartActivity, "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeItem(item: CartItem) {
        lifecycleScope.launch {
            val success = cartService.removeItemFromCart(currentUserId!!, item.id)
            if (success) {
                loadCartData()
            }
        }
    }

    private fun showDeleteConfirmDialog(item: CartItem) {
        CustomBottomSheetDialog.show(
            context = this,
            title = "Xóa sản phẩm?",
            message = "Bạn có chắc muốn xóa ${item.productName} khỏi giỏ hàng?",
            positiveText = "Xóa",
            negativeText = "Hủy",
            onConfirm = {
                removeItem(item)
            }
        )
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.getTotalPrice() }
        tvTotalPrice.text = formatter.format(total)
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerCart.visibility = View.GONE
            checkoutContainer.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerCart.visibility = View.VISIBLE
            checkoutContainer.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }

}