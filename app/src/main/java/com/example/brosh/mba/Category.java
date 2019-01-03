package com.example.brosh.mba;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.brosh on 7/13/2017.
 */

public class Category
{
    private String name;
    private String subCategoryName;
    private double remaining;
    private int budget;

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    private ArrayList<Transaction> transactions;

    public Category(String name, String subCategoryName)
    {
        this.name = name;
        this.subCategoryName = subCategoryName;
        this.remaining = 0;
        this.budget = 0;
    }

    public Category(String name)
    {
        this.name = name;
        this.subCategoryName = "ללא";
        this.remaining = 0;
        this.budget = 0;
    }

    public Category(String name, int budget, double remaining, List<Transaction> transactions)
    {
        this.name = name;
        this.subCategoryName = "ללא";//need to complete this part
        this.remaining = remaining;
        this.budget = budget;
        this.transactions = new ArrayList<Transaction>();
        if( transactions != null && transactions.size() > 0)
        {
            for (Transaction tran : transactions)
            {
                this.transactions.add(tran);
            }
        }
    }

    public Category(String name,String subCategoryName, int budget, double remaining, List<Transaction> transactions)
    {
        this.name = name;
        this.subCategoryName = subCategoryName;
        this.remaining = remaining;
        this.budget = budget;
        this.transactions = new ArrayList<Transaction>();
        if( transactions != null && transactions.size() > 0)
        {
            for (Transaction tran : transactions)
            {
                this.transactions.add(tran);
            }
        }
    }

    public void setBudgetValue(int budget)
    {
        this.budget = budget;
    }

    public void setRemainingValue(double remaining)
    {
        this.remaining = remaining;
    }

    public void subValRemaining(double valToSub)
    {
        remaining -= valToSub;
    }

    public String getName()
    {
        return name;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public double getRamainingValue()
{
    return remaining;
}

    public int getBudgetValue()
    {
        return budget;
    }

    public ArrayList<Transaction>  getTransactions()
    {
        return this.transactions;
    }

    public void addTransaction(Transaction transaction)
    {
        this.transactions.add(transaction);
    }

}
