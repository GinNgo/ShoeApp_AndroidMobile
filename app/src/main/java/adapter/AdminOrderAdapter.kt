package adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log // ⭐️ (THÊM) Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager // ⭐️ (THÊM IMPORT NÀY)
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Order
import model.OrderStatus
import model.User
import service.serviceImplement.UserService
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class AdminOrderAdapter(
    private val context: Context,
    private var orders: List<Order>,
    private val userService: UserService,
    private val onChangeStatusClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    private val userCache = mutableMapOf<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newOrders: List<Order>) {
        orders = newOrders
        Log.d("AdminOrderAdapter", "Updating list with ${newOrders.size} orders") // ⭐️ Thêm Log
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvAdminOrderId)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tvAdminOrderDate)
        private val tvOrderCustomer: TextView = itemView.findViewById(R.id.tvAdminOrderCustomer)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvAdminOrderStatus)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvAdminOrderTotal)
        private val recyclerItems: RecyclerView = itemView.findViewById(R.id.recyclerAdminOrderItems)
        private val btnChangeStatus: Button = itemView.findViewById(R.id.btnChangeStatus)

        fun bind(order: Order) {
            tvOrderId.text = "#${order.id}"
            tvOrderDate.text = "Ngày: ${dateFormatter.format(order.createdAt.toDate())}"

            // ⭐️ (SỬA LỖI) Bỏ chữ 'D' bị lỗi
            tvOrderTotal.text = formatter.format(order.totalAmount)

            tvOrderStatus.text = order.status.name

            // 1. Lấy tên Khách hàng
            if (userCache.containsKey(order.userId)) {
                tvOrderCustomer.text = "Khách: ${userCache[order.userId]}"
            } else {
                tvOrderCustomer.text = "Khách: Đang tải..."
                CoroutineScope(Dispatchers.IO).launch {
                    val user = userService.getUserById(order.userId)
                    val name = user?.email ?: "Không rõ"
                    userCache[order.userId] = name
                    withContext(Dispatchers.Main) {
                        tvOrderCustomer.text = "Khách: $name"
                    }
                }
            }

            // 2. Đặt màu cho Trạng thái
            val statusColor = when (order.status) {
                OrderStatus.PROCESSING -> "#FFA000" // Cam
                OrderStatus.IN_DELIVERY -> "#0288D1" // Xanh dương
                OrderStatus.COMPLETE -> "#388E3C" // Xanh lá
                OrderStatus.CANCELLED -> "#D32F2F" // Đỏ
            }
            tvOrderStatus.background?.setTint(Color.parseColor(statusColor))

            // 3. ⭐️ (SỬA LỖI Ở ĐÂY)
            // Hiển thị danh sách sản phẩm lồng nhau
            val itemAdapter = AdminOrderItemAdapter(context, order.items)

            // ⭐️ BẠN ĐANG THIẾU 1 DÒNG NÀY:
            recyclerItems.layoutManager = LinearLayoutManager(context)

            recyclerItems.adapter = itemAdapter

            // 4. Nút bấm
            btnChangeStatus.setOnClickListener { onChangeStatusClick(order) }
            if (order.status == OrderStatus.COMPLETE || order.status == OrderStatus.CANCELLED) {
                btnChangeStatus.visibility = View.GONE
            } else {
                btnChangeStatus.visibility = View.VISIBLE
            }
        }
    }
}