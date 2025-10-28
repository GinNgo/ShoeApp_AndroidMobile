package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.Brand

class BrandAdapter(
    private val onEdit: (Brand) -> Unit,
    private val onDelete: (Brand) -> Unit
) : ListAdapter<Brand, BrandAdapter.BrandViewHolder>(BrandDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        // 💡 Bạn cần tạo file layout item_brand.xml (xem ở mục 7)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = getItem(position)
        holder.bind(brand, onEdit, onDelete)
    }

    class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvBrandDesc: TextView = itemView.findViewById(R.id.tvBrandDescription)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditBrand)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteBrand)

        fun bind(brand: Brand, onEdit: (Brand) -> Unit, onDelete: (Brand) -> Unit) {
            tvBrandName.text = brand.name
            tvBrandDesc.text = brand.description ?: "Không có mô tả"

            btnEdit.setOnClickListener { onEdit(brand) }
            btnDelete.setOnClickListener { onDelete(brand) }
        }
    }

    class BrandDiffCallback : DiffUtil.ItemCallback<Brand>() {
        override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem == newItem
        }
    }
}