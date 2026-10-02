package com.example.ui.legacy;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.data.ExpenseEntity;
import com.example.data.ExpenseRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Legacy Java + XML Screen for Adding an Expense or Income transaction.
 * Demonstrates classic Android Java patterns (findViewById, Spinners,
 * DatePickerDialog, RadioGroup, and asynchronous repository execution).
 */
public class AddExpenseActivity extends Activity {

    private EditText etTitle;
    private EditText etAmount;
    private EditText etNotes;
    private RadioGroup radioGroupType;
    private Spinner spinnerCategory;
    private Spinner spinnerPaymentMethod;
    private TextView tvSelectedDate;
    private LinearLayout layoutDatePicker;
    private Button btnSaveTransaction;
    private ImageButton btnBack;

    private Calendar selectedCalendar;
    private ExpenseRepository repository;
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private static final String[] CATEGORIES = {
            "Food & Dining",
            "Transport",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health",
            "Salary",
            "Other"
    };

    private static final String[] PAYMENT_METHODS = {
            "Cash",
            "Credit Card",
            "Debit Card",
            "UPI / Bank Transfer"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        repository = ExpenseRepository.Companion.getInstance(this);
        selectedCalendar = Calendar.getInstance();

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);
        radioGroupType = findViewById(R.id.radioGroupType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        layoutDatePicker = findViewById(R.id.layoutDatePicker);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        tvSelectedDate.setText(displayDateFormat.format(selectedCalendar.getTime()));
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                CATEGORIES
        );
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                PAYMENT_METHODS
        );
        spinnerPaymentMethod.setAdapter(paymentAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        layoutDatePicker.setOnClickListener(v -> showDatePicker());

        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvSelectedDate.setText(displayDateFormat.format(selectedCalendar.getTime()));
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError(getString(R.string.error_empty_title));
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError(getString(R.string.error_invalid_amount));
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError(getString(R.string.error_invalid_amount));
                etAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError(getString(R.string.error_invalid_amount));
            etAmount.requestFocus();
            return;
        }

        int checkedId = radioGroupType.getCheckedRadioButtonId();
        String type = (checkedId == R.id.radioIncome) ? "INCOME" : "EXPENSE";
        String category = spinnerCategory.getSelectedItem().toString();
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();
        long dateMillis = selectedCalendar.getTimeInMillis();

        ExpenseEntity expense = new ExpenseEntity(
                0,
                title,
                amount,
                category,
                type,
                dateMillis,
                paymentMethod,
                notes
        );

        // Execute save via loose MVVM repository call
        repository.insertExpenseAsync(expense, id -> runOnUiThread(() -> {
            Toast.makeText(this, getString(R.string.msg_transaction_saved), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }));
    }
}
