package ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoesapp.R
import com.example.shoesapp.adapter.ProductAdapter
import com.example.shoesapp.model.Product

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Xử lý padding cho status bar/navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ánh xạ RecyclerView
        recyclerProducts = findViewById(R.id.recyclerProducts)

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

        // Setup Adapter
        productAdapter = ProductAdapter(productList)
        recyclerProducts.layoutManager = GridLayoutManager(this, 2) // Grid 2 cột
        recyclerProducts.adapter = productAdapter
    }
}