package ui.checkout

import adapter.CheckoutProductAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import model.Address
import model.CartItem
import model.CustomBottomSheetDialog
import model.DiscountType
import model.Order
import model.OrderItem
import model.OrderStatus
import model.Voucher
import service.IOrderService
import service.serviceImplement.AddressService
import service.serviceImplement.CartService
import service.serviceImplement.OrderService
import service.serviceImplement.ProductService
import service.serviceImplement.UserService
import service.serviceImplement.VoucherService
import ui.BaseActivity
import ui.address.AddressListActivity
import ui.home.OrderActivity
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class CheckoutActivity : BaseActivity() {

    // --- Services ---
    private val addressService = AddressService()
    private val orderServiceImpl: IOrderService = OrderService()
    private val cartService = CartService()
    private val userService = UserService()
    private val productService = ProductService()
    private val voucherService = VoucherService()

    // --- Views ---
    private lateinit var tvAddressFullName: TextView
    private lateinit var tvAddressDetails: TextView
    private lateinit var tvSelectAddressHint: TextView
    private lateinit var btnChangeAddress: Button
    private lateinit var recyclerCheckoutProducts: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPlaceOrder: Button
    private lateinit var rbCOD: RadioButton
    private lateinit var rbPayPal: RadioButton
    private lateinit var edtVoucher: TextInputEditText
    private lateinit var btnApplyVoucher: Button
    private lateinit var layoutProductDiscount: LinearLayout // ⭐️ (THÊM)
    private lateinit var tvProductDiscountAmount: TextView // ⭐️ (THÊM)
    private lateinit var layoutDiscount: LinearLayout
    private lateinit var tvDiscountAmount: TextView
    private lateinit var tvTotalSavings: TextView // ⭐️ (THÊM)

    // --- Data ---
    private var cartItems = arrayListOf<CartItem>()
    private var selectedAddress: Address? = null
    private var currentUserId: String? = null
    private var subtotal: Double = 0.0 // ⭐️ Tổng giá GỐC
    private var totalAmount: Double = 0.0 // ⭐️ Tổng cuối cùng
    private var productDiscount: Double = 0.0 // ⭐️ Giảm giá từ sản phẩm
    private var voucherDiscount: Double = 0.0 // ⭐️ Giảm giá từ voucher
    private var appliedVoucher: Voucher? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        cartItems = intent.getSerializableExtra("cart_items") as? ArrayList<CartItem> ?: arrayListOf()
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners()
//        setupProductSummary() // ⭐️ Sẽ tính toán giá
        loadInitialData()
    }

    private fun initViews() {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarCheckout)
            .setNavigationOnClickListener { finish() }

        tvAddressFullName = findViewById(R.id.tvAddressFullName)
        tvAddressDetails = findViewById(R.id.tvAddressDetails)
        tvSelectAddressHint = findViewById(R.id.tvSelectAddressHint)
        btnChangeAddress = findViewById(R.id.btnChangeAddress)
        recyclerCheckoutProducts = findViewById(R.id.recyclerCheckoutProducts)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        rbCOD = findViewById(R.id.rbCOD)
        rbPayPal = findViewById(R.id.rbPayPal)

        edtVoucher = findViewById(R.id.edtVoucher)
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher)

        // ⭐️ Ánh xạ các view tổng kết
        layoutProductDiscount = findViewById(R.id.layoutProductDiscount)
        tvProductDiscountAmount = findViewById(R.id.tvProductDiscountAmount)
        layoutDiscount = findViewById(R.id.layoutDiscount)
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount)
        tvTotalSavings = findViewById(R.id.tvTotalSavings)
    }

    /**
     * ⭐️ (SỬA) Cập nhật logic listener cho voucher
     */
    private fun setupListeners() {
        btnChangeAddress.setOnClickListener {
            val intent = Intent(this, AddressListActivity::class.java)
            startActivity(intent)
        }

        btnPlaceOrder.setOnClickListener {
            handlePlaceOrder()
        }

        btnApplyVoucher.setOnClickListener {
            if (appliedVoucher != null) {
                resetVoucher()
                Toast.makeText(this, "Đã gỡ voucher", Toast.LENGTH_SHORT).show()
            }
            else {
                val code = edtVoucher.text.toString().trim().uppercase()
                if (code.isNotEmpty()) {
                    applyVoucher(code)
                } else {
                    Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * ⭐️ (SỬA) Tính toán Tạm tính (giá gốc) và Giảm giá (sản phẩm)
     */
    private fun setupProductSummary() {
        val adapter = CheckoutProductAdapter(this, cartItems)
        recyclerCheckoutProducts.adapter = adapter

        // Tạm tính là TỔNG GIÁ GỐC
        subtotal = cartItems.sumOf { it.price * it.quantity }

        // Giảm giá sản phẩm là TỔNG TIẾT KIỆM từ sale
        productDiscount = cartItems.sumOf { it.getProductDiscount() }

        updateTotalAmount()
    }

    /**
     * ⭐️ (SỬA) Cập nhật lại toàn bộ hàm này
     */
    private fun updateTotalAmount() {
        // 1. Tính toán giảm giá Voucher
        if (appliedVoucher != null) {
            val (isValid, discount) = calculateVoucherDiscount(appliedVoucher!!, subtotal - productDiscount)
            if (isValid) {
                voucherDiscount = discount
            } else {
                // Voucher không còn hợp lệ (ví dụ: tổng tiền giảm xuống dưới mức tối thiểu)
                resetVoucher()
                Toast.makeText(this, "Voucher không còn hợp lệ", Toast.LENGTH_SHORT).show()
            }
        } else {
            voucherDiscount = 0.0
        }

        // 2. Tính tổng cuối cùng
        totalAmount = subtotal - productDiscount - voucherDiscount
        if (totalAmount < 0) totalAmount = 0.0

        // 3. Cập nhật UI Tạm tính
        tvSubtotal.text = formatter.format(subtotal)

        // 4. Cập nhật UI Giảm giá Sản phẩm
        if (productDiscount > 0) {
            tvProductDiscountAmount.text = "-${formatter.format(productDiscount)}"
            layoutProductDiscount.visibility = View.VISIBLE
        } else {
            layoutProductDiscount.visibility = View.GONE
        }

        // 5. Cập nhật UI Giảm giá Voucher
        if (voucherDiscount > 0) {
            tvDiscountAmount.text = "-${formatter.format(voucherDiscount)}"
            layoutDiscount.visibility = View.VISIBLE
        } else {
            layoutDiscount.visibility = View.GONE
        }

        // 6. ⭐️ (MỚI) Cập nhật UI Tổng tiết kiệm
        val totalSavings = productDiscount + voucherDiscount
        if (totalSavings > 0) {
            tvTotalSavings.text = "Bạn đã tiết kiệm: ${formatter.format(totalSavings)}"
            tvTotalSavings.visibility = View.VISIBLE
        } else {
            tvTotalSavings.visibility = View.GONE
        }

        // 7. Cập nhật UI Tổng cộng
        tvTotalAmount.text = "Tổng cộng: ${formatter.format(totalAmount)}"
    }

    // ... (loadInitialData, loadDefaultAddress, onResume giữ nguyên) ...
    /**
     * ⭐️ (SỬA) Cập nhật hàm này
     */
    private fun loadInitialData() {
        lifecycleScope.launch {
            if (currentUserId == null) {
                currentUserId = getUserIdFromSession()
            }
            if (currentUserId == null) {
                Toast.makeText(this@CheckoutActivity, "Lỗi: Chưa đăng nhập", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            // 1. Tải địa chỉ (và chờ)
            loadDefaultAddress()

            // 2. ⭐️ SAU KHI tải địa chỉ xong, MỚI cài đặt tóm tắt sản phẩm
            //    (Vì lúc này subtotal và productDiscount mới được tính)
            setupProductSummary()
        }
    }

    /**
     * ⭐️ (SỬA) Hàm này giờ là 'suspend'
     * Hàm này sẽ được gọi cả trong onCreate và onResume
     */
    private suspend fun loadDefaultAddress() {
        // ⭐️ (SỬA) Bỏ lifecycleScope.launch bên trong

        if (currentUserId == null) {
            // Thử lấy lại userId nếu onResume chạy trước
            currentUserId = getUserIdFromSession()
            if (currentUserId == null) return // Nếu vẫn null thì thoát
        }

        val allAddresses = addressService.getAllAddresses(currentUserId!!)
        selectedAddress = allAddresses.firstOrNull { it.isPrimaryShipping }

        if (selectedAddress != null) {
            tvAddressFullName.text = "${selectedAddress!!.fullName} | ${selectedAddress!!.phoneNumber}"
            tvAddressDetails.text = selectedAddress!!.getFullAddressString()
            tvAddressFullName.visibility = View.VISIBLE
            tvAddressDetails.visibility = View.VISIBLE
            tvSelectAddressHint.visibility = View.GONE
        } else {
            tvAddressFullName.visibility = View.GONE
            tvAddressDetails.visibility = View.GONE
            tvSelectAddressHint.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // ⭐️ Khi quay lại từ AddressListActivity, tải lại địa chỉ
        // (Khởi chạy coroutine mới vì onResume không phải suspend)
        lifecycleScope.launch {
            loadDefaultAddress()
        }
    }

    // ---------------------------------------------------
    // ⭐️ LOGIC VOUCHER (Đã cập nhật) ⭐️
    // ---------------------------------------------------

    private fun applyVoucher(code: String) {
        lifecycleScope.launch {
            val voucher = voucherService.getVoucherByCode(code)

            // ⭐️ Tổng tiền để xét voucher là Tạm tính - Giảm giá SP
            val eligibleTotal = subtotal - productDiscount

            // 1. Kiểm tra Voucher
            val (isValid, message) = validateVoucher(voucher, eligibleTotal)
            if (!isValid) {
                Toast.makeText(this@CheckoutActivity, message, Toast.LENGTH_SHORT).show()
                resetVoucher()
                return@launch
            }

            // 2. Voucher hợp lệ -> Tính toán và cập nhật
            val (isValidCalc, discount) = calculateVoucherDiscount(voucher!!, eligibleTotal)
            if (isValidCalc) {
                appliedVoucher = voucher
                voucherDiscount = discount
                updateTotalAmount() // Tính lại tổng tiền

                Toast.makeText(this@CheckoutActivity, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show()
                btnApplyVoucher.text = "Hủy"
                edtVoucher.isEnabled = false
            } else {
                resetVoucher() // Lỗi tính toán
            }
        }
    }

    /**
     * ⭐️ (MỚI) Hàm kiểm tra voucher hợp lệ
     */
    private fun validateVoucher(voucher: Voucher?, eligibleTotal: Double): Pair<Boolean, String> {
        if (voucher == null) return Pair(false, "Mã voucher không tồn tại")
        if (!voucher.isActive) return Pair(false, "Mã voucher đã bị vô hiệu")
        if (voucher.expirationDate != null && voucher.expirationDate.before(Date())) {
            return Pair(false, "Mã voucher đã hết hạn")
        }
        if (eligibleTotal < voucher.minOrderValue) {
            return Pair(false, "Đơn hàng chưa đủ ${formatter.format(voucher.minOrderValue)} để áp dụng")
        }
        if (voucher.usageLimit > 0 && voucher.usageCount >= voucher.usageLimit) {
            return Pair(false, "Mã voucher đã hết lượt sử dụng")
        }
        return Pair(true, "Hợp lệ")
    }

    /**
     * ⭐️ (MỚI) Hàm tính toán số tiền giảm
     */
    private fun calculateVoucherDiscount(voucher: Voucher, eligibleTotal: Double): Pair<Boolean, Double> {
        var discount = 0.0
        if (voucher.discountType == DiscountType.PERCENTAGE) {
            discount = eligibleTotal * (voucher.discountValue / 100)
            if (voucher.maxDiscountAmount != null && discount > voucher.maxDiscountAmount) {
                discount = voucher.maxDiscountAmount
            }
        } else { // FIXED_AMOUNT
            discount = voucher.discountValue
        }

        if (discount > eligibleTotal) {
            discount = eligibleTotal
        }
        return Pair(true, discount)
    }

    private fun resetVoucher() {
        appliedVoucher = null
        voucherDiscount = 0.0 // ⭐️ Đặt lại
        updateTotalAmount() // Tính lại tổng

        edtVoucher.setText("")
        edtVoucher.isEnabled = true
        btnApplyVoucher.text = "Áp dụng"
    }

    // ---------------------------------------------------
    // ⭐️ LOGIC ĐẶT HÀNG (Giữ nguyên) ⭐️
    // ---------------------------------------------------

    private fun handlePlaceOrder() {
        // ... (Kiểm tra address, payment method) ...
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
            return
        }
        val paymentMethod = if (rbCOD.isChecked) "COD" else if (rbPayPal.isChecked) "PAYPAL" else null
        if (paymentMethod == null) { /* ... */ return }

        val orderItems = cartItems.map {
            OrderItem(
                productId = it.productId,
                productName = it.productName,
                productImage = it.productImage,
                selectedColor = it.selectedColor,
                selectedSize = it.selectedSize,
                quantity = it.quantity,
                unitPrice = it.getDisplayPrice() // ⭐️ Sửa: Dùng giá hiển thị
            )
        }

        val newOrder = Order(
            userId = currentUserId!!,
            createdAt = Timestamp.now(),
            status = OrderStatus.PROCESSING,
            items = orderItems,
            totalAmount = totalAmount,
            // ⭐️ GÁN DỮ LIỆU SAO CHÉP ⭐️
            shippingAddress = selectedAddress, // 👈 1. Gán địa chỉ đã chọn
            discountCode = appliedVoucher?.code, // 👈 2. Gán mã (nếu có)
            discountAmount = productDiscount + voucherDiscount // 👈 3. Gán tổng tiền giảm
        )

        if (paymentMethod == "COD") {
            processOrder(newOrder)
        } else if (paymentMethod == "PAYPAL") {
            Toast.makeText(this, "Bắt đầu thanh toán PayPal... (Chưa cài đặt)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processOrder(order: Order) {
        CustomBottomSheetDialog.show(
            context = this,
            title = "Xác nhận Đặt hàng",
            message = "Bạn có chắc muốn đặt hàng với tổng tiền ${formatter.format(order.totalAmount)}?",
            positiveText = "Xác nhận",
            negativeText = "Hủy",
            onConfirm = {
                lifecycleScope.launch {
                    val orderSuccess = orderServiceImpl.createOrder(order)
                    if (orderSuccess) {
                        // 2. Trừ kho
                        val stockSuccess = productService.updateStockForOrder(order, isCancellation = false)
                        if (!stockSuccess) {
                            Toast.makeText(this@CheckoutActivity, "Lỗi nghiêm trọng: Không thể trừ kho!", Toast.LENGTH_LONG).show()
                        }

                        // 3. Cập nhật số lần dùng Voucher (nếu có)
                        appliedVoucher?.let {
                            // (Bạn cần thêm hàm updateVoucher vào VoucherService/Repo)
                            // val updatedVoucher = it.copy(usageCount = it.usageCount + 1)
                            // voucherService.updateVoucher(updatedVoucher)
                        }

                        // 4. Xóa giỏ hàng
                        cartService.clearCart(currentUserId!!)

                        Toast.makeText(this@CheckoutActivity, "Đặt hàng thành công!", Toast.LENGTH_LONG).show()

                        // 5. Chuyển sang màn hình Đơn hàng
                        val intent = Intent(this@CheckoutActivity, OrderActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@CheckoutActivity, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}