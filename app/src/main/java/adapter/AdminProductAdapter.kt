package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Product

class AdminProductAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : ListAdapter<Product, AdminProductAdapter.ProductViewHolder>(DiffCallback()) {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtName: TextView = itemView.findViewById(R.id.txtProductName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtProductPrice)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEditProduct)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteProduct)

        fun bind(product: Product) {
            txtName.text = product.name
            txtPrice.text = "₫${product.price}"
            val imageRes = product.getPrimaryImageResId(itemView.context)
            if (imageRes != 0) imgProduct.setImageResource(imageRes)
            else imgProduct.setImageResource(R.drawable.no_image)

            btnEdit.setOnClickListener { onEdit(product) }
            btnDelete.setOnClickListener { onDelete(product) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_admin_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
    }

}
