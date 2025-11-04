package adapter

import android.content.Context
import android.graphics.Paint // ⭐️ (THÊM)
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // ⭐️ (THÊM)
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.OrderItem
import model.OrderStatus // ⭐️ (THÊM)
import java.text.NumberFormat
import java.util.Locale

/**
 * ⭐️ (SỬA) Adapter này đã được nâng cấp
 * Nó nhận thêm OrderStatus và onReviewClick
 */
class OrderItemAdapter(
    private val context: Context,
    private val orderItems: List<OrderItem>,
    private val orderStatus: OrderStatus, // ⭐️ (THÊM) Trạng thái của Order
    private val onReviewClick: (OrderItem) -> Unit // ⭐️ (THÊM) Callback
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        // ⭐️ (SỬA) Dùng layout mới 'item_order_summary.xml'
        // (Bạn cần tạo file này, tôi đã gửi ở tin nhắn trước)
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_summary, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = orderItems[position]
        // ⭐️ (SỬA) Truyền thêm tham số vào bind
        holder.bind(item, orderStatus, onReviewClick)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ⭐️ (SỬA) Ánh xạ các view từ 'item_order_summary.xml'
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProductCheckout)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductNameCheckout)
        private val tvVariant: TextView = itemView.findViewById(R.id.tvProductVariantCheckout)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantityCheckout)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPriceCheckout)
        private val btnReview: Button = itemView.findViewById(R.id.btnLeaveReviewItem) // ⭐️ (THÊM)

        fun bind(
            item: OrderItem,
            status: OrderStatus, // ⭐️ (THÊM)
            onReviewClick: (OrderItem) -> Unit // ⭐️ (THÊM)
        ) {
            tvName.text = item.productName
            tvVariant.text = "Màu: ${item.selectedColor}, Size: ${item.selectedSize}"
            tvQuantity.text = "x ${item.quantity}"
            tvPrice.text = formatter.format(item.getTotalPrice())

            // Tải ảnh
            val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
            imgProduct.setImageResource(if (resId != 0) resId else R.drawable.no_image)

            // ⭐️ (THÊM) Logic Ẩn/Hiện nút Đánh giá
            // Chỉ hiển thị nút khi:
            // 1. Đơn hàng đã 'COMPLETE' (Hoàn thành)
            // 2. Item này 'isReviewed' là 'false' (Chưa được đánh giá)
            if (status == OrderStatus.COMPLETE && !item.isReviewed) {
                btnReview.visibility = View.VISIBLE
                btnReview.setOnClickListener { onReviewClick(item) }
            } else {
                btnReview.visibility = View.GONE
            }
        }
    }
}