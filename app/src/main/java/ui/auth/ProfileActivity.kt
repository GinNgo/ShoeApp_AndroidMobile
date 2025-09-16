package ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.shoesapp.R
import ui.home.HomeActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_form)

        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        spinnerGender.adapter = adapter


        val profile = findViewById<ImageView>(R.id.back_home)
        // Set click event
        profile.setOnClickListener {
            // Navigate to CartActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}