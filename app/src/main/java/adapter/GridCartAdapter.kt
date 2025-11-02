package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.shoesapp.R
import model.CartItem

class GridCartAdapter(
    context: Context,
    private val list: MutableList<CartItem>,
    private val onQuantityChanged: (cartItem: CartItem, delta: Int) -> Unit,
    private val onDeleteItem: (cartItem: CartItem) -> Unit
) : ArrayAdapter<CartItem>(context, 0, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.product_cart, parent, false)

        val cartItem = getItem(position) // tạo biến riêng
        val imageView = itemView.findViewById<ImageView>(R.id.image)
        val textView = itemView.findViewById<TextView>(R.id.name)
        val priceView = itemView.findViewById<TextView>(R.id.price)
        val quantityView = itemView.findViewById<TextView>(R.id.tvQuantity)
        val btnIncrease = itemView.findViewById<ImageButton>(R.id.btnIncrease)
        val btnDecrease = itemView.findViewById<ImageButton>(R.id.btnDecrease)
        val btnDelete = itemView.findViewById<ImageView>(R.id.ivDelete)

        cartItem?.let { item ->
            textView.text = item.product.name
            priceView.text = "${item.product.price} $"
            quantityView.text = item.quantity.toString()

            val resId = item.product.getPrimaryImageResId(context)
            imageView.setImageResource(if (resId != 0) resId else R.drawable.no_image)

            // 🔹 Callback với cartItem riêng, không dùng `it` của lambda
            btnIncrease.setOnClickListener { onQuantityChanged(item, +1) }
            btnDecrease.setOnClickListener { onQuantityChanged(item, -1) }

            btnDelete.setOnClickListener { onDeleteItem(item) }
        }


        return itemView
    }
}
