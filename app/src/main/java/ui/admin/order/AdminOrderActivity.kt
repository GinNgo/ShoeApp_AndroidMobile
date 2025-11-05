package ui.admin.order

import adapter.AdminOrderAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import model.Order
import model.OrderStatus
import service.IOrderService
import service.serviceImplement.OrderService
import service.serviceImplement.ProductService
import service.serviceImplement.UserService

class AdminOrderActivity : AppCompatActivity() {

    // --- Services ---
    private val orderServiceImpl: IOrderService = OrderService()
    private val userService = UserService()
    private val productService = ProductService()

    // --- Views ---
    private lateinit var recyclerOrders: RecyclerView
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var tvEmptyView: TextView

    // --- Data ---
    private lateinit var orderAdapter: AdminOrderAdapter
    private var allOrdersList = mutableListOf<Order>() // ⭐️ Danh sách "master"
    private var displayedOrdersList = mutableListOf<Order>() // ⭐️ Danh sách đã lọc

    // ⭐️ (SỬA 1) Đặt bộ lọc mặc định là 'null' (Tất cả)
    // Kiểu 'OrderStatus?' (có dấu ?) nghĩa là nó CÓ THỂ nhận 'null'
    private var currentFilter: OrderStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order)

        initViews()
        setupRecyclerView()
        setupFilterChips()

        // ⭐️ (SỬA 2) Tải TẤT CẢ đơn hàng
        loadAllOrders()
    }

    private fun initViews() {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarAdminOrders)
            .setNavigationOnClickListener { finish() }

        recyclerOrders = findViewById(R.id.recyclerAdminOrders)
        chipGroupStatus = findViewById(R.id.chipGroupOrderStatus)
        tvEmptyView = findViewById(R.id.tvEmptyViewOrders)
    }

    private fun setupRecyclerView() {
        orderAdapter = AdminOrderAdapter(
            this,
            displayedOrdersList, // 👈 Dùng danh sách 'displayed'
            userService,
            onChangeStatusClick = { order ->
                showChangeStatusDialog(order)
            }
        )
        recyclerOrders.layoutManager = LinearLayoutManager(this)
        recyclerOrders.adapter = orderAdapter
    }

    /**
     * ⭐️ (SỬA 3) Hàm này tải TẤT CẢ đơn hàng (chỉ 1 lần)
     */
    private fun loadAllOrders() {
        toggleEmptyState(false) // Ẩn EmptyView khi đang tải
        lifecycleScope.launch {
            val orders = orderServiceImpl.getAllOrdersAdmin()
            Log.d("AdminOrderActivity", "Đã tải ${orders.size} tổng số đơn hàng")

            allOrdersList.clear()
            allOrdersList.addAll(orders.sortedByDescending { it.createdAt.toDate() })

            applyFilters() // 👈 Áp dụng bộ lọc (lần đầu sẽ là 'null' - Tất cả)
        }
    }

    /**
     * ⭐️ (SỬA 4) Thêm Chip "Tất cả" (với tag 'null')
     */
    private fun setupFilterChips() {
        val statuses = listOf(
            "Tất cả" to null, // 👈 (THÊM)
            "Đang xử lý" to OrderStatus.PROCESSING,
            "Đang giao" to OrderStatus.IN_DELIVERY,
            "Hoàn thành" to OrderStatus.COMPLETE,
            "Đã hủy" to OrderStatus.CANCELLED
        )

        chipGroupStatus.removeAllViews()

        statuses.forEach { (name, status) ->
            val chip = Chip(this).apply {
                text = name
                tag = status // 👈 Gắn Enum (hoặc null) vào
                isCheckable = true
                isChecked = (status == null) // 👈 Đặt "Tất cả" làm mặc định
            }
            chipGroupStatus.addView(chip)
        }

        // Cập nhật lại bộ lọc mặc định
        currentFilter = null

        // ⭐️ (SỬA 5) Sửa Listener để dùng 'as?' (ép kiểu an toàn)
        chipGroupStatus.setOnCheckedChangeListener { group, checkedId ->
            val selectedChip = group.findViewById<Chip>(checkedId)
            if (selectedChip != null) {
                // ⭐️ Dùng 'as?' để nó có thể nhận 'null' (từ chip "Tất cả")
                // Đây là dòng sửa lỗi crash của bạn
                currentFilter = selectedChip.tag as? OrderStatus
                applyFilters()
            }
        }
    }

    /**
     * ⭐️ (MỚI) Hàm này lọc danh sách 'allOrdersList' đã tải
     */
    private fun applyFilters() {
        // 1. Nếu 'currentFilter' là 'null' (Tất cả), hiển thị allOrdersList
        if (currentFilter == null) {
            displayedOrdersList.clear()
            displayedOrdersList.addAll(allOrdersList)
        } else {
            // 2. Nếu có bộ lọc, lọc danh sách
            val filtered = allOrdersList.filter { it.status == currentFilter }
            displayedOrdersList.clear()
            displayedOrdersList.addAll(filtered)
        }
        orderAdapter.updateList(displayedOrdersList) // 👈 Cập nhật adapter

        toggleEmptyState(displayedOrdersList.isEmpty())
    }


    private fun showChangeStatusDialog(order: Order) {
        val currentStatus = order.status
        val options = when (currentStatus) {
            OrderStatus.PROCESSING -> arrayOf("Đang giao hàng", "Hủy đơn hàng")
            OrderStatus.IN_DELIVERY -> arrayOf("Đã hoàn thành", "Hủy đơn hàng")
            else -> { return }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Đổi trạng thái Đơn hàng")
            .setItems(options) { dialog, which ->
                val selectedOption = options[which]
                val newStatus = when (selectedOption) {
                    "Đang giao hàng" -> OrderStatus.IN_DELIVERY
                    "Đã hoàn thành" -> OrderStatus.COMPLETE
                    "Hủy đơn hàng" -> OrderStatus.CANCELLED
                    else -> null
                }
                if (newStatus != null) {
                    updateOrderStatus(order, newStatus)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        lifecycleScope.launch {
            val success: Boolean

            if (newStatus == OrderStatus.CANCELLED) {
                success = orderServiceImpl.cancelOrder(order.id)
                if (success) {
                    productService.updateStockForOrder(order, isCancellation = true)
                }
            } else {
                val updatedOrder = order.copy(status = newStatus)
                success = orderServiceImpl.updateOrder(updatedOrder)
            }

            if (success) {
                Toast.makeText(this@AdminOrderActivity, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                // ⭐️ Tải lại toàn bộ danh sách để cập nhật
                loadAllOrders()
            } else {
                Toast.makeText(this@AdminOrderActivity, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerOrders.visibility = View.GONE
            tvEmptyView.visibility = View.VISIBLE
        } else {
            recyclerOrders.visibility = View.VISIBLE
            tvEmptyView.visibility = View.GONE
        }
    }
}