package adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Order
import model.OrderItem // ⭐️ (THÊM)
import model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter này hiển thị List<Order> (Danh sách Đơn hàng)
 */
class OrderAdapter(
    private val context: Context,
    private val orders: List<Order>,
    private val onCancelClick: (Order) -> Unit,
    private val onReviewClick: (Order, OrderItem) -> Unit // ⭐️ (THÊM) Callback mới
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        // Sử dụng layout R.layout.item_order (cho cả 1 đơn hàng)
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val recyclerItems: RecyclerView = itemView.findViewById(R.id.recyclerOrderItems)
        private val btnCancelOrder: Button = itemView.findViewById(R.id.btnCancelOrder)

        fun bind(order: Order) {
            tvOrderId.text = "#${order.id.take(8)}..."
            tvOrderTotal.text = formatter.format(order.totalAmount)
            tvOrderStatus.text = order.status.name

            // 1. Đặt màu
            val statusColor = when (order.status) {
                OrderStatus.PROCESSING -> "#FFA000" // Cam
                OrderStatus.IN_DELIVERY -> "#0288D1" // Xanh dương
                OrderStatus.COMPLETE -> "#388E3C" // Xanh lá
                OrderStatus.CANCELLED -> "#D32F2F" // Đỏ
            }
            tvOrderStatus.background?.setTint(Color.parseColor(statusColor))

            // 2. Ẩn/Hiện nút Hủy
            if (order.status == OrderStatus.PROCESSING) {
                btnCancelOrder.visibility = View.VISIBLE
                btnCancelOrder.setOnClickListener { onCancelClick(order) }
            } else {
                btnCancelOrder.visibility = View.GONE
            }

            // 3. ⭐️ (SỬA LẠI) Hiển thị danh sách sản phẩm lồng nhau
            val itemAdapter = OrderItemAdapter(
                context,
                order.items,
                order.status, // 👈 Truyền trạng thái (Quan trọng)
                { orderItem ->
                    // 👈 Khi item được click, gọi callback của OrderAdapter
                    onReviewClick(order, orderItem)
                }
            )
            recyclerItems.adapter = itemAdapter
        }
    }
}