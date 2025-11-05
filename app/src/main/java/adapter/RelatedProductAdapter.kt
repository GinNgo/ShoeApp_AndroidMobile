package adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Product
import java.text.NumberFormat
import java.util.Locale

class RelatedProductAdapter(
    private val context: Context,
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<RelatedProductAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_related, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProductRelated)
        private val tvName: TextView = itemView.findViewById(R.id.tvNameRelated)
        private val tvSalePrice: TextView = itemView.findViewById(R.id.tvSalePriceRelated)
        private val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPriceRelated)

        fun bind(product: Product) {
            tvName.text = product.name

            // Tải ảnh
            val resId = product.getPrimaryImageResId(context)
            imgProduct.setImageResource(if (resId != 0) resId else R.drawable.no_image)

            // Hiển thị giá
            if (product.isOnSale()) {
                tvSalePrice.text = formatter.format(product.getDisplayPrice())
                tvOriginalPrice.text = formatter.format(product.price)
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvOriginalPrice.visibility = View.VISIBLE
            } else {
                tvSalePrice.text = formatter.format(product.getDisplayPrice())
                tvOriginalPrice.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}