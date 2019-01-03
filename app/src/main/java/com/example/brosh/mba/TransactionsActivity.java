package com.example.brosh.mba;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import static com.example.brosh.mba.MainActivity.month;
import static com.example.brosh.mba.global.DOWN_ARROW;
import static com.example.brosh.mba.global.TRAN_ID_PER_MONTH_NUMERATOR;
import static com.example.brosh.mba.global.UP_ARROW;
import static com.example.brosh.mba.global.convertDateToString;
import static com.example.brosh.mba.global.dateFormat;
import static com.example.brosh.mba.global.getYearMonth;
import static com.example.brosh.mba.global.isFirstTime;
import static com.example.brosh.mba.global.sort;

public class TransactionsActivity extends AppCompatActivity {

    LinearLayout ll;
    Spinner categoriesSpinner;
    ArrayList<TextView []> textViews = new ArrayList<TextView []>();

    int widthDisplay;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthDisplay = dm.widthPixels;
/*        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            DisplayMetrics dm = new DisplayMetrics();
            this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
            widthDisplay = dm.widthPixels;
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {

        }*/
    }

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

    public void init()
    {
        //global.setCatArrayHebNames();

        ArrayList<String> spinnerCategories = new ArrayList<String>(month.getCategoriesNames());
        spinnerCategories.add(0,"הכל");

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner, spinnerCategories);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapter);
    }

    public int getIndexRowById(long ID)
    {
        for (int i = 0; i< textViews.size(); i++)
        {
            if(textViews.get(i)[0].getText().toString().equals(String.valueOf(ID)))
                return i;
        }
        return -1;
    }

    public TextView[] getStornoRow(String catName, Transaction transaction)
    {
        ArrayList<Transaction> categoryTrans = month.getTransactions(catName);
        if(categoryTrans == null || transaction == null)
            return null;
        for (Transaction tran:categoryTrans)
        {
            if(tran == transaction)
                continue;
            if( transaction.getIsStorno() == false)
                continue;
            int i = getIndexRowById(transaction.getID());
            if(i == -1)
                return null;
            return textViews.get(i);
        }
        return null;
    }

    public void setTVLayoutParams(TextView TV,int width)
    {
        LinearLayout.LayoutParams paramsTV = (new LinearLayout.LayoutParams(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TV.setLayoutParams(paramsTV);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addCategoryRow(Transaction tran, String ID, String categoryName,String paymentMethod, String shop, String tranDate, String tranPrice, boolean isIncludeCategory, boolean isHeaderLine)
    {
        TextView IDTV = new TextView(TransactionsActivity.this);
        TextView catNameTV = new TextView(TransactionsActivity.this);
        TextView paymentMethodTV = new TextView(TransactionsActivity.this);
        TextView shopTV = new TextView(TransactionsActivity.this);
        TextView tranDateTV = new TextView(TransactionsActivity.this);
        TextView tranPriceTV = new TextView(TransactionsActivity.this);

        TextView [] row = new TextView[6];
        int i = 0;
        row[i++] = IDTV;
        row[i++] = catNameTV;
        row[i++] = paymentMethodTV;
        row[i++] = shopTV;
        row[i++] = tranDateTV;
        row[i++] = tranPriceTV;

        textViews.add(row);
        

        if(isIncludeCategory == true )
        {
           // if (isHeaderLine == true)
            catNameTV.setText(categoryName);
           // else
           //     catNameTV.setText(catNamesHebArray.get(global.getIndexByName(categoryName)-1));
        }
        IDTV.setText(ID);
        shopTV.setText(shop);
        paymentMethodTV.setText(paymentMethod);
        tranDateTV.setText(String.valueOf(tranDate));
        tranPriceTV.setText(String.valueOf(tranPrice));

        LinearLayout newll = new LinearLayout(TransactionsActivity.this);

        TableLayout.LayoutParams paramsTV = (new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,1f));

        LinearLayout.LayoutParams paramLL = (new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,1f));

        newll.setOrientation(LinearLayout.HORIZONTAL);

        if(isIncludeCategory)
        {
            setTVLayoutParams(IDTV, widthDisplay * 12 / 100);
            setTVLayoutParams(catNameTV, widthDisplay * 20 / 100);
            setTVLayoutParams(shopTV, widthDisplay * 15 / 100);
            setTVLayoutParams(tranDateTV, widthDisplay * 19 / 100);
            setTVLayoutParams(paymentMethodTV, widthDisplay * 20 / 100);//replace 20 in 25
            setTVLayoutParams(tranPriceTV, widthDisplay * 14 / 100);
        }
        else
        {
            setTVLayoutParams(IDTV, widthDisplay * 16 / 100);
            setTVLayoutParams(shopTV, widthDisplay * 19 / 100);
            setTVLayoutParams(tranDateTV, widthDisplay * 23 / 100);
            setTVLayoutParams(paymentMethodTV, widthDisplay * 24 / 100);//replace 20 in 25
            setTVLayoutParams(tranPriceTV, widthDisplay * 18 / 100);
        }

        int wi = IDTV.getWidth();

        IDTV.setTextSize(11);
        catNameTV.setTextSize(11);
        paymentMethodTV.setTextSize(11);
        shopTV.setTextSize(11);
        tranDateTV.setTextSize(11);
        tranPriceTV.setTextSize(11);

        if(ID == "סך הכל")
        {
            IDTV.setTypeface(null, Typeface.BOLD);
            IDTV.setTextSize(11);
            IDTV.setTextColor(Color.BLACK);
            tranPriceTV.setTypeface(null, Typeface.BOLD);
            tranPriceTV.setTextSize(11);
            tranPriceTV.setTextColor(Color.BLACK);
        }
        if(isHeaderLine != true)
        {
            tranPriceTV.setTextDirection(View.TEXT_DIRECTION_LTR);
            tranPriceTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
         //   paymentMethodTV.setTextDirection(View.TEXT_DIRECTION_LTR);
         //   paymentMethodTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
//        paymentMethodTV.setTextDirection(View.TEXT_DIRECTION_LTR);
//        paymentMethodTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        //tranDateTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        //tranPriceTV.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);


        if(isHeaderLine == true)
        {
            setHeaderProperties(IDTV);
            setHeaderProperties(catNameTV);
            setHeaderProperties(paymentMethodTV);
            setHeaderProperties(shopTV);
            setHeaderProperties(tranDateTV);
            setHeaderProperties(tranPriceTV);
        }

        newll.setLayoutParams(paramsTV);
        newll.addView(IDTV);

        TextView []stornoRow = getStornoRow(categoryName, tran);
        Boolean isStornoRow = (stornoRow != null);
        if(isStornoRow == true)
        {
            global.strikeThroughText(IDTV);
            global.strikeThroughText(catNameTV);
            global.strikeThroughText(paymentMethodTV);
            global.strikeThroughText(shopTV);
            global.strikeThroughText(tranDateTV);
            global.strikeThroughText(tranPriceTV);

/*          global.strikeThroughText(stornoRow[0]);
            global.strikeThroughText(stornoRow[1]);
            global.strikeThroughText(stornoRow[2]);
            global.strikeThroughText(stornoRow[3]);
            global.strikeThroughText(stornoRow[4]);*/
        }

        if(isIncludeCategory == true)
            newll.addView(catNameTV);

        newll.addView(shopTV);
        newll.addView(tranDateTV);
        newll.addView(paymentMethodTV);
        newll.addView(tranPriceTV);
        ll.addView(newll);

        if(isHeaderLine == true)
            setOnClickTextViews();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setTransactionsInGui(String catName, String sortBy, char ascOrDesc)
    {
        Boolean isIncludeCategory = false;
        if (catName.equals("הכל"))
            isIncludeCategory = true;
        month.setTransactions(catName);
        if(sortBy != null)
            sort(sortBy,ascOrDesc);
        if(month.getTransactions() == null)
            return;
        double tranSum = 0;
        for (Transaction tran : month.getTransactions() )
        {
            long ID = tran.getID();
            String categoryName = tran.getCategory();
            String paymentMethod = tran.getPaymentMethod();
            String shop = tran.getShop();
            Date payDate = tran.getPayDate();
            double transactionPrice = tran.getPrice();
            tranSum += transactionPrice;
            transactionPrice = Math.round(transactionPrice * 100.d)/ 100.0d ;
            addCategoryRow(tran, String.valueOf(ID), categoryName, paymentMethod, shop, convertDateToString(payDate, dateFormat), String.valueOf(transactionPrice),isIncludeCategory, false);
        }
        if (month.getTransactions().size() > 0)
        {
            tranSum = Math.round(tranSum * 100.d) / 100.0d;
            addCategoryRow(null, "סך הכל", "", "", "", "", String.valueOf(tranSum), isIncludeCategory, false);
        }
    }
    public void setCloseButton()
    {
        final Button myButton = new Button(this);
        myButton.setText("סגור");
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
          setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//Rotate the screen to to be on PORTRAIT moade only
                finish();
            }
        });

        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout newll = new LinearLayout(TransactionsActivity.this);

        LinearLayout.LayoutParams paramLL = (new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,1f));

        newll.setOrientation(LinearLayout.HORIZONTAL);
        newll.addView(myButton,lp);
        ll.addView(newll);
    }

/*    public void setCloseButton()
    {
        final Button myButton = (Button) findViewById( R.id.categoryCloseButtonTransactions);
        myButton.setText("סגור");
        myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        //TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        //ll.addView(myButton, lp);
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        setTitle( getYearMonth(month.getMonth(),'.'));
        //setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//Rotate the screen to to be on landspace moade only
        textViews.clear();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthDisplay = dm.widthPixels;

        isFirstTime = !isFirstTime;

        ll = (LinearLayout)findViewById( R.id.LLTransactions);
        categoriesSpinner =  (Spinner) findViewById(R.id.categorySpinnerTransactions);


        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                String categoryName = categoriesSpinner.getSelectedItem().toString();
                //clearLayout();
                clearRowsExeptHaeder();
                setTextViewsHeader();
                //Include category
                if (categoryName != "הכל")
                {
                    LinearLayout headerRowLL = (LinearLayout) ll.getChildAt(0);
                    headerRowLL.getChildAt(1).setVisibility(View.GONE);
                    setTVLayoutParams((TextView)headerRowLL.getChildAt(0), widthDisplay * 16 / 100);
                    setTVLayoutParams((TextView)headerRowLL.getChildAt(2), widthDisplay * 19 / 100);
                    setTVLayoutParams((TextView)headerRowLL.getChildAt(3), widthDisplay * 23 / 100);
                    setTVLayoutParams((TextView)headerRowLL.getChildAt(4), widthDisplay * 24 / 100);//replace 20 in 25
                    setTVLayoutParams((TextView)headerRowLL.getChildAt(5), widthDisplay * 18 / 100);
                }
                   // addCategoryRow(null, "מזהה" ,"קטגוריה","חנות", "תאריך", "סכום",true, true);
                else
                    ((LinearLayout) ll.getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
                    //Not include category
                   // addCategoryRow(null, "מזהה" ,"categoryName","חנות", "תאריך", "סכום",false, true);
                setTransactionsInGui(categoryName,"מזהה", UP_ARROW);
                if(ll.getChildCount() == 1)//if no transactions and only header in layout
                {
                    TextView messageTV = new TextView(TransactionsActivity.this);
                    messageTV.setText("לא נמצאו עסקאות!");
                    messageTV.setTextSize(24);
                    messageTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    //messageTV.setLayoutParams(paramsTV);
                    ll.addView(messageTV);
                }
                //setCloseButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        init();
        //clearLayout();
        addCategoryRow(null, "מזהה","קטגוריה", "א.תשלום", "חנות", "ת.עסקה", "סכום",true, true);
        //textViews.clear();
        setTransactionsInGui("הכל", "מזהה", UP_ARROW);
        //setCloseButton();
    }

    public void clearLayout()
    {
        //ll = (LinearLayout)findViewById( R.id.LLTransactions);
        if (ll.getChildCount() > 0 )
        {
            ll.removeAllViews();
            ll.invalidate();
            //ll.addView(categoriesSpinner);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setHeaderProperties(TextView tv)
    {
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        tv.setTextSize(14);
        tv.setClickable(true);
        Linkify.addLinks(tv,Linkify.ALL);
    }

    public void setTextViewsHeader()
    {
        LinearLayout rowLL = (LinearLayout)ll.getChildAt(0);//textViews.get(0);
        ((TextView)rowLL.getChildAt(0)).setText("מזהה");
        ((TextView)rowLL.getChildAt(1)).setText("קטגוריה");
        ((TextView)rowLL.getChildAt(2)).setText("חנות");
        ((TextView)rowLL.getChildAt(3)).setText("ת.עסקה");
        ((TextView)rowLL.getChildAt(4)).setText("א.תשלום");
        ((TextView)rowLL.getChildAt(5)).setText("סכום");

        for(int i = 0; i < 6; i++)
            ((TextView)rowLL.getChildAt(i)).setTextColor(Color.BLACK);
    }

    public void setOnClickTextViews()
    {
        for (int i = 0; i < 6; i++)
        {
            final int j = i;
            LinearLayout rowLL = (LinearLayout)ll.getChildAt(0);
            ((TextView)rowLL.getChildAt(j)).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onClick(View view)
                {
                    LinearLayout rowLL2 = (LinearLayout)ll.getChildAt(0);
                    TextView headerTV = ((TextView)rowLL2.getChildAt(j));
                    String allText = headerTV.getText().toString();
                    char ascOrDesc = allText.charAt(allText.length() - 1);
                    String text = "";
                    if(ascOrDesc != UP_ARROW && ascOrDesc != DOWN_ARROW)
                    {
                        ascOrDesc = 'X';
                        text = allText;
                    }
                    else
                        text = new String(allText.substring(0,allText.length() -1));

                    setTextViewsHeader();
                    switch(ascOrDesc)
                    {
                        case ('ꜜ'):
                        {
                            //sort(text,upArrow);
                            ascOrDesc = UP_ARROW;
                            headerTV.setText(text + String.valueOf(UP_ARROW));
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        case ('ꜛ'):
                        {
                            //sort(text,downArrow);
                            ascOrDesc = DOWN_ARROW;
                            headerTV.setText(text + String.valueOf(DOWN_ARROW));
                            headerTV.setTextColor(Color.RED);
                            break;
                        }
                        default:
                        {
                            //sort(text,upArrow);
                            headerTV.setText(text + String.valueOf(UP_ARROW));
                            headerTV.setTextColor(Color.RED);
                            ascOrDesc = UP_ARROW;
                            break;
                        }
                    }
                    clearRowsExeptHaeder();
                    setTransactionsInGui(categoriesSpinner.getSelectedItem().toString(), text, ascOrDesc);
                    setCloseButton();
                }
            });
        }
    }

    public void clearRowsExeptHaeder()
    {
        for (int i = ll.getChildCount() - 1; i > 0; i--)
        {
            ll.removeViewAt(i);
/*            Log.i("TAG IS", i + "");
            LinearLayout rowLL = (LinearLayout) ll.getChildAt(i);
            rowLL.removeAllViews();*/
        }
    }
}
