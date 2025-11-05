package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.OrderItem

class AdminOrderItemAdapter(
    private val context: Context,
    private val orderItems: List<OrderItem>
) : RecyclerView.Adapter<AdminOrderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_admin_order_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = orderItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProductAdmin)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductNameAdmin)
        private val tvVariant: TextView = itemView.findViewById(R.id.tvProductVariantAdmin)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantityAdmin)

        fun bind(item: OrderItem) {
            tvName.text = item.productName
            tvVariant.text = "Màu: ${item.selectedColor}, Size: ${item.selectedSize}"
            tvQuantity.text = "x ${item.quantity}"

            val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
            imgProduct.setImageResource(if (resId != 0) resId else R.drawable.no_image)
        }
    }
}