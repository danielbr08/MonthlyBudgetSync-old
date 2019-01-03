package com.example.brosh.mba;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.MainActivity.monthlyBudgetDB;
import static com.example.brosh.mba.global.FILE_NAME;
import static com.example.brosh.mba.global.PROJECT_PATH;
import static com.example.brosh.mba.global.SUFFIX;
import static com.example.brosh.mba.global.TRAN_ID_PER_MONTH_NUMERATOR;
import static com.example.brosh.mba.global.convertStringToDate;
import static com.example.brosh.mba.global.dateFormat;
import static com.example.brosh.mba.global.getAllMonthes;
import static com.example.brosh.mba.global.getYearMonth;
import static com.example.brosh.mba.global.reverseDateString;

/**
 * Created by daniel.brosh on 10/1/2017.
 */

public class Month
{
    //private long transactionsIdNumerator;
    private Date refMonth;
    private String dirPath;
    private boolean isActive;
    private ArrayList<Category> categories;
    private ArrayList<Transaction> transactions;
    private boolean isTransChanged;

/*    public void setTransactionsIdNumerator(long transactionsIdNumerator) {
        this.transactionsIdNumerator = transactionsIdNumerator;
    }

    public long getTransactionsIdNumerator() {
        return transactionsIdNumerator;
    }*/

    public boolean getTransChanged()
    {
        return isTransChanged;
    }
    public void setTransChanged(boolean transChanged)
    {
        isTransChanged = transChanged;
    }

    public void updateFile(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Month(Date refMonth)
    {
        this.refMonth = refMonth;
        dirPath  = PROJECT_PATH + "/" + getYearMonth(refMonth,'-');
        isActive = false;
        categories = new ArrayList<Category>();
        transactions = new ArrayList<Transaction>();
        isTransChanged = false;

        Calendar c = Calendar.getInstance();
// set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));
        Date today = c.getTime();

        if(refMonth.getYear() == today.getYear() && refMonth.getMonth() == today.getMonth())
            isActive = true;
        initCategories();
    }

    public Set<String> getSetAllShops()
    {
        Set<String> shopSet = new TreeSet<String>();
        for (Transaction tran:getTransactions())
        {
            shopSet.add(tran.getShop());
        }
        return shopSet;
    }

    ArrayList<String> getCategoriesNames()
    {
        ArrayList<String> categoriesNames = new ArrayList<>();
        for (Category cat:categories)
        {
            categoriesNames.add(cat.getName());
        }
        return categoriesNames;
    }

    public Date getMonth() {
        return refMonth;
    }

    public String getDirPath() {
        return dirPath;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public ArrayList<Transaction> getTransactions(String categoryName)
    {
        for (Category cat: categories)
        {
            if(cat.getName().equals(categoryName))
                return cat.getTransactions();
        }
        return transactions;
    }

    public void setMonth(Date month) {
        this.refMonth = month;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setAllTransactions()
    {
        transactions = new ArrayList<Transaction>();
        for (Category cat:categories)
        {
            ArrayList<Transaction> trans = cat.getTransactions();
            if(trans != null && trans.size() > 0)
                transactions.addAll(trans);
        }
    }
/*
    public String getYearMonth(Date date,char separator)
    {
        int month = date.getMonth() + 1;
        String monthStr = String.valueOf(month);
        if(month < 10)
            monthStr = "0" + month;

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        String yearStr = String.valueOf(year);

        return yearStr + separator + monthStr;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initCategories()
    {
        if(!monthlyBudgetDB.checkCurrentRefMonthExists())
        {
            monthlyBudgetDB.writeMBFromBudget(this.refMonth);
            setTransChanged(true);
        }
        TRAN_ID_PER_MONTH_NUMERATOR = 1;
        categories = getCategoriesFromDB();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Month> getAllMonthesObj()
    {
        ArrayList<Month> allMonthes = new ArrayList<>();
        for (String refMonth: getAllMonthes())
        {
            refMonth = (refMonth + ".01");
            refMonth = reverseDateString(refMonth, "\\.");
            refMonth = refMonth.replace('.', '/');
            // Close the current month by writing to files+

            allMonthes.add(new Month(convertStringToDate(refMonth, dateFormat)));
        }
        return allMonthes;
    }

    public void setTransactions(String catName)
    {
        transactions.clear();
        boolean getAll = false;

        if( catName.equals("הכל") )
            getAll = true;
        for(Category cat: month.categories)
        {
            if( getAll == false && !catName.equals(cat.getName()) )
                continue;
            ArrayList <Transaction> catTrans = cat.getTransactions();
            if( catTrans != null && catTrans.size() > 0)
            {
                for (Transaction tran : catTrans)
                    transactions.add(tran);
                if (getAll == false && transactions.size() > 0)
                    return;
            }
        }
    }

/*   Input: Path of current month directory and separators for category and transactions files
     Output: A List of all the categories from the main file(category including all her transactions)   *//*
    public ArrayList<Category> getCategoriesFromFile(String categoriesSeperator, String transactionsSeparator)
    {
        String filePath = dirPath + "/" + FILE_NAME + "." + SUFFIX;
        ArrayList<String> categoriesLines = readLinesFromFile(filePath);
        ArrayList<Category>  Categories = new ArrayList<Category>();
        transactions.clear();
*//*        if(categoriesLines == null)
            return null;*//*
        for (String line  : categoriesLines)
        {
            String [] splitterLine = line.split(categoriesSeperator);
            String categoryName = splitterLine[0];
            int budget = Integer.valueOf(splitterLine[1]);
            double remaining = Double.valueOf(splitterLine[2]);
            ArrayList<Transaction> categoryTransactions = getTransactionsFromFile(dirPath + "/Transactions/" + categoryName + "." + SUFFIX ,transactionsSeparator);
            Categories.add(new Category(categoryName,budget,remaining,categoryTransactions));
            if(categoryTransactions != null && categoryTransactions.size() > 0)
                transactions.addAll(categoryTransactions);
        }
        return Categories;
    }*/

    /*   Input: Path of current month directory and separators for category and transactions files
     Output: A List of all the categories from the main file(category including all her transactions)   */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Category> getCategoriesFromDB()
    {
        //return null;
        ArrayList<Category> Categories = new ArrayList<>();
        Categories = monthlyBudgetDB.getMonthlyBudgetDataFromDB(this.refMonth);
        transactions.clear();
        if(Categories == null || Categories.size() == 0)
            return null;
        for (Category cat : Categories)
        {
            ArrayList<Transaction> categoryTransactions = new ArrayList<>();
            //categoryTransactions = getTransactionsRefMonth(cat.getName(), this.refMonth);
            categoryTransactions = cat.getTransactions();
            cat.setTransactions(categoryTransactions);
            if(categoryTransactions != null && categoryTransactions.size() > 0)
                transactions.addAll(categoryTransactions);
        }
        TRAN_ID_PER_MONTH_NUMERATOR = monthlyBudgetDB.getMaxIDPerMonthTRN(this.refMonth) + 1;
        return Categories;
    }

    public void updateMonthData(int fromIdPerMonth)
    {
        ArrayList<Transaction> allTransactions = (ArrayList<Transaction>)this.getTransactions();
        if(allTransactions == null)
        {
            month.setTransChanged(false);
            return;
        }
        for (Transaction tran:allTransactions)
        {
            if( tran.getID()  >= fromIdPerMonth )
                monthlyBudgetDB.insertTransactionData(this.getMonth(),tran);
        }
        monthlyBudgetDB.setMonthlyBudgetBalance();
        month.setTransChanged(false);
    }

/*
    *//*     Input: Path of transaction file in specific category
           Output: A List of all the category transactions from the transactions file   *//*
    public ArrayList<Transaction> getTransactionsFromFile(String filePath, String seperator)
    {
        ArrayList<String> lines = readLinesFromFile(filePath);
        if (lines == null)
            return null;
        ArrayList<Transaction>  transactions = new ArrayList<Transaction>();
        int MaxID = TRAN_ID_PER_MONTH_NUMERATOR;
        for (String line  : lines)
        {
            String [] transaction = line.split(seperator);
            int i = 0;
            //long tranSeqID = Long.valueOf(transaction[i++]);
            int ID = Integer.valueOf(transaction[i++]);
            String category = transaction[i++];
            String paymentMethod = transaction[i++];
            String Shop = transaction[i++];
            Date payDate = convertStringToDate(transaction[i++],dateFormat);
            double price = Double.valueOf(transaction[i++]);
            Date registrationDate = convertStringToDate(transaction[i++],dateFormat);
            boolean isStorno = Boolean.valueOf(transaction[i++]);
            int stornoOf = Integer.valueOf(transaction[i++]);
            transactions.add(new Transaction( ID,category,paymentMethod,Shop,payDate,price,registrationDate,isStorno,stornoOf));
            if(MaxID < ID )
                MaxID = ID;
        }
        TRAN_ID_PER_MONTH_NUMERATOR = MaxID;
        return transactions;
    }*/
/*
//  Input: Path of current month directory and separators for category and transactions files
//  Output: A List of all the categories from the main file(category including all her transactions)
    public void writeCategories(String categoriesSeparator, String transactionsSeparator)
    {
        String pathMainFile = dirPath; //+ "/" + fileName;
        String transactionsDirPath = dirPath + "/Transactions/";
        ArrayList<String> lines = new ArrayList<String>();
        for (Category category : categories)
        {
            String categoryName = category.getName();
            // String categoryHebName = getCategoryHebName(categoryName);
            String remaining = String.valueOf(category.getRamainingValue());
            String budget = String.valueOf(category.getBudgetValue());
            lines.add(categoryName + categoriesSeparator + budget + categoriesSeparator + remaining);
            ArrayList<Transaction> catTrans = category.getTransactions();
            if(catTrans.size() > 0 )
                writeTransactions(transactionsDirPath, categoryName, catTrans , transactionsSeparator);
        }
        writeToFile(lines, FILE_NAME, pathMainFile);
    }

    *//*   Input: Path of transaction file for writing and separator
     Action: Create a List of all the transaction for specific category and write it in the filePath   *//*
    public void writeTransactions(String transactionsDirPath,String categoryName, ArrayList<Transaction> transactions, String transactionsSeperator)
    {
        String transactionsFilePath =  transactionsDirPath + categoryName + ".txt";
        ArrayList<String> lines = new ArrayList<String>();
        for (Transaction tran : transactions )
        {
            String ID = String.valueOf(tran.getID());
            String shop = tran.getShop();
            String paymentMethod =  tran.getPaymentMethod();
            String payDate = convertDateToString(tran.getPayDate(),dateFormat);
            String price = String.valueOf(tran.getPrice());
            String isStorno = String.valueOf(tran.getIsStorno());
            String stornoOf = String.valueOf(tran.getStornoOf());
            String registrationDate = convertDateToString(tran.getRegistrationDate(),dateFormat);
            lines.add(transactionsSeperator + ID + transactionsSeperator + categoryName + transactionsSeperator + paymentMethod + transactionsSeperator + shop  + transactionsSeperator + payDate + transactionsSeperator + price + transactionsSeperator +registrationDate + transactionsSeperator + isStorno + transactionsSeperator + stornoOf);
        }
        writeToFile(lines, categoryName, transactionsDirPath);
    }*/

/*    @RequiresApi(api = Build.VERSION_CODES.O)
    public void readFromFile()
    {
        final File filePath = new File(dirPath + "/" + FILE_NAME + "." + SUFFIX);

        if(!filePath.exists())
            copyOriginalFile(dirPath);
    }*/

/*    @RequiresApi(api = Build.VERSION_CODES.O)
    public void copyOriginalFile(String copyTo)
    {
        String monthStr = getYearMonth(refMonth,'-');
        copyFile(PROJECT_PATH + "/" + FILE_NAME_ORG, PROJECT_PATH + "/" + monthStr);

    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void copyFile(String sourceFilePath, String destinationtDirPath)
    {
        File src = new File(sourceFilePath);
        File destDir = new File(destinationtDirPath);
        File destFile = new File(destinationtDirPath + "/" + FILE_NAME + "." + SUFFIX);
        try {
            // make sure the target file exists
            if (src.exists())
            {
                if(!destDir.exists())
                    destDir.mkdir();

               InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(destFile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
            else
                {
                    // Get error file not found
                }
        }
        catch(Exception e)
        {
        }
    }

    /*Input:  Path of file
      Output: A list with all of the lines from the file input*/
    public void writeLinesToFile(String filePath, ArrayList<String> lines)
    {
        try
        {
            File file = new File(filePath);
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedReader = new BufferedWriter(fileWriter);
            for ( String line:lines )
            {
                bufferedReader.write(line);
                bufferedReader.newLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

/*    public static void addTransToTransArray(ArrayList<Transaction> listToAdd)
    {
        for (Transaction tran :listToAdd )
            transArray.add(tran);
    }*/
}
