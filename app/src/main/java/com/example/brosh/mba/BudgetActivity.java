package com.example.brosh.mba;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.global.*;


public class BudgetActivity extends AppCompatActivity {
    //ArrayList<Category> catArray = new ArrayList<Category>();
    //ArrayList<String> catNamesArray = new ArrayList<String>();
    LinearLayout ll;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTitle(String refMonth) {
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        TextView tv = new TextView(getApplicationContext());

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, // Width of TextView
                ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.WHITE);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setText("תקציב חודשי" + "\n" + refMonth);
        tv.setTextSize(18);

        ab.setCustomView(tv);
        ab.setDisplayShowCustomEnabled(true); //show custom title
        ab.setDisplayShowTitleEnabled(false); //hide the default title
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addCategoryRow(String categoryName, String Budget, String Remainder, boolean isExceptionFromBudget)//Bundle savedInstanceState)
    {
        TextView categoryNameTextView = new TextView(BudgetActivity.this);
        TextView budgetTextView = new TextView(BudgetActivity.this);
        TextView remainderTextView = new TextView(BudgetActivity.this);
        LinearLayout newll = new LinearLayout(BudgetActivity.this);

        categoryNameTextView.setText(categoryName);
        remainderTextView.setText(Remainder);
        budgetTextView.setText(Budget);

        budgetTextView.setTextDirection(View.TEXT_DIRECTION_LTR);
        budgetTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        remainderTextView.setTextDirection(View.TEXT_DIRECTION_LTR);
        remainderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        if(categoryName == "סך הכל")
        {
            categoryNameTextView.setTypeface(null, Typeface.BOLD);
            categoryNameTextView.setTextSize(13);
            categoryNameTextView.setTextColor(Color.BLACK);
            budgetTextView.setTypeface(null, Typeface.BOLD);
            budgetTextView.setTextSize(13);
            budgetTextView.setTextColor(Color.BLACK);
            remainderTextView.setTypeface(null, Typeface.BOLD);
            remainderTextView.setTextSize(13);
            remainderTextView.setTextColor(Color.BLACK);
        }

        if(isExceptionFromBudget == true)
        {
            categoryNameTextView.setTextColor(Color.RED);
            budgetTextView.setTextColor(Color.RED);
            remainderTextView.setTextColor(Color.RED);
        }

        Display display = getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        categoryNameTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 3, ViewGroup.LayoutParams.WRAP_CONTENT));
        budgetTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 3, ViewGroup.LayoutParams.WRAP_CONTENT));
        remainderTextView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 3, ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryValueEditText.setTextSize(18);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        newll.addView(categoryNameTextView);
        newll.addView(remainderTextView);
        newll.addView(budgetTextView);

        ll.addView(newll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  onResumeFragments();
        // setCategoriesInGui();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        setTitle(getYearMonth(month.getMonth(), '.'));

        ll = (LinearLayout) findViewById(R.id.LLBudget);

        boolean b = month.getTransChanged();
        setCategoriesInGui();
        //setCloseButton();

        b = month.getTransChanged();
    }
    public void setCloseButton()
    {
        final Button myButton = new Button(this);
        myButton.setText("סגור");
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(BudgetActivity.this);

        LinearLayout.LayoutParams paramLL = (new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,1f));

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(myButton,lp);
        ll.addView(newll);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setCategoriesInGui()
    {
        int budgetTotal = 0;
        double remainderTotal = 0;
        boolean isExceptionFromBudget = false;

        for (Category category : month.getCategories())
        {
            String categoryName = category.getName();
            double remaining = category.getRamainingValue();
            remaining = Math.round(remaining * 100.d)/ 100.0d ;
            int budget = category.getBudgetValue();
            if(remaining < 0)
                isExceptionFromBudget = true;
            addCategoryRow(categoryName,String.valueOf(remaining),String.valueOf(budget),isExceptionFromBudget);

            budgetTotal += budget;
            remainderTotal += remaining;
            isExceptionFromBudget = false;
        }
        remainderTotal =  Math.round(remainderTotal * 100.d) / 100.0d ;
        if(remainderTotal < 0)
            isExceptionFromBudget = true;
        addCategoryRow("סך הכל",String.valueOf(remainderTotal),String.valueOf(budgetTotal),isExceptionFromBudget);

        //setValueGui("BudgetTotal", budgetTotal);
        //setValueGui("RemainderTotal", remainderTotal);
    }

/*    public ArrayList<String> makeStrListCategoriesOriginFile(String separator)
    {
        ArrayList<String> lines = new ArrayList<>();
        String line = "";
        for (Category cat: month.getCategories())
        {
            line = cat.getName() + separator + cat.getBudgetValue() + separator + cat.getRamainingValue();
            lines.add(line);
        }
        return lines;
    }*/

/*
    public void setCategoriesFromGui()
    {
        setCatNamesArray();
        month.getCategories().clear();
        for (String catName:global.catNamesArray)
        {
            int budgetValue = Integer.valueOf(getValueGui("Budget" + catName));
            double remainderValue = Double.valueOf(getValueGui("Remainder" + catName));
            month.getCategories().add(new Category(catName, budgetValue, remainderValue,null));
        }
    }
    // Initialize the list of the categories
    public void initCatArray()
    {
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Monthly Budget";
        boolean isFileExists = false;
        final File filePath = new File(dirPath + "/Monthly Budget.txt");
        if(filePath.exists())
            isFileExists = true;
        if(isFileExists)
            catArray = getCategoriesFromFile(dirPath,"-->", "-->");
        else
        {
            setCategoriesFromGui();
            global.writeCategories(dirPath, "-->", null);
        }
    }

    public void subValue(String categoryName, double value)
    {
        int index = global.getIndexByName(categoryName);
        Category category = catArray.get(index);
        category.subValRemaining(value);
        catArray.set(index,category);
    }*/

/*    public void setIDRemainderArray()
    {
        IDRemainderArray.clear();
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderNavit));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderFood));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderAfternoon));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderLoanBank));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderLoanParentN));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderLoanParentD));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderMortgage));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderGarden));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderInsCars));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderFuel));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderElectric));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderTax));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderWater));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderGas));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderDonations));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderInsLife));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderCells));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderInternet));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderPharm));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderClothes));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderPlay));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderFun));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderInsHouse));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderGames));
        IDRemainderArray.add((TextView)findViewById(R.id.RemainderSavingPlan));
    }

    public void setIDBudgetArray()
    {
        IDBudgetArray.clear();
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetNavit));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetFood));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetAfternoon));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetLoanBank));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetLoanParentN));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetLoanParentD));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetMortgage));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetGarden));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetInsCars));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetFuel));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetElectric));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetTax));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetWater));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetGas));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetDonations));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetInsLife));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetCells));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetInternet));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetPharm));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetClothes));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetPlay));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetFun));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetInsHouse));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetGames));
        IDBudgetArray.add((TextView)findViewById(R.id.BudgetSavingPlan));
    }

    public Date convertStringToDate(String stringDate)
    {
        //String lastTimeDateString = "06/27/2017";
        java.text.DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
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

    public void checkDate()
    {
        Calendar c = Calendar.getInstance();

// set the calendar to start of today
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

// and get that as a Date
        Date today = c.getTime();

        String lastTimeDateString = getDateFromFile();
        Date lastTimeDate = convertStringToDate(lastTimeDateString);
        Date forMonth = lastTimeDate;

        // set the ref month to start of next month
        Calendar cal = Calendar.getInstance();
        cal.setTime(forMonth);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = 1;

        c.set(Calendar.YEAR,year);
        forMonth = c.getTime();
        if (c.before(forMonth))
        {
            // create a new file for this month
            // and close the file of previouse month
        }
    }

*/
}