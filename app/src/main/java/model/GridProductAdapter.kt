package model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.shoesapp.R
import com.example.shoesapp.model.Product

class GridProductAdapter(context: Context, private val list: List<Product>) :
    ArrayAdapter<Product>(context, 0, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.product_cart, parent, false)

        val model = getItem(position)

        val imageView = itemView.findViewById<ImageView>(R.id.image)
        val textView = itemView.findViewById<TextView>(R.id.name)
        val priceView = itemView.findViewById<TextView>(R.id.price)

        model?.let {
            textView.text = it.name
            imageView.setImageResource(it.imageResId)
            priceView.text = it.price
        }

        return itemView
    }
}
