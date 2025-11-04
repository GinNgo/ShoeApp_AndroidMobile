package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import model.FeedBack
import java.text.SimpleDateFormat
import java.util.Locale

class FeedBackAdapter(
    private val feedbacks: List<FeedBack>
) : RecyclerView.Adapter<FeedBackAdapter.FeedBackViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedBackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return FeedBackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedBackViewHolder, position: Int) {
        val feedback = feedbacks[position]
        holder.bind(feedback, dateFormatter)
    }

    override fun getItemCount(): Int = feedbacks.size

    class FeedBackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarFeedback)
        private val tvReview: TextView = itemView.findViewById(R.id.tvReviewText)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvReviewAuthor)

        fun bind(feedback: FeedBack, formatter: SimpleDateFormat) {
            ratingBar.rating = feedback.rating.toFloat()

            if (feedback.review.isEmpty()) {
                tvReview.visibility = View.GONE
            } else {
                tvReview.text = feedback.review
                tvReview.visibility = View.VISIBLE
            }

            // ⭐️ (Nâng cao) Bạn nên tải tên User thay vì ID
            val author = "bởi User ${feedback.userId.take(5)}..."
            val date = feedback.createdAt?.toDate()?.let { formatter.format(it) } ?: ""
            tvAuthor.text = "$author - $date"
        }
    }
}