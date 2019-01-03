package com.example.brosh.mba;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.MainActivity.monthlyBudgetDB;
import static com.example.brosh.mba.global.SEPARATOR;
import static com.example.brosh.mba.global.deleteCurrentMonth;
import static com.example.brosh.mba.global.reasonCheckFileChanged;
import static com.example.brosh.mba.global.setHeaderProperties;

public class Create_Budget_Activity extends AppCompatActivity {

    private ArrayList<Budget> allBudgets;
    private ArrayList<String> allCategories;
    private boolean isInputValid;
    private Display display;
    private int screenWidth;
    private int buttonSize = 120;

    private LinearLayout LLMain;
    //Button to add a row
    private Button addCategoryButton;
    //Button to write all the inserted categories to budget file
    private Button OKButton;
    private LinearLayout newll;
    private Drawable defaultBackground;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void addRow(LinearLayout ll, boolean isWithCloseBtn) {
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        if (LLMain.getChildCount() > 2) {
            LLMain.removeViewAt(LLMain.getChildCount() - 1);// Remove Close button
            LLMain.removeViewAt(LLMain.getChildCount() - 1);// Remove add button
        }
        LLMain.addView(ll);
        setAddAndDeleteButton();// Adding add button
        LinearLayout addButtonRowLL = (LinearLayout) LLMain.getChildAt(LLMain.getChildCount() - 1);
        if (isWithCloseBtn)
            setCloseButton();// Adding close button
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setAddAndDeleteButton() {
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        final ImageButton addRowButton = new ImageButton(this);
        final ImageButton deleteRowsButton = new ImageButton(this);
        addRowButton.setBackgroundDrawable(defaultBackground);
        deleteRowsButton.setBackgroundDrawable(defaultBackground);
        //addRowButton.setTooltipText("הוסף שורה");
        //deleteRowsButton.setTooltipText("נקה");

        addRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isEmptyRowExists = false;
                for (int i = 2; i < LLMain.getChildCount() - 2; i++) {
                    LinearLayout rowLL = (LinearLayout) LLMain.getChildAt(i);
                    EditText categoryNameET = (EditText) rowLL.getChildAt(1);
                    EditText categoryValueET = (EditText) rowLL.getChildAt(2);
                    if (categoryNameET.getText().toString().equals("") ||
                            categoryValueET.getText().toString().equals("")) {
                        isEmptyRowExists = true;
                        break;
                    }
                }
                if (!isEmptyRowExists)
                    add_New_row(null, 0,false, null, 0);
            }
        });

        deleteRowsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LLMain.removeViews(2, LLMain.getChildCount() - 2);
/*                for (int i = 1; i < LLMain.getChildCount() - 2; i++)
                {
                    deleteSpecificRow(i);
                }*/
                add_New_row(null, 0,false, null, 0);
            }
        });
        deleteRowsButton.setImageDrawable(getResources().getDrawable(R.drawable.clean_screen));
        deleteRowsButton.setScaleType(ImageView.ScaleType.FIT_XY);

        addRowButton.setImageDrawable(getResources().getDrawable(R.drawable.add_button_md));
        //addRowButton.setBackground(null);
        //addRowButton.getLayoutParams().width = 40;
        //addRowButton.getLayoutParams().height = 40;
        addRowButton.setScaleType(ImageView.ScaleType.FIT_XY);


        addRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowsButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        addRowButton.setAdjustViewBounds(true);
        deleteRowsButton.setAdjustViewBounds(true);
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(40, 40);

        //addRowButton.setPadding();

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(addRowButton);//,lp);
        newll.addView(deleteRowsButton);
        LLMain.addView(newll);
    }

    public void setCloseButton() {
        final Button closeButton = new Button(this);
        int size = (150 * buttonSize)/100;
        closeButton.setHeight(size);
        closeButton.setTextColor(Color.BLACK);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setText("צור");
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setBudgets();
                if (!isInputValid || allBudgets.size() == 0)
                    return;//showMessageNoButton("אנא הזן תקציב!",false);
                if (monthlyBudgetDB.checkCurrentRefMonthExists())
                    showQuestion("יצירת תקציב חדש תגרום למחיקת נתוני חודש נוכחי, האם להמשיך?");
                else
                    questionTrueAnswer();
            }
        });

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(closeButton, lp);
        LLMain.addView(newll);
    }

    public void setBudgets() {
        allCategories.clear();
        allBudgets.clear();
        for (int i = 2; i < LLMain.getChildCount() - 2; i++) {
            EditText categoryET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(1));
            EditText valueET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(2));
            CheckBox constPaymentCB = ((CheckBox) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(3));
            EditText shopET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(4));
           //EditText chargeDayET = ((EditText) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(5));
            Spinner chargeDaySP = ((Spinner) ((LinearLayout) LLMain.getChildAt(i)).getChildAt(5));


            String category = categoryET.getText().toString().trim();
            String valueStr = valueET.getText().toString().trim();
            boolean constPayment = constPaymentCB.isChecked();
            String shop = shopET.getText().toString().trim();
            //String chargeDayStr = chargeDayET.getText().toString().trim();
            String chargeDayStr = chargeDaySP.getSelectedItem().toString().trim();
            int chargeDay = 0;

            if(!constPayment)
            {
                shopET.setText("");
                shop = null;
                chargeDayStr = "0";
//                chargeDayET.setText(chargeDayStr);
            }
            chargeDay = Integer.valueOf(chargeDayStr);

            if (valueStr.equals(""))
                valueStr = "0";
            int value = Integer.valueOf(valueStr);


            allCategories.add(category);
            //String categorySon = ((EditText)((LinearLayout)LLMain.getChildAt(i)).getChildAt(1)).getText().toString();
            verifyBudgetInput(categoryET, valueET, constPaymentCB, shopET, chargeDaySP);// chargeDayET);
            if (isInputValid)
                allBudgets.add(new Budget(category,value,constPayment,shop,chargeDay));
            else
                return;
        }
    }

    public void verifyBudgetInput(EditText categoryET, EditText valueET, CheckBox constPaymentCB, EditText shopET,Spinner chargeDaySP ){//EditText chargeDayET) {
        isInputValid = true;
        String category = categoryET.getText().toString().trim();
        String valueStr = valueET.getText().toString().trim();
        boolean constPayment = constPaymentCB.isChecked();
        String shop = shopET.getText().toString().trim();
        //String chargeDayStr = chargeDayET.getText().toString().trim();
        String chargeDayStr = chargeDaySP.getSelectedItem().toString().trim();

        if (chargeDayStr.equals(""))
            chargeDayStr = "0";
        int chargeDay = Integer.valueOf(chargeDayStr);

        if (valueStr.equals(""))
            valueStr = "0";
        int value = Integer.valueOf(valueStr);

        //Check duplicate of category
        if (Collections.frequency(allCategories, category) > 1) {
            setErrorEditText(categoryET, "קטגוריה כפולה!");
            isInputValid = false;
        }

        //Check illegal characters
        if (category.contains(SEPARATOR)) {
            setErrorEditText(categoryET, "תו לא חוקי!");
            isInputValid = false;
        }
        //Check illegal category
        if (category.length() == 0) {
            setErrorEditText(categoryET, "נא להזין קטגוריה!");
            isInputValid = false;
        }
        //Check illegal value
        if (value == 0) {
            setErrorEditText(valueET, "נא להזין ערך!");
            isInputValid = false;
        }

/*        if(constPayment && shop.length() == 0 && chargeDayStr.length()  == 0)
        {
            setErrorEditText(shopET, "נא להזין חנות!");
            setErrorEditText(chargeDayET, "נא להזין יום לחיוב!");
            isInputValid = false;
        }*/

        if(constPayment && shop.length() == 0)
        {
            setErrorEditText(shopET, "נא להזין חנות!");
            isInputValid = false;
        }

/*        if(constPayment && chargeDay == 0)
        {
            setErrorEditText(chargeDayET, "נא להזין יום לחיוב!");
            isInputValid = false;
        }*/

/*        if(constPayment && chargeDay > 31)
        {
            setErrorEditText(chargeDayET, "נא להזין יום חוקי לחיוב!");
            isInputValid = false;
        }*/

        //Check illegal characters
        if (shop.contains(SEPARATOR)) {
            setErrorEditText(shopET, "תו לא חוקי!");
            isInputValid = false;
        }

        //allCategories.add(budget.getCategory());
        // Need to reduce duplicates categories by define set of categories
        if (!isInputValid)
            return;
    }

    public void setErrorEditText(EditText et, String errorMesage)
    {
        et.setError(errorMesage);
    }

    public void showQuestion(String message)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        questionTrueAnswer();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        questionFalseAnswer();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton("כן", dialogClickListener)
                .setNegativeButton("לא", dialogClickListener).show();
    }

    public void showMessageNoButton(String message, boolean isFinishNeeded)//View view)
    {
        final AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(message);//.create()
        myAlert.show();

        Handler handler = new Handler();
        if(isFinishNeeded)
            handler.postDelayed(new Runnable() {
                public void run() {
                    myAlert.create().dismiss();
                    finish();
                }
            }, 1000); // 1000 milliseconds delay
        else
        handler.postDelayed(new Runnable() {
            public void run() {
                myAlert.create().dismiss();
                //finish();
            }
        }, 1000); // 1000 milliseconds delay

    }

    public void writeBudget(long budgetNumber)
    {
        long status = 0;
        int categoryID = 0;
        int subCategoryID = 0;
        for (Budget bgt:allBudgets)
        {
            categoryID = monthlyBudgetDB.getCategoryId(bgt.getCategory());
            if(categoryID == -1) {
                status = monthlyBudgetDB.insertCategoryData(bgt.getCategory());
                categoryID = monthlyBudgetDB.getCategoryId(bgt.getCategory());
            }
            subCategoryID = monthlyBudgetDB.getSubCategoryId(categoryID,bgt.getCategorySon());
            if(subCategoryID == -1) {
                status = monthlyBudgetDB.insertSubCategoryData(categoryID, bgt.getCategorySon());
                subCategoryID = monthlyBudgetDB.getSubCategoryId(categoryID, bgt.getCategorySon());
            }
            status = monthlyBudgetDB.insertBudgetTableData(budgetNumber, categoryID, subCategoryID, bgt.getValue(), bgt.isConstPayment(), bgt.getShop(), bgt.getChargeDay());
        }
    }


    private void questionTrueAnswer()
    {
        int budgetNumber = monthlyBudgetDB.getMaxBudgetNumberBGT() + 1;
        writeBudget(budgetNumber);
        deleteCurrentMonth();
        month = null;
        reasonCheckFileChanged = "Write";
        showMessageNoButton("תקציב נוצר בהצלחה!",true);
    }

    private void questionFalseAnswer()
    {
    }

/*    public void writeBudget(ArrayList<String> lines, String fileName, String dirPath) {
        // Writing the categories and budget values to file
        writeToFile(lines, fileName, dirPath);
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);

        defaultBackground = new View(this).getBackground();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on portrait moade only

        display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        addCategoryButton = new Button(Create_Budget_Activity.this);
        OKButton = new Button(Create_Budget_Activity.this);
        newll = new LinearLayout(Create_Budget_Activity.this);

        allBudgets = new ArrayList<>();
        allCategories = new ArrayList<>();
        //setAddButton();
        setTitleRow();
        setBudgetGui();
    }

    public void setTitleRow()
    {
        final LinearLayout titleLL = new LinearLayout(Create_Budget_Activity.this);
        TextView emptyTV, categoryNameTV, categoryValueTV, constPaymentTV, shopTV, payDateTV;

        emptyTV = new TextView(Create_Budget_Activity.this);
        categoryNameTV = new TextView(Create_Budget_Activity.this);
        categoryValueTV = new TextView(Create_Budget_Activity.this);
        constPaymentTV = new TextView(Create_Budget_Activity.this);
        shopTV = new TextView(Create_Budget_Activity.this);
        payDateTV = new TextView(Create_Budget_Activity.this);

        emptyTV.setText("");
        categoryNameTV.setText("קטגוריה");
        categoryValueTV.setText("תקציב");
        constPaymentTV.setText("ת. קבוע");
        shopTV.setText("חנות");
        payDateTV.setText("יום לחיוב");

        //int screenHeight = display.getHeight();
        //categoryNameLabel.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4 , ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryFamilyEditText.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 3 ,ViewGroup.LayoutParams.WRAP_CONTENT));
        //categoryValueLabel.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / 4 ,ViewGroup.LayoutParams.WRAP_CONTENT));

        emptyTV.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, ViewGroup.LayoutParams.WRAP_CONTENT));
        categoryNameTV.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 27/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        categoryValueTV.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 17/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        constPaymentTV.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 12/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        shopTV.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 22/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        payDateTV.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 22/100, ViewGroup.LayoutParams.WRAP_CONTENT));


        //categoryValueEditText.setTextSize(18);
        //LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(screenWidth / 4,categoryNameEditText.getHeight());

        emptyTV.setTextSize(15);
        categoryNameTV.setTextSize(15);
        categoryValueTV.setTextSize(15);
        constPaymentTV.setTextSize(15);
        shopTV.setTextSize(15);
        payDateTV.setTextSize(15);

        setHeaderProperties(emptyTV);
        setHeaderProperties(categoryNameTV);
        setHeaderProperties(categoryValueTV);
        setHeaderProperties(constPaymentTV);
        setHeaderProperties(shopTV);
        setHeaderProperties(payDateTV);

        titleLL.addView(emptyTV);
        titleLL.addView(categoryNameTV);
        titleLL.addView(categoryValueTV);
        titleLL.addView(constPaymentTV);
        titleLL.addView(shopTV);
        titleLL.addView(payDateTV);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        //LinearLayout llMain = (LinearLayout)findViewById(R.id.LLMainCreateBudget);
        LLMain = (LinearLayout) findViewById(R.id.LLMainCreateBudget);
        LLMain.addView(titleLL);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setBudgetGui()
    {
        allBudgets = monthlyBudgetDB.getBudgetDataFromDB(monthlyBudgetDB.getMaxBudgetNumberBGT());
        if(allBudgets == null || allBudgets.size() == 0)
        {
            add_New_row(null, 0, false, null, 0);
            allBudgets = new ArrayList<>();
        }
        else
            for(Budget budget:allBudgets)
            {
                add_New_row(budget.getCategory(), budget.getValue(), budget.isConstPayment(), budget.getShop(), budget.getChargeDay());
                setCloseButton();// Adding close button
            }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void add_New_row(String categoryName, int categoryValue, boolean isConstPayment, String shop, int chargeDay ) {
        boolean isEmptyRow = (categoryName == null && categoryValue == 0 && isConstPayment == false && shop == null && chargeDay == 0);
        final LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);
        final EditText categoryNameET, categoryValueET;
        final EditText shopET;
        //final EditText chargeDayET;
        final Spinner optionalDaysSpinner;
        CheckBox constPaymentCB;

        categoryNameET = new EditText(Create_Budget_Activity.this);
        categoryValueET = new EditText(Create_Budget_Activity.this);
        constPaymentCB = new CheckBox(Create_Budget_Activity.this);
        shopET = new EditText(Create_Budget_Activity.this);
        //chargeDayET = new EditText(Create_Budget_Activity.this);
        optionalDaysSpinner = new Spinner(Create_Budget_Activity.this);
        setSpinnerOptionalDays(optionalDaysSpinner);

        categoryNameET.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 27/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        categoryValueET.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 14/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        constPaymentCB.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 14/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        shopET.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 23/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        //chargeDayET.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 11/100, ViewGroup.LayoutParams.WRAP_CONTENT));
        optionalDaysSpinner.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - buttonSize ) * 22/100, ViewGroup.LayoutParams.WRAP_CONTENT));

        constPaymentCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    shopET.setVisibility(View.VISIBLE);
                    //chargeDayET.setVisibility(View.VISIBLE);
                    optionalDaysSpinner.setVisibility(View.VISIBLE);
                }
                else
                {
                    shopET.setVisibility(View.INVISIBLE);
                    //chargeDayET.setVisibility(View.INVISIBLE);
                    optionalDaysSpinner.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (!isEmptyRow)
            categoryNameET.setText(categoryName);
        //EditText categoryFamilyEditText = new EditText(Create_Budget_Activity.this);
        if (!isEmptyRow)
            categoryValueET.setText(String.valueOf(categoryValue));

        constPaymentCB.setChecked(isConstPayment);

        if (!isEmptyRow)
        {
            shopET.setText(shop);
            //chargeDayET.setText(String.valueOf(chargeDay));
            optionalDaysSpinner.setSelection(chargeDay - 1);
        }

        categoryNameET.requestFocus();
        categoryNameET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        categoryValueET.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        constPaymentCB.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        shopET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        //chargeDayET.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        categoryNameET.setTextSize(12);
        categoryValueET.setTextSize(12);
        constPaymentCB.setTextSize(12);
        shopET.setTextSize(12);
        //chargeDayET.setTextSize(12);

        newll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        newll.setOrientation(LinearLayout.HORIZONTAL);

        optionalDaysSpinner.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(categoryNameET.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(categoryValueET.getWindowToken(), 0);
                //imm.hideSoftInputFromWindow(constPaymentCB.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(shopET.getWindowToken(), 0);
                return false;
            }
        }) ;

        final ImageButton deleteRowButton = new ImageButton(this);
        //deleteRowButton.setTooltipText("מחק שורה");

        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LLMain.removeView(newll);
                //for (int i = 1; i < LLMain.getChildCount() - 2; i++)
                //deleteSpecificRow(i);
                if (LLMain.getChildCount() == 4)
                    add_New_row(null, 0,false, null, 0);
            }
        });
        deleteRowButton.setImageDrawable(getResources().getDrawable(R.drawable.delete_icon));
        deleteRowButton.setBackgroundDrawable(defaultBackground);
        deleteRowButton.setScaleType(ImageView.ScaleType.FIT_XY);
        deleteRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowButton.setAdjustViewBounds(true);

        newll.addView(deleteRowButton);

        if(!constPaymentCB.isChecked())
        {
            shopET.setVisibility(View.INVISIBLE);
            //chargeDayET.setVisibility(View.INVISIBLE);
            optionalDaysSpinner.setVisibility(View.INVISIBLE);
        }
        newll.addView(categoryNameET);
        newll.addView(categoryValueET);
        newll.addView(constPaymentCB);
        newll.addView(shopET);
        //newll.addView(chargeDayET);
        newll.addView(optionalDaysSpinner);

        boolean isWithCloseBtn = isEmptyRow;
        addRow(newll, isWithCloseBtn);
    }

        public void setSpinnerOptionalDays(Spinner OptionalDaysSP)
    {
        ArrayList<String> daysInMonth = new ArrayList<>();
        int i = 1;
        while(i <= 31 )
            daysInMonth.add(String.valueOf(i++));
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner,daysInMonth );
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        OptionalDaysSP.setAdapter(adapter);
        OptionalDaysSP.setSelection(1,true);
    }


    public void deleteSpecificRow(int index) {
        LLMain.removeViewAt(index);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setDeleteButton() {
        final ImageButton deleteRowButton = new ImageButton(this);

        deleteRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

            }
        });
        deleteRowButton.setImageDrawable(getResources().getDrawable(R.drawable.delete_icon));
        //addRowButton.setBackground(null);
        //addRowButton.getLayoutParams().width = 40;
        //addRowButton.getLayoutParams().height = 40;
        deleteRowButton.setScaleType(ImageView.ScaleType.FIT_XY);


        deleteRowButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        deleteRowButton.setAdjustViewBounds(true);
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(40, 40);
        LinearLayout newll = new LinearLayout(Create_Budget_Activity.this);

        //addRowButton.setPadding();

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(deleteRowButton);//,lp);
        LLMain.addView(newll);
    }
}
