package com.example.model;

/**
 * INCOMPLETE MULTI-MODULE ATTEMPT
 * This class was the initial target for extracting pure domain models
 * into an independent :core:model module.
 * Currently unused; domain models remain in :app for build stability.
 */
public class ExpenseModelWip {
    private final long id;
    private final String title;
    private final double amount;

    public ExpenseModelWip(long id, String title, double amount) {
        this.id = id;
        this.title = title;
        this.amount = amount;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
}
