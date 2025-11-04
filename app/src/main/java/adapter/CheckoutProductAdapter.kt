package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CheckoutProductAdapter(
    private val context: Context,
    private val cartItems: List<CartItem>
) : RecyclerView.Adapter<CheckoutProductAdapter.CheckoutViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checkout_product, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProductCheckout)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductNameCheckout)
        private val tvVariant: TextView = itemView.findViewById(R.id.tvProductVariantCheckout)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvProductQuantityCheckout)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPriceCheckout)

        fun bind(item: CartItem) {
            tvName.text = item.productName
            tvVariant.text = "Màu: ${item.selectedColor}, Size: ${item.selectedSize}"
            tvQuantity.text = "x ${item.quantity}"
            tvPrice.text = formatter.format(item.getTotalPrice())

            val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
            if (resId != 0) {
                imgProduct.setImageResource(resId)
            } else {
                imgProduct.setImageResource(R.drawable.no_image)
            }
        }
    }
}