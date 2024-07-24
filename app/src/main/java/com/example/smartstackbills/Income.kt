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

class Income : AppCompatActivity() {

    data class IncomeEntry(
        val id: String,
        val title: String,
        val category: String,
        val subcategory: String,
        val amount: String,
        val dateReceived: String,
        val frequency: String,
        val comments: String
    )

    private val database by lazy { FirebaseDatabase.getInstance().reference }
    private lateinit var titleField: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var subcategorySpinner: Spinner
    private lateinit var amountField: EditText
    private lateinit var dateReceivedField: EditText
    private lateinit var frequencySpinner: Spinner
    private lateinit var commentsField: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_income)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleField = findViewById(R.id.titleField)
        categorySpinner = findViewById(R.id.categorySpinner)
        subcategorySpinner = findViewById(R.id.subcategorySpinner)
        amountField = findViewById(R.id.amountField)
        dateReceivedField = findViewById(R.id.dateReceivedField)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        commentsField = findViewById(R.id.commentsField)
        createButton = findViewById(R.id.createButton)

        // Set up spinners
        setupSpinners()

        // Initialize date picker
        dateReceivedField.setOnClickListener {
            showDatePickerDialog(dateReceivedField)
        }

        createButton.setOnClickListener {
            if (validateInputs()) {
                saveIncome()
            }
        }
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this,
            R.array.income_category_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.subcategory_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subcategorySpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.frequency_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            frequencySpinner.adapter = adapter
        }
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
        val amount = amountField.text.toString()

        if (title.isEmpty()) {
            titleField.error = "Title is required"
            return false
        }

        if (categorySpinner.selectedItem == null) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show()
            return false
        }

        val amountValue = amount.toDoubleOrNull()
        if (amount.isEmpty() || amountValue == null || amountValue <= 0 || amountValue > 1_000_000) {
            amountField.error = "Please enter a valid amount between 0 and 1,000,000"
            return false
        }

        return true
    }

    private fun saveIncome() {
        val incomeEntry = IncomeEntry(
            id = database.push().key.toString(),
            title = titleField.text.toString(),
            category = categorySpinner.selectedItem.toString(),
            subcategory = subcategorySpinner.selectedItem.toString(),
            amount = amountField.text.toString(),
            dateReceived = dateReceivedField.text.toString(),
            frequency = frequencySpinner.selectedItem.toString(),
            comments = commentsField.text.toString()
        )

        database.child("incomes").child(incomeEntry.id).setValue(incomeEntry)
            .addOnSuccessListener {
                Toast.makeText(this, "Income saved successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save income: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
