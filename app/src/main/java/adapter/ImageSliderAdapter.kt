package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoesapp.R
import model.ProductImage

class ImageSliderAdapter(
    private val context: Context,
    private val images: List<ProductImage>
) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false) // 👈 Sẽ tạo file này ở bước 3
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val image = images[position]
        holder.bind(image, context)
    }

    override fun getItemCount(): Int = images.size

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imgSlide)

        fun bind(image: ProductImage, context: Context) {
            // Logic tải ảnh:
            // 1. Nếu là URL (http) hoặc Uri (content://)
            if (image.imageUrl.startsWith("http") || image.imageUrl.startsWith("content")) {
                Glide.with(context)
                    .load(image.imageUrl)
                    .error(R.drawable.no_image) // Ảnh lỗi
                    .into(imageView)
            }
            // 2. Nếu là tên từ drawable (dữ liệu mẫu)
            else {
                val resId = context.resources.getIdentifier(
                    image.imageUrl,
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    imageView.setImageResource(R.drawable.no_image)
                }
            }
        }
    }
}