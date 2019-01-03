package com.example.brosh.mba;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.MainActivity.monthlyBudgetDB;
import static com.example.brosh.mba.R.id.paymentMethodSpinner;
import static com.example.brosh.mba.global.TRAN_ID_PER_MONTH_NUMERATOR;
import static com.example.brosh.mba.global.categoryUpdateSync;
import static com.example.brosh.mba.global.convertStringToDate;
import static com.example.brosh.mba.global.dateFormat;
import static com.example.brosh.mba.global.getDateStartMonth;
import static com.example.brosh.mba.global.getTodayDate;
import static com.example.brosh.mba.global.getYearMonth;
import static com.example.brosh.mba.global.reasonCheckFileChanged;
import static com.example.brosh.mba.global.reverseDateString;
import static com.example.brosh.mba.global.shopsSet;
import static com.example.brosh.mba.global.transactionAddSync;

public class InsertTransactionActivity extends AppCompatActivity
{
    Spinner categoriesSpinner;
    Spinner paymentTypeSpinner;
    Button btnSendTransaction;
    Button btnClose;
    EditText payDateEditText;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTitle(String refMonth)
    {
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
        tv.setText("תקציב חודשי" + "\n"  + refMonth);
        tv.setTextSize(18);

        ab.setCustomView(tv);
        ab.setDisplayShowCustomEnabled(true); //show custom title
        ab.setDisplayShowTitleEnabled(false); //hide the default title
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void init()
    {
        //global.setCatArrayHebNames();
        ArrayAdapter<String> adapter;
         adapter = new ArrayAdapter<String>(this,
               R.layout.custom_spinner, month.getCategoriesNames());
          //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         categoriesSpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, global.catPaymentMethodArray);
        paymentTypeSpinner.setAdapter(adapter);

        if(month.getTransChanged())
        {
            int maxIDPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth());
            month.updateMonthData(maxIDPerMonth + 1);
        }

        if(shopsSet.size() == 0)
            shopsSet.addAll( monthlyBudgetDB.getAllShops());
        ArrayList<String> shopsList = new ArrayList<String>(shopsSet);
        AutoCompleteTextView shposAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.shopAutoCompleteTextView);
        ArrayAdapter<String> anotherAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shopsList);
        shposAutoCompleteTextView.setAdapter(anotherAdapter);
        shposAutoCompleteTextView.setThreshold(1);// Set auto complete from the first character
    }

    public String getCurrentDate()
    {
        Calendar mcurrentDate=Calendar.getInstance();
        int mYear=mcurrentDate.get(Calendar.YEAR);
        int mMonth=mcurrentDate.get(Calendar.MONTH);
        int mDay= mcurrentDate.get(Calendar.DAY_OF_MONTH);

        String day,month;
        if(mDay < 10)
            day = "0" + mDay;
        else
            day = String.valueOf(mDay);
        if( (mMonth + 1) < 10 )
            month = "0" + (int)(mMonth + 1) ;
        else
            month = String.valueOf(mMonth + 1);
        return (day + "/" + month + "/" +  mYear);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_transaction);
        setTitle( getYearMonth(month.getMonth(),'.'));
        categoriesSpinner =  (Spinner) findViewById(R.id.categorySpinner);
        paymentTypeSpinner =  (Spinner) findViewById(paymentMethodSpinner);
        btnSendTransaction = (Button) findViewById(R.id.sendTransactionButton);
        //btnClose = (Button) findViewById(R.id.closeInsertTransactionButton);
        init();

        payDateEditText = (EditText) findViewById(R.id.payDatePlainText);
        payDateEditText.setText(getCurrentDate());
        //payDateEditText.setEnabled(false);

        payDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                Calendar mcurrentDate=Calendar.getInstance();
                int mYear=mcurrentDate.get(Calendar.YEAR);
                int mMonth=mcurrentDate.get(Calendar.MONTH);
                int mDay= mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(InsertTransactionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                    /*      Your code   to get date and time    */
                     String day,month;
                    if(selectedday < 10)
                        day = "0" + selectedday;
                    else
                        day = String.valueOf(selectedday);
                     if( (selectedmonth + 1) < 10 )
                        month = "0" + (int)(selectedmonth + 1) ;
                     else
                         month = String.valueOf(selectedmonth + 1);
                     payDateEditText.setText(day + "/" + month + "/" +  selectedyear);
                     payDateEditText.setError(null);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("בחר תאריך");
                mDatePicker.show();  }
        });


        //Intent i = getIntent();
        // Receiving the Data
        //String name = i.getStringExtra("name");
        // String email = i.getStringExtra("email");

        // Displaying Received data
        //txtName.setText(name);
        //txtEmail.setText(email);

        // Binding Click event to Button
//        btnClose.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View arg0) {
//                //Closing SecondScreen Activity
//                finish();
//            }
//        });

        btnSendTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                EditText shopET = ((EditText) findViewById(R.id.shopAutoCompleteTextView));
                EditText payDateET = ((EditText) findViewById(R.id.payDatePlainText));
                EditText transactionPriceET = ((EditText) findViewById(R.id.transactionPricePlainText));
                if( setErrorEditText(payDateET) || setErrorEditText(transactionPriceET) )
                    return;
                //Insert data
                String categoryName = categoriesSpinner.getSelectedItem().toString();

                String paymentMethod = paymentTypeSpinner.getSelectedItem().toString();
                //String category = getCategoryName(categoryHeb);
                String shop = shopET.getText().toString();
                Date payDate = global.convertStringToDate( payDateET.getText().toString(), dateFormat);
                double transactionPrice = Double.valueOf(transactionPriceET.getText().toString());
                int idPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth()) + 1;
                //init on create
                //int idPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth()) + 1;
                //long transID = monthlyBudgetDB.getMaxIDTRN() + 1;

                Transaction transaction = new Transaction( TRAN_ID_PER_MONTH_NUMERATOR++, categoryName,paymentMethod , shop, payDate, transactionPrice, new Date());
                boolean isStorno = false;
                int stornoOf = -1;

                for (Category cat : month.getCategories())
                    if (categoryName.equals(cat.getName()))
                    {
                        cat.subValRemaining(transactionPrice);
                        ArrayList<Transaction> catTrans =  cat.getTransactions();
                        for (Transaction tran : catTrans)
                        {
                            isStorno = tran.isStorno(transaction);
                            if(isStorno == true)
                            {
                                stornoOf = tran.getID();
                                tran.setIsStorno(true);
                                tran.setStornoOf(transaction.getID());
                                break;
                            }
                        }
                        transaction.setIsStorno(isStorno);
                        transaction.setStornoOf(stornoOf);
                        cat.addTransaction(transaction);
                        categoryUpdateSync = cat;
                        break;
                    }
                month.setTransChanged(true);
                shopsSet.add(shop);

                transactionAddSync = transaction;
                //Maybe will do problems
                month.getTransactions().add(transaction);
                reasonCheckFileChanged = "Write";
                // send message and close window
                //showMessage("העסקה הוכנסה בהצלחה!");
                showMessageNoButton("העסקה הוכנסה בהצלחה!");

                //finish();
            }
        });
    }

    public void showMessage(String message)//View view)
    {
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(message).setPositiveButton("סגור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }).setTitle("הודעה")
                .create();
        myAlert.show();
    }

    public boolean setErrorEditText(EditText et)
    {
        if( et.length() == 0 )
        {
            et.setError("שדה חובה!");
            return true;
        }
        return false;
    }

    public void showMessageNoButton(String message)//View view)
    {
        final AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setMessage(message);//.create()
        myAlert.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                myAlert.create().dismiss();
                finish();
            }
        }, 1000); // 3000 milliseconds delay

    }
}