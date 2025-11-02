package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.shoesapp.R
import model.Order.Order
import model.Order.OrderStatus

class GridOrderItemAdapter(
    context: Context,
    private val orders: List<Order>,
    private val onBtnClick: (order: Order) -> Unit
) : ArrayAdapter<Order>(context, 0, orders) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.order_item, parent, false)

        val order = getItem(position)

        val imageView = itemView.findViewById<ImageView>(R.id.image)
        val nameView = itemView.findViewById<TextView>(R.id.name)
        val optionsView = itemView.findViewById<TextView>(R.id.tvOptions)
        val statusView = itemView.findViewById<TextView>(R.id.tvStatus)
        val priceView = itemView.findViewById<TextView>(R.id.price)
        val btnTrack = itemView.findViewById<Button>(R.id.btnTrackOrder)
        val btnLeaveReview = itemView.findViewById<Button>(R.id.btnLeaveReview)

        order?.let { o ->
            // 🖼 Hiển thị thông tin
            nameView.text = o.product.name
            optionsView.text = "Color | Size = 40 | Qty = ${o.quantity}"
            priceView.text = "$${o.product.price * o.quantity}"

            // 🟢 Gán trạng thái (In Delivery / Completed / Cancel)
            when (o.status) {
                OrderStatus.IN_DELIVERY -> {
                    statusView.text = "In Delivery"
                    btnLeaveReview.visibility = View.GONE
                    btnTrack.visibility = View.VISIBLE
                    btnTrack.setOnClickListener { onBtnClick(o) }
                }
                OrderStatus.COMPLETE -> {
                    statusView.text = "Completed"
                    btnLeaveReview.visibility = View.VISIBLE
                    btnTrack.visibility = View.GONE
                    btnLeaveReview.setOnClickListener { onBtnClick(o) }
                }
                OrderStatus.CANCEL -> {
                    statusView.text = "Cancelled"
                }
            }

            // 🖼 Ảnh sản phẩm
            val resId = o.product.getPrimaryImageResId(context)
            imageView.setImageResource(if (resId != 0) resId else R.drawable.no_image)
        }

        return itemView
    }
}

