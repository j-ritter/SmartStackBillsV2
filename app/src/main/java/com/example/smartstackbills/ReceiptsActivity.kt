package com.example.smartstackbills

import android.app.DatePickerDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ReceiptsActivity : AppCompatActivity() {

    private lateinit var purchaseDateField: EditText
    private lateinit var reminderField: EditText
    private lateinit var recurrenceSpinner: Spinner
    private lateinit var paidCheckbox: CheckBox

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipts)

        // Initialize the title field
        val titleField: EditText = findViewById(R.id.titleField)

        // Initialize the category spinner
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)
        val categories = arrayOf("Choose", "Accommodation", "Communication", "Insurance", "Subscriptions and Memberships", "Transportation", "Finance/Fees", "Taxes", "Health", "Education", "Shopping and Consumption")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Initialize the vendor spinner
        val vendorSpinner: Spinner = findViewById(R.id.vendorSpinner)
        val vendors = arrayOf("Choose", "Amazon", "Aldi", "Edeka", "REWE", "Target", "Costco", "Carrefour", "Tesco", "Home Depot", "Best Buy")
        val vendorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vendors)
        vendorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vendorSpinner.adapter = vendorAdapter

        // Initialize the recurrence spinner
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner)
        val recurrenceOptions = arrayOf("Choose", "Weekly", "Monthly", "Yearly", "Custom")
        val recurrenceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recurrenceOptions)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recurrenceSpinner.adapter = recurrenceAdapter

        // Initialize the paid checkbox
        paidCheckbox = findViewById(R.id.paidCheckbox)

        // Initialize date fields
        purchaseDateField = findViewById(R.id.purchaseDateField)
        reminderField = findViewById(R.id.reminderField)

        // Set current date as default for purchase date field
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
        purchaseDateField.setText(currentDate)

        purchaseDateField.setOnClickListener { showDatePickerDialog(purchaseDateField) }
        reminderField.setOnClickListener { showDatePickerDialog(reminderField) }

        // Initialize the add image button
        val addImageButton: Button = findViewById(R.id.addImageButton)
        addImageButton.setOnClickListener {
            openImagePicker()
        }

        // Initialize the buttons
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val createButton: Button = findViewById(R.id.createButton)

        cancelButton.setOnClickListener {
            // Handle cancel button click
            finish()
        }

        createButton.setOnClickListener {
            // Validate mandatory fields
            val title = titleField.text.toString().trim()
            val categoryPosition = categorySpinner.selectedItemPosition

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
            } else if (categoryPosition == 0) {
                Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show()
            } else {
                // Handle create button click - mandatory fields are filled out
                // Add your logic to create a receipt
                val isPaid = paidCheckbox.isChecked

                // Redirect to My Bills screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            // Handle the selected image (e.g., display it or upload it)
        }
    }
}
