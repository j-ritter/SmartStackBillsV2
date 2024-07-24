package com.example.smartstackbills

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class Spendings : AppCompatActivity() {

    data class SpendingEntry(
        val id: String,
        val title: String,
        val categories: List<String>,
        val subcategories: List<String>,
        val amounts: List<String>,
        val date: String,
        val comments: String
    )

    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private lateinit var titleField: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var subcategorySpinner: Spinner
    private lateinit var amountField: EditText
    private lateinit var dateField: EditText
    private lateinit var commentsField: EditText
    private lateinit var createButton: Button
    private lateinit var dynamicFieldsContainer: LinearLayout
    private val categorySpinnerList = mutableListOf<Spinner>()
    private val subcategorySpinnerList = mutableListOf<Spinner>()
    private val amountFieldList = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spendings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleField = findViewById(R.id.titleField)
        dateField = findViewById(R.id.dateField)
        commentsField = findViewById(R.id.commentsField)
        createButton = findViewById(R.id.createButton)
        dynamicFieldsContainer = findViewById(R.id.dynamicFieldsContainer)
        val addCategoryButton = findViewById<Button>(R.id.addCategoryButton)

        addCategoryButton.setOnClickListener {
            addCategoryFields()
        }

        dateField.setOnClickListener {
            showDatePickerDialog(dateField)
        }

        createButton.setOnClickListener {
            if (validateInputs()) {
                saveSpending()
            }
        }

        addCategoryFields() // Add initial category and amount field
    }

    private fun addCategoryFields() {
        if (categorySpinnerList.size >= 4) {
            Toast.makeText(this, "You can only add up to 4 categories", Toast.LENGTH_SHORT).show()
            return
        }

        val categorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.category_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
        val subcategorySpinner = Spinner(this).apply {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.subcategory_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
        val amountField = EditText(this).apply {
            hint = getString(R.string.spending_hint_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            background = getDrawable(R.drawable.rounded_frame_background)
            setPadding(8, 8, 8, 8)
        }

        dynamicFieldsContainer.addView(categorySpinner)
        dynamicFieldsContainer.addView(subcategorySpinner)
        dynamicFieldsContainer.addView(amountField)

        categorySpinnerList.add(categorySpinner)
        subcategorySpinnerList.add(subcategorySpinner)
        amountFieldList.add(amountField)
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(date)
            },
            year, month, day
        )
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis // No future dates

        // Set min date to one year ago
        calendar.add(Calendar.YEAR, -1)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun validateInputs(): Boolean {
        val title = titleField.text.toString()
        val date = dateField.text.toString()
        val amounts = amountFieldList.map { it.text.toString() }

        if (title.isEmpty()) {
            titleField.error = "Title is required"
            return false
        }

        if (categorySpinnerList.any { it.selectedItem == null }) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show()
            return false
        }

        val amountValues = amounts.mapNotNull { it.toDoubleOrNull() }
        if (amounts.any { it.isEmpty() } || amountValues.any { it <= 0 || it > 1_000_000 }) {
            Toast.makeText(this, "Please enter valid amounts between 0 and 1,000,000", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveSpending() {
        val title = titleField.text.toString()
        val date = dateField.text.toString()
        val comments = commentsField.text.toString()
        val categories = categorySpinnerList.map { it.selectedItem.toString() }
        val subcategories = subcategorySpinnerList.map { it.selectedItem.toString() }
        val amounts = amountFieldList.map { it.text.toString() }

        val spendingId = database.child("spendings").push().key ?: return
        val spendingEntry = SpendingEntry(
            id = spendingId,
            title = title,
            categories = categories,
            subcategories = subcategories,
            amounts = amounts,
            date = date,
            comments = comments
        )

        database.child("spendings").child(spendingId).setValue(spendingEntry)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Spending saved successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity
                } else {
                    Toast.makeText(this, "Failed to save spending", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
