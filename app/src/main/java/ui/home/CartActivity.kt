package ui.home

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import com.example.shoesapp.R
import com.example.shoesapp.model.Product
import model.GridProductAdapter
import ui.BaseActivity

class CartActivity : BaseActivity() {

    private lateinit var productList: ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cart)

        // Fake data sản phẩm
        productList = ArrayList()
        productList.add(
            Product(
                "Puma Suede Classic",
                "$120.00",
                "4.6",
                "6,843 sold",
                R.drawable.ic_shoe
            )
        )
        productList.add(Product("Nike Air Max 270", "$150.00", "4.8", "12,345 sold", R.drawable.ic_shoe))
        productList.add(Product("Adidas Ultraboost", "$180.00", "4.9", "8,765 sold", R.drawable.ic_shoe))
        productList.add(Product("Converse Chuck Taylor", "$90.00", "4.5", "15,000 sold", R.drawable.ic_shoe))
        productList.add(Product("Converse Chuck Taylor", "$90.00", "4.5", "15,000 sold", R.drawable.ic_shoe))
        productList.add(Product("Converse Chuck Taylor", "$90.00", "4.5", "15,000 sold", R.drawable.ic_shoe))
        productList.add(Product("Converse Chuck Taylor", "$90.00", "4.5", "15,000 sold", R.drawable.ic_shoe))
        productList.add(Product("Converse Chuck Taylor", "$90.00", "4.5", "15,000 sold", R.drawable.ic_shoe))

        val grid = GridProductAdapter(this, productList);
        val cart = findViewById<GridView>(R.id.grid_view);
        cart.adapter = grid

       handleNavigation(R.id.nav_cart)
    }
}