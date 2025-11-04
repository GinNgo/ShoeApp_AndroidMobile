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
import com.google.android.material.chip.Chip
import model.Address

class AddressAdapter(
    private val onEdit: (Address) -> Unit,
    private val onDelete: (Address) -> Unit,
    private val onSetPrimary: (Address) -> Unit
) : ListAdapter<Address, AddressAdapter.AddressViewHolder>(AddressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = getItem(position)
        holder.bind(address, onEdit, onDelete, onSetPrimary)
    }

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFullNamePhone: TextView = itemView.findViewById(R.id.tvFullNamePhone)
        private val tvAddressString: TextView = itemView.findViewById(R.id.tvAddressString)
        private val chipPrimary: Chip = itemView.findViewById(R.id.chipPrimary)
        private val btnSetPrimary: Button = itemView.findViewById(R.id.btnSetPrimary)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditAddress)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteAddress)

        fun bind(
            address: Address,
            onEdit: (Address) -> Unit,
            onDelete: (Address) -> Unit,
            onSetPrimary: (Address) -> Unit
        ) {
            tvFullNamePhone.text = "${address.fullName} | ${address.phoneNumber}"
            tvAddressString.text = address.getFullAddressString()

            // Ẩn/hiện nút "Mặc định"
            if (address.isPrimaryShipping == true) {
                chipPrimary.visibility = View.VISIBLE
                btnSetPrimary.visibility = View.GONE
            } else {
                chipPrimary.visibility = View.GONE
                btnSetPrimary.visibility = View.VISIBLE
            }

            // Click Listeners
            btnEdit.setOnClickListener { onEdit(address) }
            btnDelete.setOnClickListener { onDelete(address) }
            btnSetPrimary.setOnClickListener { onSetPrimary(address) }
        }
    }

    class AddressDiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Address, newItem: Address) = oldItem == newItem
    }
}