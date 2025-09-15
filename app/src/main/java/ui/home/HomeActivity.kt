package ui.home

import android.content.Intent
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
import ui.product.ProductDetailActivity

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
                R.drawable.ic_shoe_puma,
                "Giày sneaker cổ điển với chất liệu da lộn mềm mại, phong cách retro."
            )
        )
        productList.add(
            Product(
                "Nike Air Max 270",
                "$150.00",
                "4.8",
                "12,345 sold",
                R.drawable.ic_shoe,
                "Nike Air Max 270 với đế Air lớn, êm ái và phong cách thể thao hiện đại."
            )
        )
        productList.add(
            Product(
                "Adidas Ultraboost",
                "$180.00",
                "4.9",
                "8,765 sold",
                R.drawable.ic_shoe_adidas,
                "Ultraboost mang lại sự thoải mái tối đa, thiết kế ôm chân và đế Boost đàn hồi."
            )
        )
        productList.add(
            Product(
                "Converse Chuck Taylor",
                "$90.00",
                "4.5",
                "15,000 sold",
                R.drawable.ic_shoe2,
                "Giày Converse Chuck Taylor cổ điển, phù hợp với mọi phong cách thời trang."
            )
        )
        productList.add(
            Product(
                "New Balance 574",
                "$110.00",
                "4.7",
                "9,432 sold",
                R.drawable.ic_shoe3,
                "Thiết kế retro pha chút hiện đại, êm ái khi đi bộ và phù hợp đi hàng ngày."
            )
        )
        productList.add(
            Product(
                "Vans Old Skool",
                "$85.00",
                "4.6",
                "11,289 sold",
                R.drawable.ic_shoe5,
                "Vans Old Skool biểu tượng với đường sọc side stripe, phong cách streetwear."
            )
        )
        productList.add(
            Product(
                "Nike Air Force 1",
                "$100.00",
                "4.8",
                "20,542 sold",
                R.drawable.ic_shoe6,
                "Air Force 1 huyền thoại, thiết kế da trắng đơn giản nhưng cực kỳ phong cách."
            )
        )
        productList.add(
            Product(
                "Adidas Stan Smith",
                "$95.00",
                "4.7",
                "13,872 sold",
                R.drawable.ic_shoe_adidas16,
                "Stan Smith tối giản, dễ phối đồ, một trong những đôi giày nổi tiếng nhất của Adidas."
            )
        )
        productList.add(
            Product(
                "Reebok Classic Leather",
                "$80.00",
                "4.5",
                "7,654 sold",
                R.drawable.ic_shoe8,
                "Phong cách retro, chất liệu da mềm, thích hợp mang cả ngày."
            )
        )
        productList.add(
            Product(
                "Asics Gel-Kayano 27",
                "$160.00",
                "4.9",
                "5,431 sold",
                R.drawable.ic_shoe9,
                "Dòng giày chạy bộ cao cấp với công nghệ Gel giảm chấn đặc trưng."
            )
        )
        productList.add(
            Product(
                "Nike Dunk Low",
                "$140.00",
                "4.8",
                "18,245 sold",
                R.drawable.ic_shoe10,
                "Nike Dunk Low với phối màu đa dạng, cực hot trong giới trẻ streetwear."
            )
        )
        productList.add(
            Product(
                "Adidas Yeezy Boost 350",
                "$220.00",
                "4.9",
                "9,876 sold",
                R.drawable.ic_shoe_adidas10,
                "Thiết kế độc đáo bởi Kanye West, cực kỳ êm ái và cá tính."
            )
        )
        productList.add(
            Product(
                "Fila Disruptor II",
                "$75.00",
                "4.4",
                "6,432 sold",
                R.drawable.ic_shoe12,
                "Phong cách chunky sneaker với đế răng cưa nổi bật."
            )
        )
        productList.add(
            Product(
                "Jordan 1 Retro High",
                "$200.00",
                "5.0",
                "22,765 sold",
                R.drawable.ic_shoe_adidas9,
                "Huyền thoại Jordan 1, đôi giày gắn liền với lịch sử bóng rổ và sneakerhead."
            )
        )


        // Setup Adapter
        productAdapter = ProductAdapter(productList) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product) // truyền qua intent
            startActivity(intent)
        }
        recyclerProducts.layoutManager = GridLayoutManager(this, 2) // Grid 2 cột
        recyclerProducts.adapter = productAdapter
    }
}