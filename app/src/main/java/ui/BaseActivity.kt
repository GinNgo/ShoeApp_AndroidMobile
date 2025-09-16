package ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.shoesapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import ui.home.CartActivity
import ui.home.HomeActivity

abstract class BaseActivity : AppCompatActivity() {

    protected fun handleNavigation(selectedId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = selectedId

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_cart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    // open Profile
                    true
                }

                else -> false
            }
        }
    }
}