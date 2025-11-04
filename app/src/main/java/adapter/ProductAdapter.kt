package adapter

import android.graphics.Paint // ⭐️ (THÊM) Import để dùng gạch ngang
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Product
import java.text.NumberFormat // ⭐️ (THÊM) Import để format tiền
import java.util.Locale // ⭐️ (THÊM) Import Locale

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ⭐️ (THÊM) Bộ định dạng tiền tệ
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvSold: TextView = itemView.findViewById(R.id.tvSold)

        // ⭐️ (SỬA) Ánh xạ 2 TextView giá mới
        val tvSalePrice: TextView = itemView.findViewById(R.id.tvSalePrice)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // 🔹 Lấy hình (Giữ nguyên)
        val imageResId = product.getPrimaryImageResId(holder.itemView.context)
        if (imageResId != 0) {
            holder.imgProduct.setImageResource(imageResId)
        } else {
            holder.imgProduct.setImageResource(R.drawable.no_image)
        }

        holder.tvName.text = product.name

        // 🔹 Demo Rating/Sold (Giữ nguyên)
        holder.tvRating.text = "⭐ ${"%.1f".format(Math.random() * 5)}"
        holder.tvSold.text = "${(50..5000).random()} sold"

        // ⭐️ (SỬA) Logic hiển thị giá
        if (product.isOnSale()) {
            // --- CÓ SALE ---
            // 1. Hiển thị giá sale (màu đỏ, in đậm)
            holder.tvSalePrice.text = formatter.format(product.getDisplayPrice()) // Đây là salePrice
            holder.tvSalePrice.visibility = View.VISIBLE

            // 2. Hiển thị giá gốc (màu xám, gạch ngang)
            holder.tvOriginalPrice.text = formatter.format(product.price) // Đây là price gốc
            holder.tvOriginalPrice.paintFlags = holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvOriginalPrice.visibility = View.VISIBLE

        } else {
            // --- KHÔNG SALE ---
            // 1. Chỉ hiển thị giá bình thường
            holder.tvSalePrice.text = formatter.format(product.getDisplayPrice()) // Đây là price gốc
            holder.tvSalePrice.visibility = View.VISIBLE

            // 2. Ẩn TextView giá gốc và xóa gạch ngang (quan trọng khi tái sử dụng view)
            holder.tvOriginalPrice.visibility = View.GONE
            holder.tvOriginalPrice.paintFlags = holder.tvOriginalPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }


        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}