package ui.home

import adapter.GridOrderItemAdapter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import model.Order.Order
import model.Order.OrderStatus
import model.Product
import service.IOrderService
import service.OrderServiceImpl
import ui.BaseActivity

class OrderActivity : BaseActivity() {
    private lateinit var orderItems: MutableList<Order>
    private lateinit var gridOrderItemAdapter: GridOrderItemAdapter
    private lateinit var orderServiceImpl: IOrderService
    private var userId: String? = null
    private lateinit var gridView: GridView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tabOngoing: TextView
    private lateinit var tabCompleted: TextView
    private lateinit var tabIndicator: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.order)

        orderItems = mutableListOf()
        orderServiceImpl = OrderServiceImpl()

        gridView = findViewById(R.id.grid_view)
        emptyStateLayout = findViewById(R.id.empty_state_layout)

        tabOngoing = findViewById(R.id.tab_ongoing)
        tabCompleted = findViewById(R.id.tab_completed)
        tabIndicator = findViewById(R.id.tab_indicator)

        // Khởi tạo adapter
        gridOrderItemAdapter = GridOrderItemAdapter(
            context = this,
            orders = orderItems,
            onBtnClick = { order ->
                if(order.status == OrderStatus.IN_DELIVERY){
                    Toast.makeText(this, "Tracking order: ${order.id}", Toast.LENGTH_SHORT).show()
                }else if (order.status == OrderStatus.COMPLETE){
                    showReviewDialog(order)
                }
            }
        )

        gridView.adapter = gridOrderItemAdapter

        // Click tab
        tabOngoing.setOnClickListener { selectTab(OrderStatus.IN_DELIVERY) }
        tabCompleted.setOnClickListener { selectTab(OrderStatus.COMPLETE) }

        // Load tab mặc định
        selectTab(OrderStatus.IN_DELIVERY)

        handleNavigation(R.id.nav_order)
    }

    private fun selectTab(status: OrderStatus) {
        // Update màu tab
        tabOngoing.setTextColor(
            ContextCompat.getColor(this, if (status == OrderStatus.IN_DELIVERY) R.color.black else R.color.gray)
        )
        tabCompleted.setTextColor(
            ContextCompat.getColor(this, if (status == OrderStatus.COMPLETE) R.color.black else R.color.gray)
        )

        // Di chuyển tab indicator
        tabIndicator.animate()
            .x(if (status == OrderStatus.IN_DELIVERY) tabOngoing.x else tabCompleted.x)
            .setDuration(200)
            .start()

        // Load dữ liệu theo tab
        lifecycleScope.launch {
            userId = getUserIdFromSession()
            if (userId == null) {
                Toast.makeText(this@OrderActivity, "Chưa đăng nhập", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            val fetchedOrders = orderServiceImpl.getAllOrderByUserIdAndStatus(userId!!, status)
            orderItems.clear()
            orderItems.addAll(fetchedOrders)
            gridOrderItemAdapter.notifyDataSetChanged()

            // Toggle GridView / EmptyState
            if (orderItems.isEmpty()) {
                gridView.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
            } else {
                gridView.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            }
        }
    }

    private fun showReviewDialog(o: Order) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.leave_review_popup, null)
        dialog.setContentView(view)

        // Tìm các view
        val tvProductName = view.findViewById<TextView>(R.id.review_name)
        val tvPrice = view.findViewById<TextView>(R.id.reviewPrice)
        val tvQuantity = view.findViewById<TextView>(R.id.review_quantity)
        val tvImg = view.findViewById<ImageView>(R.id.review_img)
        val etReview = view.findViewById<EditText>(R.id.etReview)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)

        // Cập nhật dữ liệu
        tvProductName.text = o.product.name
        tvQuantity.text = "Color | Size = 40 | Qty = ${o.quantity}"
        tvPrice.text = "$${o.product.price * o.quantity}"

        val resId = o.product.getPrimaryImageResId(this)
        tvImg.setImageResource(if (resId != 0) resId else R.drawable.no_image)

        // Xử lý nút
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val review = etReview.text.toString()
            // Xử lý dữ liệu ở đây
            Toast.makeText(this, "Rating: $rating\nReview: $review", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
