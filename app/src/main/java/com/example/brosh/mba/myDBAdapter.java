/*
 * Copyright (c) Daniel Brosh.
 */
package com.example.brosh.mba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Date;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.MainActivity.monthlyBudgetDB;
import static com.example.brosh.mba.global.DB_FILE_NAME;
import static com.example.brosh.mba.global.DB_SUFFIX;
import static com.example.brosh.mba.global.LOG_REPORT;
import static com.example.brosh.mba.global.TRAN_ID_PER_MONTH_NUMERATOR;
import static com.example.brosh.mba.global.convertDateToString;
import static com.example.brosh.mba.global.convertStringToDate;
import static com.example.brosh.mba.global.dateFormat;
import static com.example.brosh.mba.global.dateFormat2;
import static com.example.brosh.mba.global.getDateStartMonth;
import static com.example.brosh.mba.global.getYearMonth;
import static com.example.brosh.mba.global.reverseDateString;
import static com.example.brosh.mba.global.wrapStrForDb;

/**
 * Created by daniel.brosh on 3/26/2018.
 */
public class myDBAdapter {
    myDbHelper myhelper;

    public myDBAdapter(Context context) {
        myhelper = new myDbHelper(context);
    }

    public int getMaxIDPerMonthTRN(Date refMonth)
    {
        String wrappedRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" +  myDbHelper.transIDPerMonth + ") " +
                "FROM " + myhelper.TRANSACTION_TABLE + " " +
                "WHERE " + myhelper.transReferenceMonth + " = " + wrappedRefMonth, null);
        if (c.moveToFirst())
            if (c.moveToFirst())
            {
                int maxIDPerMonth = c.getInt(0);
                c.close();
                return maxIDPerMonth;
            }
        LOG_REPORT.add("No data found in getMaxIDPerMonthTRN method. ref month: " + wrappedRefMonth +"\n");
        return 0;
    }

    public int getMaxIDTRN()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" +  myDbHelper.transID + ") FROM " + myhelper.TRANSACTION_TABLE , null);
        if (c.moveToFirst())
        {
            int maxTransactionID = c.getInt(0);
            c.close();
            return maxTransactionID;
        }
        return 0;
    }

    public int getMaxBudgetNumberBGT()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" +  myDbHelper.budgetNumber + ") FROM " + myhelper.BUDGET_TABLE , null);
        if (c.moveToFirst())
        {
            int maxBudgetNumber = c.getInt(0);
            c.close();
            return maxBudgetNumber;
        }
        LOG_REPORT.add("No data found in getMaxBudgetNumberBGT method." +"\n");
        return 0;
    }

    public boolean checkCurrentRefMonthExists()
    {
        String wrappedStrRefMonth = wrapStrForDb(reverseDateString((global.convertDateToString(getDateStartMonth(),dateFormat)),"/"));
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT 1 " + "FROM " + myhelper.MONTHLY_BUDGET_TABLE + " WHERE " + myDbHelper.monthlyBudgetRefMonth + "=" + wrappedStrRefMonth , null);
        if (c.moveToFirst())
        {
            boolean isCurrentRefMonthExists = (c.getInt(0) == 1);
            c.close();
            return isCurrentRefMonthExists;
        }
        LOG_REPORT.add("No data found in checkCurrentRefMonthExists method. ref month: " + wrappedStrRefMonth +"\n");
        return false;
    }

    public boolean checkBudgetExists()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT distinct 1 " + "FROM " + myhelper.BUDGET_TABLE , null);
        if (c.moveToFirst())
        {
            boolean isBudgetExists =  (c.getInt(0) == 1);
            c.close();
            return isBudgetExists;
        }
        LOG_REPORT.add("No data found in checkBudgetExists method." +"\n");
        return false;
    }

    public boolean updateBudgetNumberMB(Date refMonth,int newBudgetNumber)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String strRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));

        String setNewBudgetNumberMB = "UPDATE " + myhelper.MONTHLY_BUDGET_TABLE +
                " SET " + myDbHelper.monthlyBudgetNumber + " = " + newBudgetNumber +
                " WHERE " + myDbHelper.monthlyBudgetRefMonth + " = " + strRefMonth;
        try
        {
            db.execSQL(setNewBudgetNumberMB);
        }
        catch(Exception e)
        {
            LOG_REPORT.add("Exception in updateBudgetNumberMB method. ref month: " + strRefMonth + ",new budget number" + newBudgetNumber + "\n" + e.getMessage().toString() + "\n");
            String error = e.getMessage().toString();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertAddedCategoriesToMBFromBudget(Date refMonth, ArrayList<Budget> newBudgets)
    {
        long status = 0;
        boolean isErrorOccurred = false;
        int maxBudgetNumber = monthlyBudgetDB.getMaxBudgetNumberBGT();
        for (Budget bgt:newBudgets)
        {
            int categoryID = monthlyBudgetDB.getCategoryId(bgt.getCategory());
            int subCategoryID = monthlyBudgetDB.getSubCategoryId(categoryID,bgt.getCategorySon());
            int budget = bgt.getValue();
            int balance = bgt.getValue();

            status = monthlyBudgetDB.insertMonthlyBudgetData(refMonth, categoryID, subCategoryID, maxBudgetNumber, budget, balance);
            if(status == -1) {
                LOG_REPORT.add("Insertion failed in insertAddedCategoriesToMBFromBudget method. ref month: " + refMonth + ", max budget number: " + maxBudgetNumber+ ", category ID: " + categoryID + "\n");
                isErrorOccurred = true;
            }
        }
        return status;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long writeMBFromBudget(Date refMonth)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        int maxBudgetNumber = getMaxBudgetNumberBGT();
        long status = 0;
        boolean isErrorOccurred = false;

        String selectMaxBudgetData = "SELECT " + myDbHelper.budgetCategoryID + "," + myDbHelper.budgetSubCategoryID + "," + myDbHelper.budgetCatPriority + "," + myDbHelper.budgetValue + "," + myDbHelper.budgetIsConstPayment + "," + myDbHelper.budgetShop + "," + myDbHelper.budgetChargeDay + " " +
                "FROM " + myDbHelper.BUDGET_TABLE + " " +
                "WHERE " + myDbHelper.budgetNumber + " = " + maxBudgetNumber + " order by " + myDbHelper.budgetCatPriority + " asc";
        Cursor cursor = db.rawQuery( selectMaxBudgetData, null);
        if(cursor.moveToFirst()) {
            do
            {
                int categoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetCategoryID));
                int subCategoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetSubCategoryID));
                //int catPriority = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetCatPriority));
                int budget = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetValue));
                int balance = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetValue));

                status = monthlyBudgetDB.insertMonthlyBudgetData(refMonth, categoryID, subCategoryID, maxBudgetNumber, budget, balance);
                if(status == -1)
                {
                    LOG_REPORT.add("No data found in writeMBFromBudget method. ref month: " + refMonth + "\n");
                    isErrorOccurred = true;
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return status;
    }

    public int getCategoryIdByName(String categoryName) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String wrapedCategoryName = wrapStrForDb(categoryName);
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + myDbHelper.categoryID + " FROM " + myhelper.CATEGORY_TABLE + " WHERE " + myDbHelper.categoryName + " = " + wrapedCategoryName, null);
        if (cursor.moveToFirst())
        {
            do
            {
                int categoryId = cursor.getInt(cursor.getColumnIndex(myDbHelper.categoryID));
                cursor.close();
                return categoryId;
            }
            while (cursor.moveToNext());

        }
        LOG_REPORT.add("In method getCategoryIdByName - No category ID for category name " + categoryName + "\n");

        return -1;
    }

    public int getSubCategoryIdByName(String subCategoryName) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String wrapedSubCategoryName = wrapStrForDb(subCategoryName);

        Cursor cursor = db.rawQuery("SELECT DISTINCT " + myDbHelper.subCategoryID + " FROM " + myhelper.SUB_CATEGORY_TABLE + " WHERE " + myDbHelper.subCategoryCatName + " = " + wrapedSubCategoryName , null);
        if (cursor.moveToFirst())
        {
            do
            {
                int subCatId = cursor.getInt(cursor.getColumnIndex(myDbHelper.subCategoryID));
                cursor.close();
                return subCatId;
            }
            while (cursor.moveToNext());

        }
        LOG_REPORT.add("In method getSubCategoryIdByName - No sub category ID for sub category name " + subCategoryName + "\n");
        return -1;
    }

    public String getCategoryNameByID(int categoryID)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + myDbHelper.categoryName + " FROM " + myhelper.CATEGORY_TABLE + " WHERE " + myDbHelper.categoryID + " = " + categoryID, null);
        if (cursor.moveToFirst())
        {
            String categoryName = cursor.getString(cursor.getColumnIndex(myDbHelper.categoryName));
            cursor.close();
            return categoryName;
        }
        LOG_REPORT.add("In method getCategoryNameByID - No category name for category ID " + categoryID + "\n");
        return null;
    }

    public String getSubCategoryNameByID(int subCategoryID)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + myDbHelper.subCategoryCatName + " FROM " + myhelper.SUB_CATEGORY_TABLE + " WHERE " + myDbHelper.subCategoryID + " = " + subCategoryID, null);
        if (cursor.moveToFirst())
        {
            String subCategoryName = cursor.getString(cursor.getColumnIndex(myDbHelper.subCategoryCatName));
            cursor.close();
            return subCategoryName;
        }
        LOG_REPORT.add("In method getSubCategoryNameByID - No sub category name for sub category ID " + subCategoryID + "\n");
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean setMonthlyBudgetBalance()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        for (Category cat: month.getCategories())
        {
            int categoryId = getCategoryIdByName(cat.getName());
            int subCategoryId = getSubCategoryIdByName(cat.getSubCategoryName());
            int maxBudgetNumber = getMaxBudgetNumberBGT();
            String strRefMonth = wrapStrForDb(reverseDateString(convertDateToString(month.getMonth(),dateFormat),"/"));

            String setBalanceMB = "UPDATE " + myhelper.MONTHLY_BUDGET_TABLE +
                    " SET " + myDbHelper.monthlyBudgetBalance + " = " + String.valueOf(cat.getRamainingValue() ) +
                    " WHERE " + myDbHelper.monthlyBudgetRefMonth + " = " + strRefMonth +
                    " AND " + myDbHelper.monthlyBudgetCategoryID + " = " + categoryId +
                    " AND " + myDbHelper.monthlyBudgetSubCategoryID + " = " + subCategoryId +
                    " AND " + myDbHelper.monthlyBudgetNumber + " = " + maxBudgetNumber;
            try {
                db.execSQL(setBalanceMB);
            }
            catch(Exception e)
            {
                LOG_REPORT.add("Exception in setMonthlyBudgetBalance method: (ref month: " + strRefMonth + ",category ID: " + categoryId + ")\n" + e.getMessage().toString() + "\n");
                String error = e.getMessage().toString();
                return false;
            }

        }
        return true;
    }

    public ArrayList<String> getAllShops()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String distinctShops = "SELECT DISTINCT " + myDbHelper.transShop + " FROM " + myhelper.TRANSACTION_TABLE;
        Cursor cursor = db.rawQuery( distinctShops, null);
        long status = 0;
        boolean isErrorOccurred = false;
        ArrayList<String> allShops = new ArrayList<>();

        if(cursor.moveToFirst())
        {
            do {
                String shop = cursor.getString(cursor.getColumnIndex(myDbHelper.transShop));
                allShops.add(shop);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        else
        {
            LOG_REPORT.add("No data found in getAllShops method." + "\n");
            status = -1;
            isErrorOccurred = true;
        }
        return allShops;
    }

    public ArrayList<String> getAllMonthesYearMonth(String ascOrDesc)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String distinctMonthesMB = "SELECT DISTINCT " + myDbHelper.monthlyBudgetRefMonth + " FROM " + myhelper.MONTHLY_BUDGET_TABLE + " ORDER BY " + myDbHelper.monthlyBudgetRefMonth + " " + ascOrDesc;
        Cursor cursor = db.rawQuery( distinctMonthesMB, null);
        long status = 0;
        boolean isErrorOccurred = false;
        ArrayList<String> allMonthes = new ArrayList<>();

        if(cursor.moveToFirst())
        {
            do {
                String refernceMonthStr = cursor.getString(cursor.getColumnIndex(myDbHelper.monthlyBudgetRefMonth));
                refernceMonthStr = reverseDateString(refernceMonthStr.replace("'",""),"/");
                Date referenceDate = convertStringToDate(refernceMonthStr,dateFormat);
                String yearMonthStr = getYearMonth(referenceDate,'.');
                allMonthes.add(yearMonthStr);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        else
        {
            LOG_REPORT.add("No data found in getAllMonthesYearMonth method." + "\n");
            status = -1;
            isErrorOccurred = true;
        }

        if(!isErrorOccurred)
            return allMonthes;
        return null;
    }

    public ArrayList<Budget> getBudgetDataFromDB(int BudgetNumberBGT) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        long status = 0;
        boolean isErrorOccurred = false;
        ArrayList<Budget> allBudget =  new ArrayList<>();

        String selectBudgetData = "SELECT " + myDbHelper.budgetCategoryID + "," + myDbHelper.budgetSubCategoryID + "," + myDbHelper.budgetCatPriority + "," + myDbHelper.budgetValue + "," + myDbHelper.budgetIsConstPayment + "," + myDbHelper.budgetShop + "," + myDbHelper.budgetChargeDay + " " +
                "FROM " + myDbHelper.BUDGET_TABLE + " " +
                "WHERE " + myDbHelper.budgetNumber + "=" + BudgetNumberBGT + " ORDER BY " + myDbHelper.budgetCatPriority + " ASC";;
        Cursor cursor = db.rawQuery(selectBudgetData, null);
        if (cursor.moveToFirst()) {
            do {
                int categoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetCategoryID));
                int subCcategoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetSubCategoryID));
                int catPriority = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetCatPriority));
                int budget = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetValue));
                boolean isConstPayment = (cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetIsConstPayment))) == 1;
                String shop = cursor.getString(cursor.getColumnIndex(myDbHelper.budgetShop));
                int chargeDay = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetChargeDay));

                String categoryName = getCategoryNameByID(categoryID);
                String subCategoryName = getSubCategoryNameByID(subCcategoryID);
                allBudget.add(new Budget(catPriority,categoryName, subCategoryName, budget, isConstPayment, shop, chargeDay));

                if (status == -1) {
                    LOG_REPORT.add("No data found in getBudgetDataFromDB method. budget number: " + BudgetNumberBGT + "\n");
                    isErrorOccurred = true;
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return allBudget;
    }

    public ArrayList<Category> getMonthlyBudgetDataFromDB(Date refMonth) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        long status = 0;
        boolean isErrorOccurred = false;
        String wrappedStrRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));
        ArrayList<Category> refMonthCategories =  new ArrayList<>();

        String selectBudgetData = "SELECT " + myDbHelper.monthlyBudgetCategoryID + "," +
                myDbHelper.monthlyBudgetSubCategoryID + "," +
                myDbHelper.monthlyBudget + "," +
                myDbHelper.monthlyBudgetBalance + " " +
                "FROM " + myDbHelper.MONTHLY_BUDGET_TABLE + "," + myDbHelper.BUDGET_TABLE + " " +
                "WHERE " + myDbHelper.monthlyBudgetRefMonth + " = " + wrappedStrRefMonth +
                " and " + myDbHelper.monthlyBudgetNumber + " = " +  myDbHelper.budgetNumber +
                " and " + myDbHelper.monthlyBudgetCategoryID + " = " +  myDbHelper.budgetCategoryID  +
                " and " + myDbHelper.monthlyBudgetSubCategoryID + " = " +  myDbHelper.budgetSubCategoryID + " " +
                "ORDER BY " + myDbHelper.budgetCatPriority + " ASC";
        Cursor cursor = db.rawQuery(selectBudgetData, null);
        if (cursor.moveToFirst()) {
            do {
                int categoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.monthlyBudgetCategoryID));
                int subCcategoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.monthlyBudgetSubCategoryID));

                String categoryName = getCategoryNameByID(categoryID);
                String subCategoryName = getSubCategoryNameByID(subCcategoryID);

                int budget = cursor.getInt(cursor.getColumnIndex(myDbHelper.monthlyBudget));
                double balance = (cursor.getDouble(cursor.getColumnIndex(myDbHelper.monthlyBudgetBalance)));

                ArrayList<Transaction> categoryTrans = getTransactionsRefMonth(categoryName,refMonth);
                refMonthCategories.add(new Category(categoryName, subCategoryName, budget, balance,categoryTrans));

                if (status == -1)
                {
                    LOG_REPORT.add("No data found in getMonthlyBudgetDataFromDB method. ref month: " + wrappedStrRefMonth + "\n");
                    isErrorOccurred = true;
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return refMonthCategories;
    }

    public ArrayList<Transaction> getTransactionsRefMonth(String categoryName, Date refMonth)
    {
        ArrayList<Transaction> categoryTrans = new ArrayList<>();
        SQLiteDatabase db = myhelper.getWritableDatabase();
        int categoryId = getCategoryIdByName(categoryName);
        String wrappedStrRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));
        int MaxID = TRAN_ID_PER_MONTH_NUMERATOR;
        String selectCategories = "SELECT " + //myDbHelper.transID + "," +
                myDbHelper.transIDPerMonth + "," +
                myDbHelper.transSubCategoryID + "," +
                myDbHelper.transPaymentMethod + "," +
                myDbHelper.transShop + "," +
                myDbHelper.transPayDate + "," +
                myDbHelper.transPrice + "," +
                myDbHelper.transRegistrationDate + "," +
                myDbHelper.transIsStorno + "," +
                myDbHelper.transStornoOf + " " +
                "FROM " + myDbHelper.TRANSACTION_TABLE + " " +
                "WHERE " + myDbHelper.transReferenceMonth + " = " + wrappedStrRefMonth + " " +
                "AND " +  myDbHelper.transCategoryID + " = " + categoryId;
        Cursor cursor = db.rawQuery( selectCategories, null);
        if(cursor.moveToFirst()) {
            do {
                int IDPerMonth = cursor.getInt(cursor.getColumnIndex(myDbHelper.transIDPerMonth));
                int subCategoryID = cursor.getInt(cursor.getColumnIndex(myDbHelper.transSubCategoryID));
                String subCategoryName = getSubCategoryNameByID(subCategoryID);
                String paymentMethod = cursor.getString(cursor.getColumnIndex(myDbHelper.transPaymentMethod));
                String shop = cursor.getString(cursor.getColumnIndex(myDbHelper.transShop));
                String SPayDate = reverseDateString(cursor.getString(cursor.getColumnIndex(myDbHelper.transPayDate)),"/");
                Date dPayDate = convertStringToDate(SPayDate, dateFormat);
                double price = cursor.getDouble(cursor.getColumnIndex(myDbHelper.transPrice));
                String sRegistrationDate = cursor.getString(cursor.getColumnIndex(myDbHelper.transRegistrationDate));
                Date dRegistrationDate = convertStringToDate(sRegistrationDate, dateFormat2);
                boolean isStorno = (cursor.getInt(cursor.getColumnIndex(myDbHelper.transIsStorno))) == 1;
                int stornoOf = cursor.getInt(cursor.getColumnIndex(myDbHelper.transStornoOf));

                if(MaxID < IDPerMonth )
                    MaxID = IDPerMonth;
                categoryTrans.add(new Transaction( IDPerMonth, categoryName, subCategoryName, paymentMethod, shop, dPayDate, price, dRegistrationDate, isStorno, stornoOf));
            }
            while (cursor.moveToNext());
        }
        TRAN_ID_PER_MONTH_NUMERATOR = MaxID;
        if(!cursor.moveToFirst())
            LOG_REPORT.add("No data found in getTransactionsRefMonth method. ref month: " + wrappedStrRefMonth + ", category name: " + categoryName + "\n");
        cursor.close();
        return categoryTrans;
    }

    public boolean updateStornoTransaction(Date refMonth, int tranIdPerMonth, int stornoOf)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String strRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));

        String setStornoTransactions = "UPDATE " + myhelper.TRANSACTION_TABLE +
                " SET " + myDbHelper.transIsStorno + " = " + String.valueOf(1) + "," +
                myDbHelper.transStornoOf + " = " + String.valueOf(stornoOf) +
                " WHERE " + myDbHelper.transReferenceMonth + " = " + strRefMonth +
                " AND "   + myDbHelper.transIDPerMonth + " = " + tranIdPerMonth;
        try {
            db.execSQL(setStornoTransactions);
        }
        catch(Exception e)
        {
            LOG_REPORT.add("Exception in updateStornoTransaction method:( ref month: " + strRefMonth + ", tran Id Per Month: " + tranIdPerMonth + ", stornoOf: " + stornoOf + ")\n" + e.getMessage().toString() + "\n");
            return false;
        }
        return true;
    }

    public long insertBudgetTableData(long budgetNumber, int budgetCategoryID,int budgetSubCategoryID, int catPriority, int budgetValue,boolean budgetIsConstPayment, String budgetShop,int budgetChargeDay)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long id = 0;
        // Insert into BUDGET_TABLE
        //contentValues.put(myDbHelper.budgetID, arg1);
        contentValues.put(myDbHelper.budgetNumber, budgetNumber);
        contentValues.put(myDbHelper.budgetCategoryID, budgetCategoryID);
        contentValues.put(myDbHelper.budgetSubCategoryID, budgetSubCategoryID);
        contentValues.put(myDbHelper.budgetCatPriority, catPriority);
        contentValues.put(myDbHelper.budgetValue, budgetValue);
        contentValues.put(myDbHelper.budgetIsConstPayment, budgetIsConstPayment);
        contentValues.put(myDbHelper.budgetShop, budgetShop);
        contentValues.put(myDbHelper.budgetChargeDay, budgetChargeDay);
        id = db.insert(myhelper.BUDGET_TABLE, null, contentValues);
        if(id == -1)
            LOG_REPORT.add("Insertion failed in insertBudgetTableData method. budget number: " + budgetNumber + ", budget category ID: " + budgetCategoryID + "\n");
        return id;
    }

    //public long insertMonthlyBudgetData(Date monthlyBudgetRefMonth, int budgetCategoryID,int budgetSubCategoryID, int catPriority, int monthlyBudgetNumber,int monthlyBudget, double monthlyBudgetBalance)
    public long insertMonthlyBudgetData(Date monthlyBudgetRefMonth, int budgetCategoryID,int budgetSubCategoryID, int monthlyBudgetNumber,int monthlyBudget, double monthlyBudgetBalance)
    {
        String strRefMonth = reverseDateString(convertDateToString(monthlyBudgetRefMonth,dateFormat),"/");
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long id = 0;
        contentValues.put(myDbHelper.monthlyBudgetRefMonth, strRefMonth);
        contentValues.put(myDbHelper.monthlyBudgetCategoryID, budgetCategoryID);
        contentValues.put(myDbHelper.monthlyBudgetSubCategoryID, budgetSubCategoryID);
        //contentValues.put(myDbHelper.monthlyBudgetNumber, catPriority);
        contentValues.put(myDbHelper.monthlyBudgetNumber, monthlyBudgetNumber);
        contentValues.put(myDbHelper.monthlyBudget,monthlyBudget);
        contentValues.put(myDbHelper.monthlyBudgetBalance, monthlyBudgetBalance);

        id = db.insert(myhelper.MONTHLY_BUDGET_TABLE, null, contentValues);
        if(id == -1)
            LOG_REPORT.add("Insertion failed in insertMonthlyBudgetData method. ref month: " + strRefMonth + ", budget category ID: " + budgetCategoryID + "\n");
        return id;
    }

    public long insertTransactionData(Date refMonth,Transaction tran)
    {
        //long transID = tran.getTranSeqID();
        long transIDPerMonth = tran.getID();
        int transCategoryID = getCategoryIdByName(tran.getCategory());
        int transSubCategoryID = getSubCategoryId(transCategoryID,tran.getSubCategory());
        String transPaymentMethod = tran.getPaymentMethod();
        String transShop = tran.getShop();
        Date transPayDate = tran.getPayDate();
        double transPrice = tran.getPrice();
        Date transRegistrationDate = tran.getRegistrationDate();
        boolean transIsStorno = tran.isStorno();
        int transStornoOf = tran.getStornoOf();

        String wrappedTransPayDate = reverseDateString(convertDateToString(transPayDate,dateFormat),"/");
        String wrappedRefMonth = reverseDateString(convertDateToString(refMonth,dateFormat),"/");
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long id = 0;
        // Insert to TRANSACTION_TABLE
        //contentValues.put(myDbHelper.transID, arg1);
        contentValues.put(myDbHelper.transReferenceMonth, wrappedRefMonth);
        contentValues.put(myDbHelper.transCategoryID, transCategoryID);
        contentValues.put(myDbHelper.transSubCategoryID, transSubCategoryID);
        contentValues.put(myDbHelper.transIDPerMonth, transIDPerMonth);
        contentValues.put(myDbHelper.transShop, transShop);
        contentValues.put(myDbHelper.transPaymentMethod, transPaymentMethod);
        contentValues.put(myDbHelper.transPayDate, wrappedTransPayDate);
        contentValues.put(myDbHelper.transPrice, transPrice);
        contentValues.put(myDbHelper.transRegistrationDate, String.valueOf(transRegistrationDate));
        contentValues.put(myDbHelper.transIsStorno, transIsStorno);
        contentValues.put(myDbHelper.transStornoOf, transStornoOf);
        id = db.insert(myhelper.TRANSACTION_TABLE, null, contentValues);
        if(id == -1)
            LOG_REPORT.add("Insertion failed in insertTransactionData method. ref month: " + wrappedRefMonth + ", transaction ID per month: " + transIDPerMonth + ", transaction category ID: " + transCategoryID + ", trans shop: " + transShop +"\n");
        return id;
    }

    public long insertCategoryData(String CategoryName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long id = 0;
        contentValues.put(myDbHelper.categoryName, CategoryName);
        id = db.insert(myhelper.CATEGORY_TABLE, null, contentValues);
        return id;
    }

    public long insertSubCategoryData(int categoryID,String subCategoryName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        long id = 0;
        contentValues.put(myDbHelper.subCategoryCatID, categoryID);
        contentValues.put(myDbHelper.subCategoryCatName, subCategoryName);
        id = db.insert(myhelper.SUB_CATEGORY_TABLE, null, contentValues);
        return id;
    }

    public void deleteDataRefMonth(Date refMonth)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String wrappedRefMonth = wrapStrForDb(reverseDateString(convertDateToString(refMonth,dateFormat),"/"));
        String deleteFromTRN = "DELETE FROM " + myhelper.TRANSACTION_TABLE + " WHERE " +  myhelper.transReferenceMonth + " = " + wrappedRefMonth;
        String deleteFromMB = "DELETE FROM " + myhelper.MONTHLY_BUDGET_TABLE + " WHERE " +  myhelper.monthlyBudgetRefMonth + " = " + wrappedRefMonth;

        db.execSQL(deleteFromTRN);
        db.execSQL(deleteFromMB);
    }

    public int getCategoryId(String categoryName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c1 = db.rawQuery("SELECT " +  myhelper.categoryID + " FROM " + myhelper.CATEGORY_TABLE + " WHERE " + myhelper.categoryName + "='" + categoryName + "'", null);
        if(c1.moveToFirst())
            return c1.getInt(0);
        return -1;
    }

    public int getSubCategoryId(int categoryId, String subCategoryName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " +  myhelper.subCategoryID + " FROM " + myhelper.SUB_CATEGORY_TABLE + " WHERE " +  myhelper.subCategoryCatName + "='" + subCategoryName + "'" , null);//+ " and " + myhelper.subCategoryCatID + "=" + categoryId, null);
        if(c.moveToFirst())
        {
            int subCatId =  c.getInt(0);
            c.close();
            return subCatId;
        }
        return -1;
    }

/*    public String getData(String tableName) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] budgetColumns = {myDbHelper.budgetNumber, myDbHelper.budgetCategoryID, myDbHelper.budgetSubCategoryID, myDbHelper.budgetValue, myDbHelper.budgetIsConstPayment, myDbHelper.budgetShop, myDbHelper.budgetChargeDay};
        String[] monthlyBudgetColumns = {myDbHelper.monthlyBudgetRefMonth, myDbHelper.monthlyBudgetCategoryID, myDbHelper.monthlyBudgetNumber, myDbHelper.monthlyBudgetSubCategoryID, myDbHelper.monthlyBudget, myDbHelper.monthlyBudgetBalance};
        String[] transactionColumns = {myDbHelper.transID, myDbHelper.transCategoryID, myDbHelper.transPaymentMethod, myDbHelper.transShop, myDbHelper.transPayDate, myDbHelper.transPrice, myDbHelper.transRegistrationDate, myDbHelper.transIsStorno, myDbHelper.transStornoOf};

        StringBuffer buffer = new StringBuffer();
        if(tableName.equals(myhelper.BUDGET_TABLE))
        {
            Cursor cursor = db.query(myDbHelper.BUDGET_TABLE, budgetColumns, null, null, null, null, null);
            while (cursor.moveToNext())
            {
                long id = cursor.getLong(cursor.getColumnIndex(myDbHelper.budgetNumber));
                Long categoryID = cursor.getLong(cursor.getColumnIndex(myDbHelper.budgetCategoryID));
                Long subCategoryID = cursor.getLong(cursor.getColumnIndex(myDbHelper.budgetSubCategoryID));
                int budgetValue = cursor.getInt(cursor.getColumnIndex(myDbHelper.budgetValue));
                boolean budgetIsConstPayment = Boolean.valueOf((cursor.getString(cursor.getColumnIndex(myDbHelper.budgetIsConstPayment))));
                String budgetShop = cursor.getString(cursor.getColumnIndex(myDbHelper.budgetShop));
                Date budgetChargeDate = convertStringToDate(cursor.getString(cursor.getColumnIndex(myDbHelper.budgetChargeDay)),dateFormat);

                String categoryName ="";
                String subCategoryName ="";
                Cursor c1 = db.rawQuery("SELECT " +  myhelper.categoryName + " FROM " + myhelper.CATEGORY_TABLE + " WHERE " + myhelper.categoryID + "=" + categoryID, null);
                Cursor c2 = db.rawQuery("SELECT " +  myhelper.subCategoryCatName + " FROM " + myhelper.SUB_CATEGORY_TABLE + " WHERE " + myhelper.subCategoryID + "=" + subCategoryID, null);
                if (c1.moveToFirst())
                    categoryName = c1.getString(0);
                if (c2.moveToFirst())
                    subCategoryName = c2.getString(0);

                buffer.append(id + SEPARATOR + categoryName + SEPARATOR + subCategoryName +
                        budgetValue + SEPARATOR + budgetIsConstPayment + SEPARATOR + budgetShop + SEPARATOR + budgetChargeDate + "\n");
            }
            cursor.close();
        }
        else if(tableName.equals(myhelper.MONTHLY_BUDGET_TABLE))
        {
            Cursor cursor = db.query(myDbHelper.MONTHLY_BUDGET_TABLE, monthlyBudgetColumns, null, null, null, null, null);
            while (cursor.moveToNext())
            {
                String id = cursor.getString(cursor.getColumnIndex(myDbHelper.monthlyBudgetRefMonth));
                String categoryName = cursor.getString(cursor.getColumnIndex(myDbHelper.monthlyBudgetSubCategoryID));
                String categorySonName = cursor.getString(cursor.getColumnIndex(myDbHelper.monthlyBudget));
                double budget = cursor.getInt(cursor.getColumnIndex(myDbHelper.monthlyBudgetBalance));

                buffer.append(id + SEPARATOR + categoryName + SEPARATOR + categorySonName + budget + "\n");
            }
            cursor.close();
        }
        else if(tableName.equals(myhelper.TRANSACTION_TABLE))
        {
            Cursor cursor = db.query(myDbHelper.TRANSACTION_TABLE, budgetColumns, null, null, null, null, null);
            while (cursor.moveToNext())
            {
                long transID = cursor.getLong(cursor.getColumnIndex(myDbHelper.transID));
                String transCategory = cursor.getString(cursor.getColumnIndex(myDbHelper.transCategoryID));
                String transPaymentMethod = cursor.getString(cursor.getColumnIndex(myDbHelper.transPaymentMethod));
                int transShop = cursor.getInt(cursor.getColumnIndex(myDbHelper.transShop));
                boolean transPayDate = Boolean.valueOf((cursor.getString(cursor.getColumnIndex(myDbHelper.transPayDate))));
                String transPrice = cursor.getString(cursor.getColumnIndex(myDbHelper.transPrice));
                Date transRegistrationDate = convertStringToDate(cursor.getString(cursor.getColumnIndex(myDbHelper.transRegistrationDate)),dateFormat);
                boolean transIsStorno = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(myDbHelper.transIsStorno)));
                String transStornoOf = cursor.getString(cursor.getColumnIndex(myDbHelper.transStornoOf));

                buffer.append(transID + SEPARATOR + transCategory + SEPARATOR + transPaymentMethod +
                        transShop + SEPARATOR + transPayDate + SEPARATOR + transPrice + SEPARATOR +
                        transRegistrationDate + SEPARATOR + transIsStorno + SEPARATOR + transStornoOf + "\n");
            }
            cursor.close();
        }
        return buffer.toString();
    }*/

/*    public int delete(String uname) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs = {uname};

        int count = db.delete(myDbHelper.TABLE_NAME, myDbHelper.NAME + " = ?", whereArgs);
        return count;
    }

    public int updateName(String oldName, String newName) {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.NAME, newName);
        String[] whereArgs = {oldName};
        int count = db.update(myDbHelper.TABLE_NAME, contentValues, myDbHelper.NAME + " = ?", whereArgs);
        return count;*/

    static class myDbHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = DB_FILE_NAME + "." + DB_SUFFIX;
        public static final String BUDGET_TABLE = "BUDGET";
        public static final String MONTHLY_BUDGET_TABLE = "MONTHLY_BUDGET";
        public static final String TRANSACTION_TABLE = "TRANSACTIONS";
        public static final String CATEGORY_TABLE = "CATEGORY";
        public static final String SUB_CATEGORY_TABLE = "SUB_CATEGORY";
        //public static final String CATEGORY_TABLE = "categories";

        // Columns of BUDGET_TABLE
        public static final String budgetNumber = "BGT_BUDGET_NUMBER";
        public static final String budgetCategoryID = "BGT_CATEGORY_ID";
        public static final String budgetSubCategoryID = "BGT_SUB_CATEGORY_ID";
        public static final String budgetCatPriority = "BGT_CAT_PRIORITY";
        public static final String budgetValue = "BGT_VALUE";
        public static final String budgetIsConstPayment = "BGT_IS_CONST_PAYMENT";
        public static final String budgetShop = "BGT_SHOP";
        public static final String budgetChargeDay = "BGT_CHARGE_DAY";//charge day

        // Columns of MONTHLY_BUDGET_TABLE
        public static final String monthlyBudgetRefMonth = "MB_REFERENCE_MONTH";
        public static final String monthlyBudgetCategoryID= "MB_CATEGORY_ID";
        public static final String monthlyBudgetSubCategoryID = "MB_SUB_CATEGORY_ID";
        public static final String monthlyBudgetNumber = "MB_BUDGET_NUMBER";
        public static final String monthlyBudget = "MB_BUDGET";
        public static final String monthlyBudgetBalance = "MB_BALANCE";

        // Columns of TRANSACTION_TABLE
        public static final String transID = "TRN_ID";
        public static final String transReferenceMonth = "TRN_REFERENCE_MONTH";
        public static final String transCategoryID = "TRN_CATEGORY_ID";
        public static final String transSubCategoryID= "TRN_SUB_CATEGORY_ID";
        public static final String transIDPerMonth = "TRN_ID_PER_MONTH";
        public static final String transShop = "TRN_SHOP";
        public static final String transPaymentMethod = "TRN_PAYMENT_METHOD";
        public static final String transPayDate = "TRN_PAY_DATE";
        public static final String transPrice = "TRN_PRICE";
        public static final String transRegistrationDate = "TRN_REGISTRATION_DATE";
        public static final String transIsStorno = "TRN_IS_STORNO";
        public static final String transStornoOf = "TRN_STORNO_OF";

        // Columns of CATEGORY_TABLE
        public static final String categoryID = "CAT_ID";
        public static final String categoryName= "CAT_NAME";

        // Columns of SUB_CATEGORY_TABLE
        public static final String subCategoryID = "SC_ID";
        public static final String subCategoryCatID= "SC_CAT_ID";
        public static final String subCategoryCatName= "SC_CAT_NAME";

        //private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private Context context;

        // Here create the DB
        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
            this.context = context;
        }

        //Create the tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + BUDGET_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + MONTHLY_BUDGET_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + TRANSACTION_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + SUB_CATEGORY_TABLE);

                //db.execSQL("DROP TABLE IF EXISTS " + BUDGET_TABLE);
                //db.execSQL("DROP TABLE IF EXISTS " + MONTHLY_BUDGET_TABLE);
                //db.execSQL("DROP TABLE IF EXISTS " + TRANSACTION_TABLE);

                //Cursor c = db.rawQuery("SELECT * from " +  myDbHelper.BUDGET_TABLE , null);

                db.execSQL("create table " + BUDGET_TABLE + "(" +
                        budgetNumber + " INTEGER NOT NULL CHECK(" + budgetNumber + " > 0)," +
                        budgetCategoryID + " INTEGER NOT NULL," +
                        budgetSubCategoryID + " INTEGER NOT NULL," +
                        budgetCatPriority + " INTEGER NOT NULL," +
                        budgetValue + " INTEGER NOT NULL," +
                        budgetIsConstPayment + " BOOLEAN," +
                        budgetShop + " TEXT," +
                        budgetChargeDay + " INTEGER," +
                        "PRIMARY KEY( " + budgetNumber + "," +budgetCategoryID + "," + budgetSubCategoryID +"))");

                db.execSQL("create table " + MONTHLY_BUDGET_TABLE + "(" +
                        monthlyBudgetRefMonth + " DATE NOT NULL," +
                        monthlyBudgetCategoryID + " INTEGER NOT NULL," +
                        monthlyBudgetSubCategoryID + " INTEGER NOT NULL," +
                        monthlyBudgetNumber + " INTEGER NOT NULL," +
                        monthlyBudget + " INTEGER NOT NULL," +
                        monthlyBudgetBalance + " DOUBLE NOT NULL," +
                        "PRIMARY KEY( " + monthlyBudgetRefMonth + "," +monthlyBudgetCategoryID + "," + monthlyBudgetSubCategoryID +"))");
                //"FOREIGN KEY ("+transReferenceMonth+") REFERENCES "+MONTHLY_BUDGET_TABLE+"("+monthlyBudgetRefMonth+")," +
                //"FOREIGN KEY ("+transReferenceMonth+") REFERENCES "+MONTHLY_BUDGET_TABLE+"("+monthlyBudgetRefMonth+"))");

                db.execSQL("create table " + TRANSACTION_TABLE + "(" +
                        transID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        transReferenceMonth + " DATE NOT NULL," +
                        transCategoryID + "  INTEGER NOT NULL," +
                        transSubCategoryID + " INTEGER," +
                        transIDPerMonth + " INTEGER NOT NULL," +
                        transShop + " TEXT," +
                        transPaymentMethod + " TEXT," +
                        transPayDate + " DATE," +
                        transPrice + " DOUBLE NOT NULL," +
                        transRegistrationDate + " DATE," +
                        transIsStorno + " BOOLEAN NOT NULL," +
                        transStornoOf + " INTEGER," +
                        "FOREIGN KEY ("+transReferenceMonth + ", " + transCategoryID + ", " + transSubCategoryID + ") " +
                        "REFERENCES " + MONTHLY_BUDGET_TABLE + "("+monthlyBudgetRefMonth + ", " + monthlyBudgetCategoryID + ", " + monthlyBudgetSubCategoryID + ") " +
                        "ON DELETE CASCADE )");

                db.execSQL("create table " + CATEGORY_TABLE + "(" +
                        categoryID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        categoryName + " TEXT  NOT NULL)");

                db.execSQL("create table " + SUB_CATEGORY_TABLE + "(" +
                        subCategoryID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        subCategoryCatID + " INTEGER  NOT NULL," +
                        subCategoryCatName+ " TEXT ,"+
                        "FOREIGN KEY ("+subCategoryID+") REFERENCES "+BUDGET_TABLE+"("+budgetSubCategoryID+"))");
                db.execSQL("PRAGMA foreign_keys = ON");


            } catch (Exception e) {
                String s = e.getMessage().toString();
                //Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                //Message.message(context,"OnUpgrade");
                db.execSQL("DROP TABLE IF EXISTS " + BUDGET_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + MONTHLY_BUDGET_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + TRANSACTION_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + SUB_CATEGORY_TABLE);
                onCreate(db);
            } catch (Exception e) {
                //Message.message(context,""+e);
            }
        }

    }
}