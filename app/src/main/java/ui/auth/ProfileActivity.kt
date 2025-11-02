package ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shoesapp.R
import kotlinx.coroutines.launch
import model.CustomBottomSheetDialog
import service.UserService
import ui.home.HomeActivity
import utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private val userService = UserService()

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_form)

        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        spinnerGender.adapter = adapter

        sessionManager = SessionManager(this)
        lifecycleScope.launch {
            val email = sessionManager.getUserSession()?.first ?: return@launch
            val user = userService.getUserByEmail(email)

            val mail = findViewById<TextView>(R.id.mail)
            val firstName = findViewById<TextView>(R.id.first_name)
            val lastName = findViewById<TextView>(R.id.last_name)
            //        val date = findViewById<EditText>(R.id.date).text.toString()
            val phone = findViewById<TextView>(R.id.phone)
            val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)

            firstName.text = user?.firstName
            lastName.text = user?.lastName
            phone.text = user?.phoneNumber
            mail.text = email

            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == user?.gender) {
                    genderSpinner.setSelection(i)
                    break
                }
            }
        }

        val saveBtn = findViewById<TextView>(R.id.submit_btn)
        saveBtn.setOnClickListener {
            CustomBottomSheetDialog.show(
                context = this,
                title="Save Profile",
                message = "Are you sure you want to save this?",
                positiveText = "Yes, Save",
                negativeText = "Cancel",
                onConfirm = {
                    performSaveProfile()
                }
            )
        }

        val profile = findViewById<ImageView>(R.id.back_home)
        profile.setOnClickListener {
            // Navigate to CartActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSaveProfile(){
        lifecycleScope.launch {
            val firstName = findViewById<EditText>(R.id.first_name).text.toString()
            val lastName = findViewById<EditText>(R.id.last_name).text.toString()
            //        val date = findViewById<EditText>(R.id.date).text.toString()
            val phone = findViewById<EditText>(R.id.phone).text.toString()
            val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
            val gender = genderSpinner.selectedItem?.toString()

            // ✅ Tạo map dữ liệu
            val profileData: Map<String, Any?> = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
//            "dateOfBirth" to date,
                "phoneNumber" to phone,
                "gender" to gender
            )

            Log.d("PROFILE_DATA", profileData.toString())

            val email = sessionManager.getUserSession()?.first ?: return@launch
            val user = userService.getUserByEmail(email)
            if (user != null) {
                userService.updateUser(user.id.toString(), profileData)
                Toast.makeText(this@ProfileActivity, "Save success", Toast.LENGTH_SHORT).show()
            }
        }
    }
}