package com.example.brosh.mba;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static com.example.brosh.mba.MainActivity.month;

/**
 * Created by daniel.brosh on 7/23/2017.
 */

public class global {
    public static TextView remainder;
    public static TextView budget;
    public static ArrayList<String> catPaymentMethodArray = new ArrayList<String>();
    public static final String dateFormat = "dd/MM/yyyy";
    public static final String dateFormat2 =  "EEE MMM dd HH:mm:ss zzz yyyy";
    //public static String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static int TRAN_ID_PER_MONTH_NUMERATOR = 1;
    public static String DCIM = "";
    public static String PROJECT_PATH = "";
    public static String DB_FOLDER_PATH = "/data/data/monthlybudget.apps.danielbrosh.monthlybudget/databases";
    public static String DB_FILE_NAME = "MonthlyBudget";
    public static String FILE_NAME = "Monthly Budget";
    public static String FILE_NAME_BUDGET = "Budget";
    public static String FILE_NAME_ORG = "Monthly Budget ORG";
    public static String SUFFIX = "txt";
    public static String DB_SUFFIX = "db";
    public static String SEPARATOR = "-->";
    //public static long TRANSACTIONS_ID_NUMERATOR = 0;
    public static char DOWN_ARROW = 'ꜜ';
    public static char UP_ARROW = 'ꜛ';
    public static boolean IS_MONTH_CHANGABLE = false;
    public static Set<String> shopsSet = new TreeSet<String>();

    public static boolean isFirstTime = true;
    public static boolean isDBFileDownloaded = false;
    public static long lastUpdatedTimeMillis = 0;
    public static long myLastUpdatedTimeMillis = 0;
    public static String reasonCheckFileChanged = "Show";
    public static Transaction transactionAddSync = null;
    public static Category categoryUpdateSync = null;


    public static Thread closeActivity;


    public static String DEFAULT_LANGUAGE;
    public static boolean IS_AD_ENEABLED = true;
    public static ArrayList<String> LOG_REPORT = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setHeaderProperties(TextView tv)
    {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        tv.setClickable(true);
        Linkify.addLinks(tv,Linkify.ALL);
    }

    public static String getSentenceCapitalLetter(String sentence,char separator)
    {
        if(sentence.indexOf(separator) == -1)
            return getWordCapitalLetter(sentence);
        return new String(getWordCapitalLetter(sentence.substring(0,sentence.indexOf(separator)+1)) + getSentenceCapitalLetter(sentence.substring(sentence.indexOf(separator)+1),separator));
    }


    public static String getWordCapitalLetter(String word)
    {
        char firstLetter = word.charAt(0);
        if(firstLetter >= 97 && firstLetter <= 122)
            firstLetter -= 32;
        return new String(firstLetter + word.substring(1));
    }

    public static void reverseLinearLayout(LinearLayout linearLayout)
    {
        for(int i = linearLayout.getChildCount()-1 ; i >= 0 ; i--)
        {
            View item = linearLayout.getChildAt(i);
            linearLayout.removeViewAt(i);
            linearLayout.addView(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setLanguageConf(LinearLayout l)
    {
        for (int i = 0;i < l.getChildCount();i++)
        {
            View v = l.getChildAt(i);
            v.setTextDirection(View.TEXT_DIRECTION_LTR);
            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }
        reverseLinearLayout(l);
    }

    public static String wrapStrForDb(String str)
    {
        return "'" + str + "'";
    }

    public static String getYearMonth(Date date,char separator)
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
    }

    public static Date getTodayDate()
    {
        Calendar c = Calendar.getInstance();

// set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date getDateStartMonth()
    {
        Calendar c = Calendar.getInstance();

        // set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    public static void setMyBudget() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("נאוית" + SEPARATOR + 1500 + SEPARATOR + 1500);
        lines.add("אוכל" + SEPARATOR + 2000 + SEPARATOR + 2000);
        lines.add("פארם" + SEPARATOR + 500 + SEPARATOR + 500);
        lines.add("דלק" + SEPARATOR + 1700 + SEPARATOR + 1700);
        lines.add("חינוך" + SEPARATOR + 544 + SEPARATOR + 544);
        lines.add("חוגים" + SEPARATOR + 216 + SEPARATOR + 216);
        //lines.add("הלוואה בנק" + SEPARATOR + 1100 + SEPARATOR + 1100);
        lines.add("הלוואה נאוית" + SEPARATOR + 2100 + SEPARATOR + 2100);
        lines.add("הלוואה דניאל" + SEPARATOR + 500 + SEPARATOR + 500);
        lines.add("משכנתא" + SEPARATOR + 5100 + SEPARATOR + 5100);
        lines.add("מעון" + SEPARATOR + 1584 + SEPARATOR + 1584);
        lines.add("ביטוח רכבים" + SEPARATOR + 450 + SEPARATOR + 450);
        lines.add("חשמל" + SEPARATOR + 350 + SEPARATOR + 350);
        lines.add("גז" + SEPARATOR + 60 + SEPARATOR + 60);
        lines.add("ארנונה" + SEPARATOR + 415 + SEPARATOR + 415);
        lines.add("מים" + SEPARATOR + 100 + SEPARATOR + 100);
        lines.add("סלולרי" + SEPARATOR + 80 + SEPARATOR + 80);
        lines.add("אינטרנט" + SEPARATOR + 105 + SEPARATOR + 105);
        lines.add("ביטוח בריאות וחיים" + SEPARATOR + 30 + SEPARATOR + 30);
        lines.add("ביטוח משכנתא" + SEPARATOR + 130 + SEPARATOR + 130);
        lines.add("תרומות" + SEPARATOR + 104 + SEPARATOR + 104);
        //lines.add("ביגוד" + SEPARATOR + 400 + SEPARATOR + 400);
        lines.add("ארגון המורים" + SEPARATOR + 58 + SEPARATOR + 58);
        lines.add("תרבות" + SEPARATOR + 100 + SEPARATOR + 100);
        lines.add("חברת אחזקה" + SEPARATOR + 280 + SEPARATOR + 280);
        lines.add("תכנית חיסכון" + SEPARATOR + 0 + SEPARATOR + 0);
        lines.add("שונות" + SEPARATOR + 0 + SEPARATOR + 0);
        writeToFile(lines,FILE_NAME,PROJECT_PATH);
    }

    /*    *//*   Input: Separator for category
     Output: A List of all the categories and values of budget file *//*
    public static ArrayList<Budget> getBudgetFromFile(String budgetSeperator)
    {
        String filePath = PROJECT_PATH + "/" + FILE_NAME_BUDGET + "." + SUFFIX;
        ArrayList<String> budgetLines = readLinesFromFile(filePath);
        if(budgetLines == null)
            return null;
        ArrayList<Budget>  BudgetList = new ArrayList<Budget>();
*//*        if(categoriesLines == null)
            return null;*//*
        for (String line  : budgetLines)
        {
            String [] splitterLine = line.split(budgetSeperator);
            String categoryName = splitterLine[0];
            int budget = Integer.valueOf(splitterLine[1]);
            //int remainder = Integer.valueOf(splitterLine[2]);
            boolean isConstPayment = Boolean.valueOf(splitterLine[3]);
            String shop = splitterLine[4];
            int chargeDay = Integer.valueOf(splitterLine[5]);

            if(shop.equals("null"))
                shop = null;

            BudgetList.add(new Budget(categoryName,budget,isConstPayment,shop,chargeDay));
        }
        return BudgetList;
    }*/

    /*  Input: Path of file
    Output: A list with all of the lines from the file input   */
    public static ArrayList<String> readLinesFromFile(String filePath)
    {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        ArrayList<String> lines = null;
        try
        {
            File file = new File(filePath);
            if(file.exists())
            {
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                lines = new ArrayList<String>();
                String line = "";
                while ((line = bufferedReader.readLine()) != null)
                    lines.add(line);
                return lines;
            }
            return null;
        }

        catch (IOException e)
        {
            Log.e("Exception", "File read failed: " + e.toString());
            e.printStackTrace();
        }

        finally {
            if(bufferedReader != null &&  fileReader != null && lines != null) {
                try {
                    bufferedReader.close();
                    fileReader.close();
                    return lines;
                } catch (IOException e) {
                    Log.e("Exception", "File read failed: " + e.toString());
                    String s = e.getMessage();
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void writeToFile(ArrayList<String> data, String fileName, String dirPath)
    {
        if(data == null || data.size() == 0)
            return;
        final File path = new File(dirPath);

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, fileName + "." + SUFFIX);
        FileOutputStream fOut = null;
        OutputStreamWriter myOutWriter = null;
        try
        {
            file.createNewFile();
            fOut = new FileOutputStream(file,false);
            myOutWriter = new OutputStreamWriter(fOut);

            for (String line:data)
            {
                myOutWriter.write(line + "\n");
            }

        }

        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        finally
        {
            if(myOutWriter != null && fOut != null)
            {
                try {
                    myOutWriter.close();
                    fOut.flush();
                    fOut.close();
                }
                catch (IOException e)
                {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        }
    }

    public static void deleteCurrentMonth()
    {
        deleteFiles(PROJECT_PATH + "/" + getYearMonth(getTodayDate(),'-'));
    }

    public static boolean checkBudgetFileExists()
    {
        // try
        // {
        File budgetFile = new File(PROJECT_PATH + "/" + FILE_NAME_BUDGET + "." + SUFFIX);
        if(!budgetFile.exists())
            return false;
        return true;
        //}
/*        catch(Exception e)
        {
            String message = e.getMessage().toString();
            return false;
        }*/
    }

    public static Date getSartOfMonth(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH,1);
        return c.getTime();
    }

    public static ArrayList<String> getAllMonthes()
    {
        String today = convertDateToString(getTodayDate(),dateFormat);
        today  = today.replace('/','.');
        today = today.substring(today.indexOf(".")+ 1);
        ArrayList<String> allMonthes = new ArrayList<>();
        //allMonthes.add(today);
        File root = new File(global.PROJECT_PATH);

        for (File file : root.listFiles())
            if (file.isDirectory())
                allMonthes.add(file.getName().replace('-','.'));
        //Collections.sort(allMonthes,Collections.reverseOrder(COMPARE_String));
        return allMonthes;

    }

    public static void setCatPaymentMethodArray()
    {
        catPaymentMethodArray.clear();
        catPaymentMethodArray.add("כרטיס אשראי");
        catPaymentMethodArray.add("מזומן");
        catPaymentMethodArray.add("צ'ק");
        catPaymentMethodArray.add("העברה בנקאית");
    }

    public  static void strikeThroughText(TextView price)
    {
        price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }


    public static void deleteFiles(String path)
    {
        File dir = new File(path);
        if (dir.isDirectory())
            for (File child : dir.listFiles())
                deleteFiles(child.getPath());
        dir.delete();
    }

    public static void setDelay(final long seconds)
    {
        closeActivity = new Thread(new Runnable() {
            @Override
            public void run () {
                try {
                    Thread.sleep(seconds * 1000);
                    // Do some stuff
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
            }
        });
        closeActivity.run();
    }

/*

    public static String getCategoryHebName(String categoryName)
    {
        return global.catNamesHebArray.get(global.getIndexByName(categoryName)-1);
    }

    public static String getCategoryName(String categoryHebName)
    {
        return global.catNamesArray.get(global.getIndexByHebName(categoryHebName)-1);
    }


    public static ArrayList<Category> getCategoriesByDefault()
    {
        int i = 0;
        ArrayList<Category> catArray = new ArrayList<Category>();
        catArray.add(new Category(catNamesHebArray.get(i++),1500,1500,null));
        catArray.add(new Category(catNamesHebArray.get(i++),2000,2000,null));
        catArray.add(new Category(catNamesHebArray.get(i++),150,150,null));
        catArray.add(new Category(catNamesHebArray.get(i++),1100,1100,null));
        catArray.add(new Category(catNamesHebArray.get(i++),1000,1000,null));
        catArray.add(new Category(catNamesHebArray.get(i++),250,250,null));
        catArray.add(new Category(catNamesHebArray.get(i++),5100,5100,null));
        catArray.add(new Category(catNamesHebArray.get(i++),1584,1584,null));
        catArray.add(new Category(catNamesHebArray.get(i++),450,450,null));
        catArray.add(new Category(catNamesHebArray.get(i++),1700,1700,null));
        catArray.add(new Category(catNamesHebArray.get(i++),350,350,null));
        catArray.add(new Category(catNamesHebArray.get(i++),415,415,null));
        catArray.add(new Category(catNamesHebArray.get(i++),100,100,null));
        catArray.add(new Category(catNamesHebArray.get(i++),60,60,null));
        catArray.add(new Category(catNamesHebArray.get(i++),104,104,null));
        catArray.add(new Category(catNamesHebArray.get(i++),30,30,null));
        catArray.add(new Category(catNamesHebArray.get(i++),80,80,null));
        catArray.add(new Category(catNamesHebArray.get(i++),105,105,null));
        catArray.add(new Category(catNamesHebArray.get(i++),300,300,null));
        catArray.add(new Category(catNamesHebArray.get(i++),400,400,null));
        catArray.add(new Category(catNamesHebArray.get(i++),200,200,null));
        catArray.add(new Category(catNamesHebArray.get(i++),100,100,null));
        catArray.add(new Category(catNamesHebArray.get(i++),130,130,null));
        catArray.add(new Category(catNamesHebArray.get(i++),0,0,null));
        catArray.add(new Category(catNamesHebArray.get(i++),0,0,null));
        return catArray;
    }

    public static void setCatNamesArray()
    {
        catNamesArray.clear();
        catNamesArray.add("Navit");
        catNamesArray.add("Food");
        catNamesArray.add("Afternoon");
        catNamesArray.add("LoanBank");
        catNamesArray.add("LoanParentN");
        catNamesArray.add("LoanParentD");
        catNamesArray.add("Mortgage");
        catNamesArray.add("Garden");
        catNamesArray.add("InsCars");
        catNamesArray.add("Fuel");
        catNamesArray.add("Electric");
        catNamesArray.add("Tax");
        catNamesArray.add("Water");
        catNamesArray.add("Gas");
        catNamesArray.add("Donations");
        catNamesArray.add("InsLife");
        catNamesArray.add("Cells");
        catNamesArray.add("Internet");
        catNamesArray.add("Pharm");
        catNamesArray.add("Clothes");
        catNamesArray.add("Play");
        catNamesArray.add("Fun");
        catNamesArray.add("InsHouse");
        catNamesArray.add("Games");
        catNamesArray.add("SavingPlan");
    }

    public static void setCatArrayHebNames()
    {
        catNamesHebArray.clear();
        catNamesHebArray.add("נאוית");
        catNamesHebArray.add("אוכל");
        catNamesHebArray.add("צהרון");
        catNamesHebArray.add("הלוואה בנק");
        catNamesHebArray.add("הלוואה נאוית");
        catNamesHebArray.add("הלוואה דניאל");
        catNamesHebArray.add("משכנתא");
        catNamesHebArray.add("מעון");
        catNamesHebArray.add("ביטוח רכבים");
        catNamesHebArray.add("דלק");
        catNamesHebArray.add("חשמל");
        catNamesHebArray.add("ארנונה");
        catNamesHebArray.add("מים");
        catNamesHebArray.add("גז");
        catNamesHebArray.add("תרומות");
        catNamesHebArray.add("ביטוח בריאות וחיים");
        catNamesHebArray.add("סלולרי");
        catNamesHebArray.add("אינטרנט");
        catNamesHebArray.add("פארם");
        catNamesHebArray.add("ביגוד");
        catNamesHebArray.add("חוגים");
        catNamesHebArray.add("תרבות");
        catNamesHebArray.add("ביטוח משכנתא");
        catNamesHebArray.add("משחקים");
        catNamesHebArray.add("תכנית חיסכון");
    }

    public static void setBudget(TextView textView)
    {
        global.budget = textView;
    }

    public static void setRemainder(TextView textView)
    {
        global.remainder = textView;
    }

    public static void setBudgetValue(String text)
    {
        global.budget.setText(text);
    }

    public static String getBudgetValue()
    {
        return global.budget.getText().toString();
    }

    public static void setRemainderValue(String text)
    {
        global.remainder.setText(text);
    }

    public static String getRemainderValue()
    {
        return global.remainder.getText().toString();
    }

        public static int getIndexByName(String category)
    {

        switch(category)
        {
            case "Navit":
            {
                return 1;
            }
            case "Food":
            {
                return 2;
            }
            case "Afternoon":
            {
                return 3;
            }
            case "LoanBank":
            {
                return 4;
            }
            case "LoanParentN":
            {
                return 5;
            }
            case "LoanParentD":
            {
                return 6;
            }
            case "Mortgage":
            {
                return 7;
            }
            case "Garden":
            {
                return 8;
            }
            case "InsCars":
            {
                return 9;
            }
            case "Fuel":
            {
                return 10;
            }
            case "Electric":
            {
                return 11;
            }
            case "Tax":
            {
                return 12;
            }
            case "Water":
            {
                return 13;
            }
            case "Gas":
            {
                return 14;
            }
            case "Donations":
            {
                return 15;
            }
            case "InsLife":
            {
                return 16;
            }
            case "Cells":
            {
                return 17;
            }
            case "Internet":
            {
                return 18;

            }
            case "Pharm":
            {
                return 19;

            }
            case "Clothes":
            {
                return 20;

            }
            case "Play":
            {
                return 21;

            }
            case "Fun":
            {
                return 22;

            }
            case "InsHouse":
            {
                return 23;

            }
            case "Games":
            {
                return 24;

            }
            case "SavingPlan":
            {
                return 25;

            }
            case "Total":
            {
                return 26;
            }
        }
        return -1;
    }

    public static int getIndexByHebName(String categoryHeb)
    {

        switch(categoryHeb)
        {
            case "נאוית":
            {
                return 1;
            }
            case "אוכל":
            {
                return 2;
            }
            case "צהרון":
            {
                return 3;
            }
            case "הלוואה בנק":
            {
                return 4;
            }
            case "הלוואה נאוית":
            {
                return 5;
            }
            case "הלוואה דניאל":
            {
                return 6;
            }
            case "משכנתא":
            {
                return 7;
            }
            case "מעון":
            {
                return 8;
            }
            case "ביטוח רכבים":
            {
                return 9;
            }
            case "דלק":
            {
                return 10;
            }
            case "חשמל":
            {
                return 11;
            }
            case "ארנונה":
            {
                return 12;
            }
            case "מים":
            {
                return 13;
            }
            case "גז":
            {
                return 14;
            }
            case "תרומות":
            {
                return 15;
            }
            case "ביטוח בריאות וחיים":
            {
                return 16;
            }
            case "סלולרי":
            {
                return 17;
            }
            case "אינטרנט":
            {
                return 18;

            }
            case "פארם":
            {
                return 19;

            }
            case "ביגוד":
            {
                return 20;

            }
            case "חוגים":
            {
                return 21;

            }
            case "תרבות":
            {
                return 22;

            }
            case "ביטוח משכנתא":
            {
                return 23;

            }
            case "משחקים":
            {
                return 24;

            }
            case "תכנית חיסכון":
            {
                return 25;

            }
            case "סך הכל":
            {
                return 26;
            }
        }
        return -1;
    }
*/

    //  Input: String with date include day
    public static String reverseDateString(String date, String separator)
    {
        String[] l = date.split(separator);
        if(separator == "\\.")
            separator = ".";
        return l[2] + separator + l[1] + separator + l[0];
    }

    public static Date convertStringToDate(String stringDate, String format) // "dd/MM/yyyy"
    {
        //String lastTimeDateString = "06/27/2017";
        java.text.DateFormat df = new SimpleDateFormat(format, Locale.US);
        Date date = null;
        try {
            date = df.parse(stringDate);
            String newStringDate = df.format(date);
            System.out.println(newStringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // need end of month
        return date;
    }

    public static String convertDateToString(Date date, String format)
    {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static void writeToFile2(String content, String path)
    {
        try{
            // Create new file
            File file = new File(path);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(content);

            // Close connection
            bw.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    public static Comparator<Transaction> COMPARE_BY_ID = new Comparator<Transaction>() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public int compare(Transaction one, Transaction other) {
            return Long.compare(one.getID(),other.getID());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_PAYMENT_METHOD = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getPaymentMethod().compareToIgnoreCase(other.getPaymentMethod());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_Category = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getCategory().compareToIgnoreCase(other.getCategory());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_SHOP = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getShop().compareToIgnoreCase(other.getShop());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_TRANSACTION_DATE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getPayDate().compareTo(other.getPayDate());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_PRICE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return Double.compare(one.getPrice(), other.getPrice());
        }
    };

    public static Comparator<Transaction> COMPARE_BY_REGISTRATION_DATE = new Comparator<Transaction>() {
        public int compare(Transaction one, Transaction other) {
            return one.getRegistrationDate().compareTo(other.getPayDate());
        }
    };

    public static Comparator<String> COMPARE_String = new Comparator<String>() {
        public int compare(String one, String other) {
            return one.compareToIgnoreCase(other);
        }
    };

/*    public static Comparator<Budget> COMPARE_BY_CATEGORY_PRIORITY = new Comparator<Budget>() {
        public int compare(Budget one, Budget other) {
            return Integer.compare(one.getCatPriority(), other.getCatPriority());
        }
    };*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void sort(String sortBy, char ascOrDesc)
    {
        if(sortBy.equals("מזהה"))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(),global.COMPARE_BY_ID);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_ID));
        }
        else if(sortBy.equals(getWordCapitalLetter("קטגוריה")))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(),global.COMPARE_BY_Category);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_Category));
        }
        else if(sortBy.equals(getSentenceCapitalLetter("א.תשלום",'.')))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_PAYMENT_METHOD);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_PAYMENT_METHOD));
        }
        else if(sortBy.equals(getWordCapitalLetter("חנות")))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_SHOP);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_SHOP));
        }
        else if(sortBy.equals(getSentenceCapitalLetter("ת.עסקה",'.')))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_TRANSACTION_DATE);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_TRANSACTION_DATE));
        }
        else if(sortBy.equals(getWordCapitalLetter("סכום")))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_PRICE);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_PRICE));
        }
        else if(sortBy.equals(getSentenceCapitalLetter("ת.רישום",'.')))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_REGISTRATION_DATE);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_REGISTRATION_DATE));
        }
/*
        else if(sortBy.equals(language.regisrationDateName))
        {
            if(ascOrDesc == UP_ARROW)
                Collections.sort(month.getTransactions(), global.COMPARE_BY_CATEGORY_PRIORITY);
            else if(ascOrDesc == DOWN_ARROW)
                Collections.sort(month.getTransactions(), Collections.reverseOrder(global.COMPARE_BY_CATEGORY_PRIORITY));
        }*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void copyFile(String sourceFilePath, String destinationtDirPath)
    {
        File src = new File(sourceFilePath);
        File destDir = new File(destinationtDirPath);
        File destFile = new File(destinationtDirPath + "/" + DB_FILE_NAME + "." + DB_SUFFIX);
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
            String s = e.getMessage().toString();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void copyFile(String sourceFilePath, String destinationtDirPath,String fileName, String suffix)
    {
        File src = new File(sourceFilePath);
        File destDir = new File(destinationtDirPath);
        File destFile = new File(destinationtDirPath + "/" + fileName + "." + suffix);
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

}
