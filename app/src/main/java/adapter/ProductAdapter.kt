package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvSold: TextView = itemView.findViewById(R.id.tvSold)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // 🔹 Lấy hình primary từ drawable bằng hàm trong model
        val imageResId = product.getPrimaryImageResId(holder.itemView.context)
        if (imageResId != 0) {
            holder.imgProduct.setImageResource(imageResId)
        } else {
            holder.imgProduct.setImageResource(R.drawable.no_image) // ảnh mặc định
        }

        holder.tvName.text = product.name
        holder.tvPrice.text = "${product.price}₫"
        holder.tvRating.text = "⭐ ${"%.1f".format(Math.random() * 5)}" // demo rating
        holder.tvSold.text = "${(50..5000).random()} sold" // demo sold

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}
