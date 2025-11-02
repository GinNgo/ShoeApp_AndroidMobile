package ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.shoesapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import service.UserService
import ui.home.CartActivity
import ui.home.HomeActivity
import utils.SessionManager

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var userService: UserService
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


    protected suspend fun getUserIdFromSession(): String? {
        userService = UserService()
        sessionManager = SessionManager(this)
        val email = sessionManager.getUserSession()?.first ?: return null
        val user = userService.getUserByEmail(email)
        return user?.id
    }
}