package com.example.ui.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;
import com.example.data.ExpenseEntity;
import com.example.data.ExpenseRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Legacy Java + XML Screen for Viewing and Filtering Expense History.
 * Demonstrates classic Android Java ListView with BaseAdapter, TextWatcher search,
 * and AlertDialog interactions.
 */
public class ExpenseHistoryActivity extends Activity implements ExpenseAdapter.OnItemActionCallback {

    private ImageButton btnBackHistory;
    private EditText etSearchHistory;
    private Spinner spinnerFilterCategory;
    private TextView tvHistoryCount;
    private Button btnQuickAddJava;
    private ListView listViewExpenses;
    private TextView tvEmptyHistory;

    private ExpenseAdapter adapter;
    private ExpenseRepository repository;
    private final List<ExpenseEntity> allTransactions = new ArrayList<>();
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);

    private static final String[] CATEGORY_FILTERS = {
            "All Categories",
            "Food & Dining",
            "Transport",
            "Shopping",
            "Bills & Utilities",
            "Entertainment",
            "Health",
            "Salary",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        repository = ExpenseRepository.Companion.getInstance(this);

        initViews();
        setupFilterSpinner();
        setupListeners();
        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void initViews() {
        btnBackHistory = findViewById(R.id.btnBackHistory);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        tvHistoryCount = findViewById(R.id.tvHistoryCount);
        btnQuickAddJava = findViewById(R.id.btnQuickAddJava);
        listViewExpenses = findViewById(R.id.listViewExpenses);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        adapter = new ExpenseAdapter(this, new ArrayList<>(), this);
        listViewExpenses.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                CATEGORY_FILTERS
        );
        spinnerFilterCategory.setAdapter(filterAdapter);
    }

    private void setupListeners() {
        btnBackHistory.setOnClickListener(v -> finish());

        btnQuickAddJava.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            startActivity(intent);
        });

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadExpenses() {
        repository.getAllExpensesAsync(list -> runOnUiThread(() -> {
            allTransactions.clear();
            allTransactions.addAll(list);
            applyFilters();
        }));
    }

    private void applyFilters() {
        String query = etSearchHistory.getText().toString().trim().toLowerCase(Locale.US);
        int selectedIndex = spinnerFilterCategory.getSelectedItemPosition();
        String selectedCategory = (selectedIndex > 0) ? CATEGORY_FILTERS[selectedIndex] : null;

        List<ExpenseEntity> filtered = new ArrayList<>();
        for (ExpenseEntity item : allTransactions) {
            boolean matchesCategory = (selectedCategory == null) || item.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = query.isEmpty() ||
                    item.getTitle().toLowerCase(Locale.US).contains(query) ||
                    item.getCategory().toLowerCase(Locale.US).contains(query) ||
                    item.getNotes().toLowerCase(Locale.US).contains(query);

            if (matchesCategory && matchesSearch) {
                filtered.add(item);
            }
        }

        adapter.updateData(filtered);
        tvHistoryCount.setText(String.format(Locale.US, "Transactions (%d)", filtered.size()));

        if (filtered.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            listViewExpenses.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            listViewExpenses.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(ExpenseEntity expense) {
        String formattedDate = fullDateFormat.format(new Date(expense.getDate()));
        String typeLabel = "EXPENSE".equalsIgnoreCase(expense.getType()) ? "Expense" : "Income";

        String details = "Amount: $" + String.format(Locale.US, "%.2f", expense.getAmount()) +
                "\nType: " + typeLabel +
                "\nCategory: " + expense.getCategory() +
                "\nPayment Method: " + expense.getPaymentMethod() +
                "\nDate: " + formattedDate +
                (expense.getNotes().isEmpty() ? "" : "\nNotes: " + expense.getNotes());

        new AlertDialog.Builder(this)
                .setTitle(expense.getTitle())
                .setMessage(details)
                .setPositiveButton("Close", null)
                .setNegativeButton("Delete", (dialog, which) -> confirmDelete(expense))
                .show();
    }

    @Override
    public void onDeleteClick(ExpenseEntity expense) {
        confirmDelete(expense);
    }

    private void confirmDelete(ExpenseEntity expense) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message) + "\n\n\"" + expense.getTitle() + "\"")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteByIdAsync(expense.getId(), success -> runOnUiThread(() -> {
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        loadExpenses();
                    }));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
