package ui.home

import adapter.OrderAdapter // ⭐️ (SỬA) Dùng Adapter mới
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView // ⭐️ (SỬA)
import com.example.shoesapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder // ⭐️ (THÊM)
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import model.FeedBack
import model.Order
import model.OrderItem
import model.OrderStatus
import service.IFeedBackService
import service.IOrderService
import service.serviceImplement.FeedBackService
import service.serviceImplement.OrderService
import service.serviceImplement.ProductService
import ui.BaseActivity
import java.text.NumberFormat
import java.util.Locale

class OrderActivity : BaseActivity() {

    // --- Data ---
    private var orderItems = mutableListOf<Order>() // ⭐️ (SỬA) Model mới
    private lateinit var orderServiceImpl: IOrderService
    private lateinit var feedBackService: IFeedBackService
    private val productService = ProductService()
    private var currentUserId: String? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    // --- Views ---
    private lateinit var recyclerOrders: RecyclerView// ⭐️ (SỬA)
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tabProcessing: TextView // ⭐️ (SỬA)
    private lateinit var tabDelivering: TextView // ⭐️ (SỬA)
    private lateinit var tabCompleted: TextView
    private lateinit var tabCancelled: TextView // ⭐️ (THÊM)
    private lateinit var tabIndicator: View // ⭐️ (THÊM)

    // --- Adapter ---
    private lateinit var orderAdapter: OrderAdapter // ⭐️ (SỬA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.order)

        initViews()
        setupRecyclerView()
        setupListeners()

        // Load tab mặc định
        selectTab(OrderStatus.PROCESSING) // ⭐️ Bắt đầu bằng "Đang xử lý"

        handleNavigation(R.id.nav_order)
    }

    private fun initViews() {
        orderServiceImpl = OrderService()
        feedBackService = FeedBackService()
        recyclerOrders = findViewById(R.id.recyclerOrders)
        emptyStateLayout = findViewById(R.id.empty_state_layout)

        // ⭐️ (SỬA) Ánh xạ 4 tab và indicator
        tabProcessing = findViewById(R.id.tab_processing)
        tabDelivering = findViewById(R.id.tab_delivering)
        tabCompleted = findViewById(R.id.tab_completed)
        tabCancelled = findViewById(R.id.tab_cancelled)
        tabIndicator = findViewById(R.id.tab_indicator)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            context = this,
            orders = orderItems,
            onCancelClick = { order ->
                showCancelConfirmDialog(order)
            },
            onReviewClick = { order, item -> // ⭐️ (THÊM)
                showReviewDialog(order, item)
            }
        )
        recyclerOrders.adapter = orderAdapter
    }

    private fun setupListeners() {
        // ⭐️ (SỬA) Listener cho 4 tab
        tabProcessing.setOnClickListener { selectTab(OrderStatus.PROCESSING) }
        tabDelivering.setOnClickListener { selectTab(OrderStatus.IN_DELIVERY) }
        tabCompleted.setOnClickListener { selectTab(OrderStatus.COMPLETE) }
        tabCancelled.setOnClickListener { selectTab(OrderStatus.CANCELLED) }
    }

    /**
     * ⭐️ (VIẾT LẠI) Hàm chọn Tab và Di chuyển Indicator
     */
    private fun selectTab(status: OrderStatus) {
        // 1. Cập nhật màu text
        tabProcessing.setTextColor(ContextCompat.getColor(this, if (status == OrderStatus.PROCESSING) R.color.black else R.color.gray))
        tabDelivering.setTextColor(ContextCompat.getColor(this, if (status == OrderStatus.IN_DELIVERY) R.color.black else R.color.gray))
        tabCompleted.setTextColor(ContextCompat.getColor(this, if (status == OrderStatus.COMPLETE) R.color.black else R.color.gray))
        tabCancelled.setTextColor(ContextCompat.getColor(this, if (status == OrderStatus.CANCELLED) R.color.black else R.color.gray))

        // 2. ⭐️ (SỬA) Logic di chuyển indicator cho 4 tab
        val targetTab = when (status) {
            OrderStatus.PROCESSING -> tabProcessing
            OrderStatus.IN_DELIVERY -> tabDelivering
            OrderStatus.COMPLETE -> tabCompleted
            OrderStatus.CANCELLED -> tabCancelled
        }

        // Đợi cho layout tính toán xong vị trí 'x'
        targetTab.post {
            tabIndicator.animate()
                .x(targetTab.x) // 👈 Di chuyển đến 'x' của tab mục tiêu
                .setDuration(200)
                .start()
        }

        // 3. Load dữ liệu
        loadOrders(status)
    }

    private fun loadOrders(status: OrderStatus) {
        lifecycleScope.launch {
            if (currentUserId == null) {
                currentUserId = getUserIdFromSession()
            }
            if (currentUserId == null) {
                Toast.makeText(this@OrderActivity, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
                toggleEmptyState(true)
                return@launch
            }

            // ⭐️ (SỬA) Dùng hàm mới (getOrdersByStatus)
            val fetchedOrders = orderServiceImpl.getOrdersByStatus(currentUserId!!, status)
            orderItems.clear()
            orderItems.addAll(fetchedOrders)
            orderAdapter.notifyDataSetChanged()

            toggleEmptyState(orderItems.isEmpty())
        }
    }

    private fun showCancelConfirmDialog(order: Order) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc muốn hủy đơn hàng #${order.id.take(8)}?")
            .setPositiveButton("Xác nhận hủy") { dialog, _ ->
                lifecycleScope.launch {
                    val success = orderServiceImpl.cancelOrder(order.id)
                    if (success) {
                        // 2. ⭐️ (THÊM) Hoàn trả kho
                        // (Truyền 'isCancellation = true' để CỘNG trả kho)
                        val stockSuccess = productService.updateStockForOrder(order, isCancellation = true)
                        if (!stockSuccess) {
                            Toast.makeText(this@OrderActivity, "Lỗi: Không thể hoàn kho!", Toast.LENGTH_LONG).show()
                        }
                        Toast.makeText(this@OrderActivity, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show()
                        loadOrders(OrderStatus.PROCESSING) // Tải lại tab hiện tại
                    } else {
                        Toast.makeText(this@OrderActivity, "Lỗi khi hủy đơn", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Không", null)
            .show()
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerOrders.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerOrders.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }
    private fun showReviewDialog(order: Order, item: OrderItem) {
        val dialog = BottomSheetDialog(this)
        // ⭐️ (QUAN TRỌNG) Đảm bảo bạn có file 'leave_review_popup.xml'
        val view = layoutInflater.inflate(R.layout.leave_review_popup, null)
        dialog.setContentView(view)

        // Tìm các view
        val tvProductName = view.findViewById<TextView>(R.id.review_name)
        val tvOptions = view.findViewById<TextView>(R.id.review_quantity) // ⭐️ Sửa ID
        val tvPrice = view.findViewById<TextView>(R.id.reviewPrice)
        val tvImg = view.findViewById<ImageView>(R.id.review_img)
        val etReview = view.findViewById<EditText>(R.id.etReview)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        // Cập nhật dữ liệu
        tvProductName.text = item.productName
        tvOptions.text = "Màu: ${item.selectedColor}, Size: ${item.selectedSize}, SL: ${item.quantity}"
        tvPrice.text = formatter.format(item.getTotalPrice())

        // Tải ảnh
        val resId = resources.getIdentifier(item.productImage, "drawable", packageName)
        tvImg.setImageResource(if (resId != 0) resId else R.drawable.no_image)

        // Xử lý nút
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toDouble()
            val review = etReview.text.toString()

            if (rating == 0f.toDouble()) {
                Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tạo đối tượng FeedBack
            val newFeedback = FeedBack(
                userId = currentUserId!!,
                productId = item.productId,
                orderId = order.id,
                rating = rating,
                review = review,
                createdAt = Timestamp.now(),
            )

            lifecycleScope.launch {
                // ⭐️ Gọi service mới
                val success = feedBackService.createFeedBack(newFeedback, order)

                if (success) {
                    Toast.makeText(this@OrderActivity, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadOrders(OrderStatus.COMPLETE) // Tải lại (nút Review sẽ biến mất)
                } else {
                    Toast.makeText(this@OrderActivity, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}