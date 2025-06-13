package com.example.brokenomore;

public class TransactionItem {
    private String category;
    private String date;
    private double amount;

    public TransactionItem(String category, String date, double amount) {
        this.category = category;
        this.date = date;
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }
}