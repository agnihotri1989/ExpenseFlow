package com.example.ui.legacy;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.R;
import com.example.data.ExpenseEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classic Java BaseAdapter demonstrating legacy Android list rendering
 * with the ViewHolder pattern.
 */
public class ExpenseAdapter extends BaseAdapter {

    public interface OnItemActionCallback {
        void onDeleteClick(ExpenseEntity expense);
        void onItemClick(ExpenseEntity expense);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private List<ExpenseEntity> expenseList;
    private final OnItemActionCallback actionCallback;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public ExpenseAdapter(Context context, List<ExpenseEntity> expenseList, OnItemActionCallback callback) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.expenseList = expenseList != null ? expenseList : new ArrayList<>();
        this.actionCallback = callback;
    }

    public void updateData(List<ExpenseEntity> newList) {
        this.expenseList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return expenseList.size();
    }

    @Override
    public ExpenseEntity getItem(int position) {
        return expenseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return expenseList.get(position).getId();
    }

    private static class ViewHolder {
        TextView tvCategoryBadge;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvDate;
        TextView tvAmount;
        TextView tvPaymentMethod;
        ImageButton btnDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_expense_legacy, parent, false);
            holder = new ViewHolder();
            holder.tvCategoryBadge = convertView.findViewById(R.id.tvItemCategoryBadge);
            holder.tvTitle = convertView.findViewById(R.id.tvItemTitle);
            holder.tvCategory = convertView.findViewById(R.id.tvItemCategory);
            holder.tvDate = convertView.findViewById(R.id.tvItemDate);
            holder.tvAmount = convertView.findViewById(R.id.tvItemAmount);
            holder.tvPaymentMethod = convertView.findViewById(R.id.tvItemPaymentMethod);
            holder.btnDelete = convertView.findViewById(R.id.btnItemDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ExpenseEntity expense = getItem(position);

        holder.tvTitle.setText(expense.getTitle());
        holder.tvCategory.setText(expense.getCategory());
        holder.tvDate.setText(dateFormat.format(new Date(expense.getDate())));
        holder.tvPaymentMethod.setText(expense.getPaymentMethod());

        // Category icon representation
        String category = expense.getCategory();
        if (category.contains("Food")) {
            holder.tvCategoryBadge.setText("🍔");
        } else if (category.contains("Transport")) {
            holder.tvCategoryBadge.setText("🚗");
        } else if (category.contains("Bills") || category.contains("Utilities")) {
            holder.tvCategoryBadge.setText("💡");
        } else if (category.contains("Shopping")) {
            holder.tvCategoryBadge.setText("🛍️");
        } else if (category.contains("Entertainment")) {
            holder.tvCategoryBadge.setText("🎬");
        } else if (category.contains("Health")) {
            holder.tvCategoryBadge.setText("💊");
        } else if (category.contains("Salary") || category.contains("Income")) {
            holder.tvCategoryBadge.setText("💰");
        } else {
            holder.tvCategoryBadge.setText("💳");
        }

        // Color coding for Expense vs Income
        boolean isExpense = "EXPENSE".equalsIgnoreCase(expense.getType());
        if (isExpense) {
            holder.tvAmount.setText(String.format(Locale.US, "-$%.2f", expense.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#F43F5E")); // Rose red
            holder.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_expense);
        } else {
            holder.tvAmount.setText(String.format(Locale.US, "+$%.2f", expense.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#059669")); // Emerald green
            holder.tvCategoryBadge.setBackgroundResource(R.drawable.bg_badge_income);
        }

        convertView.setOnClickListener(v -> {
            if (actionCallback != null) {
                actionCallback.onItemClick(expense);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionCallback != null) {
                actionCallback.onDeleteClick(expense);
            }
        });

        return convertView;
    }
}
