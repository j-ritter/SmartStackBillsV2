package com.example.smartstackbills

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Receipts : AppCompatActivity() {

    data class Receipt(
        val id: String,
        val title: String,
        val category: String,
        val subcategory: String,
        val vendor: String,
        val amount: String,
        val purchaseDate: String,
        val recurrence: String,
        val reminderDate: String,
        val paid: Boolean,
        val comments: String,
        var status: String
    )

    private val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinners()

        findViewById<EditText>(R.id.purchaseDateField).setOnClickListener {
            showDatePickerDialog(it as EditText)
        }

        findViewById<EditText>(R.id.reminderField).setOnClickListener {
            showDatePickerDialog(it as EditText)
        }

        findViewById<Button>(R.id.createButton).setOnClickListener {
            if (validateInputs()) {
                saveReceipt()
            }
        }
    }

    private fun setupSpinners() {
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.category_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        val subcategorySpinner = findViewById<Spinner>(R.id.subcategorySpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.subcategory_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subcategorySpinner.adapter = adapter
        }

        val vendorSpinner = findViewById<Spinner>(R.id.vendorSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.vendor_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            vendorSpinner.adapter = adapter
        }

        val recurrenceSpinner = findViewById<Spinner>(R.id.recurrenceSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.recurrence_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            recurrenceSpinner.adapter = adapter
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
        datePickerDialog.datePicker.minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.timeInMillis
        datePickerDialog.show()
    }

    private fun validateInputs(): Boolean {
        val titleField = findViewById<EditText>(R.id.titleField)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val amountField = findViewById<EditText>(R.id.amountField)

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

    private fun saveReceipt() {
        val receipt = Receipt(
            id = database.push().key.toString(),
            title = findViewById<EditText>(R.id.titleField).text.toString(),
            category = findViewById<Spinner>(R.id.categorySpinner).selectedItem.toString(),
            subcategory = findViewById<Spinner>(R.id.subcategorySpinner).selectedItem.toString(),
            vendor = findViewById<Spinner>(R.id.vendorSpinner).selectedItem.toString(),
            amount = findViewById<EditText>(R.id.amountField).text.toString(),
            purchaseDate = findViewById<EditText>(R.id.purchaseDateField).text.toString(),
            recurrence = findViewById<Spinner>(R.id.recurrenceSpinner).selectedItem.toString(),
            reminderDate = findViewById<EditText>(R.id.reminderField).text.toString(),
            paid = findViewById<CheckBox>(R.id.paidCheckbox).isChecked,
            comments = findViewById<EditText>(R.id.commentsField).text.toString(),
            status = calculateStatus(
                findViewById<EditText>(R.id.purchaseDateField).text.toString(),
                findViewById<CheckBox>(R.id.paidCheckbox).isChecked
            )
        )

        database.child("receipts").child(receipt.id).setValue(receipt)
            .addOnSuccessListener {
                Toast.makeText(this, "Receipt added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add receipt: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateStatus(purchaseDate: String, paid: Boolean): String {
        return when {
            paid -> "paid"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val receiptDate = sdf.parse(purchaseDate)
                if (receiptDate.before(Date())) "due" else "incoming"
            }
        }
    }
}
