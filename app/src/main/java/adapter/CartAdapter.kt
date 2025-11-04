package adapter

import android.content.Context
import android.graphics.Paint // ⭐️ (THÊM)
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val context: Context,
    private val cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ⭐️ (SỬA) Ánh xạ ID từ layout item_cart.xml mới
        private val imgProduct: ImageView = itemView.findViewById(R.id.image)
        private val tvName: TextView = itemView.findViewById(R.id.name)
        private val tvVariant: TextView = itemView.findViewById(R.id.tvOptions)

        // ⭐️ (THÊM) Ánh xạ 2 view giá
        private val tvSalePrice: TextView = itemView.findViewById(R.id.tvSalePrice)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)

        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnMinus: ImageButton = itemView.findViewById(R.id.btnDecrease)
        private val btnPlus: ImageButton = itemView.findViewById(R.id.btnIncrease)
        private val btnDelete: ImageView = itemView.findViewById(R.id.ivDelete)

        fun bind(item: CartItem) {
            // ⭐️ (SỬA) Dùng các trường 'phẳng' của CartItem
            tvName.text = item.productName
            tvVariant.text = "Màu: ${item.selectedColor}, Size: ${item.selectedSize}"
            tvQuantity.text = item.quantity.toString()

            // ⭐️ (SỬA) Logic hiển thị giá (Giống hệt ProductAdapter)
            if (item.salePrice != null && item.salePrice!! < item.price) {
                // --- CÓ SALE ---
                tvSalePrice.text = formatter.format(item.getDisplayPrice()) // Giá sale
                tvOriginalPrice.text = formatter.format(item.price) // Giá gốc
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvOriginalPrice.visibility = View.VISIBLE
            } else {
                // --- KHÔNG SALE ---
                tvSalePrice.text = formatter.format(item.getDisplayPrice()) // Giá gốc
                tvOriginalPrice.visibility = View.GONE
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // ⭐️ (SỬA) Logic tải ảnh
            val resId = context.resources.getIdentifier(item.productImage, "drawable", context.packageName)
            if (resId != 0) {
                imgProduct.setImageResource(resId)
            } else {
                imgProduct.setImageResource(R.drawable.no_image)
            }

            // Listeners
            btnMinus.setOnClickListener {
                onQuantityChange(item, item.quantity - 1)
            }
            btnPlus.setOnClickListener {
                onQuantityChange(item, item.quantity + 1)
            }
            btnDelete.setOnClickListener {
                onDelete(item)
            }
        }
    }
}