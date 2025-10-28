package adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.DiscountType
import model.Voucher
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class VoucherAdapter(
    private val onEdit: (Voucher) -> Unit,
    private val onDelete: (Voucher) -> Unit
) : ListAdapter<Voucher, VoucherAdapter.VoucherViewHolder>(VoucherDiffCallback()) {

    // Helper để format tiền tệ và ngày
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = getItem(position)
        holder.bind(voucher, onEdit, onDelete, currencyFormatter, dateFormatter)
    }

    class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewStatus: View = itemView.findViewById(R.id.viewStatus)
        private val tvVoucherCode: TextView = itemView.findViewById(R.id.tvVoucherCode)
        private val tvDiscountInfo: TextView = itemView.findViewById(R.id.tvDiscountInfo)
        private val tvVoucherDescription: TextView = itemView.findViewById(R.id.tvVoucherDescription)
        private val tvVoucherExpiry: TextView = itemView.findViewById(R.id.tvVoucherExpiry)
        private val tvVoucherUsage: TextView = itemView.findViewById(R.id.tvVoucherUsage)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditVoucher)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteVoucher)

        fun bind(
            voucher: Voucher,
            onEdit: (Voucher) -> Unit,
            onDelete: (Voucher) -> Unit,
            formatter: NumberFormat,
            dateFormatter: SimpleDateFormat
        ) {
            tvVoucherCode.text = voucher.code
            tvVoucherDescription.text = voucher.description

            // Hiển thị thông tin giảm giá
            tvDiscountInfo.text = when (voucher.discountType) {
                DiscountType.PERCENTAGE -> "Giảm ${voucher.discountValue}%"
                DiscountType.FIXED_AMOUNT -> "Giảm ${formatter.format(voucher.discountValue)}"
            }

            // Hiển thị trạng thái
            val statusColor = if (voucher.isActive) {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark)
            }
            viewStatus.background.setTint(statusColor)

            // Hạn sử dụng
            tvVoucherExpiry.text = voucher.expirationDate?.let {
                "Hết hạn: ${dateFormatter.format(it)}"
            } ?: "Không hết hạn"

            // Số lần dùng
            tvVoucherUsage.text = "Đã dùng: ${voucher.usageCount} / ${voucher.usageLimit}"

            btnEdit.setOnClickListener { onEdit(voucher) }
            btnDelete.setOnClickListener { onDelete(voucher) }
        }
    }

    class VoucherDiffCallback : DiffUtil.ItemCallback<Voucher>() {
        override fun areItemsTheSame(oldItem: Voucher, newItem: Voucher) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Voucher, newItem: Voucher) = oldItem == newItem
    }
}