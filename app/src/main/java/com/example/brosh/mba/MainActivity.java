/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.example.brosh.mba;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.brosh.mba.R.id.monthSpinner;
import static com.example.brosh.mba.global.DB_FILE_NAME;
import static com.example.brosh.mba.global.DB_FOLDER_PATH;
import static com.example.brosh.mba.global.DB_SUFFIX;
import static com.example.brosh.mba.global.DCIM;
import static com.example.brosh.mba.global.FILE_NAME;
import static com.example.brosh.mba.global.IS_MONTH_CHANGABLE;
import static com.example.brosh.mba.global.PROJECT_PATH;
import static com.example.brosh.mba.global.TRAN_ID_PER_MONTH_NUMERATOR;
import static com.example.brosh.mba.global.categoryUpdateSync;
import static com.example.brosh.mba.global.convertStringToDate;
import static com.example.brosh.mba.global.copyFile;
import static com.example.brosh.mba.global.dateFormat;
import static com.example.brosh.mba.global.deleteFiles;
import static com.example.brosh.mba.global.getDateStartMonth;
import static com.example.brosh.mba.global.getSartOfMonth;
import static com.example.brosh.mba.global.getTodayDate;
import static com.example.brosh.mba.global.getYearMonth;
import static com.example.brosh.mba.global.isDBFileDownloaded;
import static com.example.brosh.mba.global.lastUpdatedTimeMillis;
import static com.example.brosh.mba.global.myLastUpdatedTimeMillis;
import static com.example.brosh.mba.global.reasonCheckFileChanged;
import static com.example.brosh.mba.global.reverseDateString;
import static com.example.brosh.mba.global.shopsSet;
import static com.example.brosh.mba.global.transactionAddSync;

//import android.text.format.DateFormat;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity
{
    Intent budgetScreen, transactionsScreen, insertTransactionScreen, createBudgetScreen;
    Spinner refMonthSpinner;
    Button insertTransactionButton;
    Button budgetButton;
    Button transactionsButton;
    Button createBudgetButton;
    Button closeMainButton;

    static String tempFilePath = "";
    Uri pdfUri;// uri actually URLs that are meant for local storage

    FirebaseStorage storage;// used for uploadinffiles// EX: pdf
    FirebaseDatabase database;// used to store URLs of uploaded files..
    ProgressDialog progressDialog;

    public static myDBAdapter monthlyBudgetDB;

    public static Month month;

    /**
     * Check if there is any connectivity
     *
     * @return is Device Connected
     */
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        }
        return false;
    }

    public void backupDB()
    {
        String sourceBackupFilePath = DB_FOLDER_PATH + "/"+DB_FILE_NAME+"."+DB_SUFFIX;
        String destBackupPath = PROJECT_PATH;
        String sourceFilePath = PROJECT_PATH+"/"+DB_FILE_NAME+"_FB"+"."+DB_SUFFIX;
        String destPath = DB_FOLDER_PATH;
        //monthlyBudgetDB.myhelper.close();
        boolean cloudFileExists = isFileExists(sourceFilePath);

        if(cloudFileExists)
        {
            copyFile(sourceBackupFilePath,destBackupPath,DB_FILE_NAME+"_BAC",DB_SUFFIX);//backup file
            copyFile(sourceFilePath, destPath, DB_FILE_NAME, DB_SUFFIX);//real DB file from cloud
        }
    }

    public void getTimeLastUpdateFile(final String reason, final boolean isForUpdateData)
    {
        StorageReference fileRef = storage.getReference().child("MB").child(DB_FILE_NAME);
        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                if(reason.equals("Write"))
                {
                    int a = 0;
                }
                lastUpdatedTimeMillis = storageMetadata.getUpdatedTimeMillis();
                //if(isForUpdateData)
                //    myLastUpdatedTimeMillis = lastUpdatedTimeMillis;
                if(reasonCheckFileChanged.equals("Write") && myLastUpdatedTimeMillis == lastUpdatedTimeMillis)
                {
                    myLastUpdatedTimeMillis = lastUpdatedTimeMillis;
                    int maxIDPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth());
                    month.updateMonthData(maxIDPerMonth + 1);
                    uploadCommonFile();
                }
                else
                    downloadCommonFile(reason);
                reasonCheckFileChanged = "Show";
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Toast.makeText(MainActivity.this, exception.getMessage().toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public boolean isDataFileChanged()
    {
        return (myLastUpdatedTimeMillis != lastUpdatedTimeMillis &&
                myLastUpdatedTimeMillis != 0);
    }

    // Download DB file from firebase and save it in project path
    // save local DB file as BAC DB file in project path
    // replacing real DB file by copy downloaded DB file from project path
    @RequiresApi(api = Build.VERSION_CODES.O)
public void downloadCommonFile(final String reason)
{
    StorageReference storageReference = storage.getReference();// returns root path
    StorageReference fileRef = storageReference.child("MB").child(DB_FILE_NAME);

    boolean isDownloadNeeded = false;
    boolean isFileChanged = isDataFileChanged();

    // File exists - on open app or on other client update the file
    if((lastUpdatedTimeMillis > 0 && myLastUpdatedTimeMillis == 0) || isFileChanged)
    {
        int state = 0;
        if(myLastUpdatedTimeMillis == 0)
            state = 1;
        if(isFileChanged)
            state = 2;
        isDownloadNeeded = true;
    }
    if(!isDownloadNeeded)
    {
        budgetButton.setEnabled(true);
        transactionsButton.setEnabled(true);
        insertTransactionButton.setEnabled(IS_MONTH_CHANGABLE);
        // Disabled buttons except creation budget if there is no budget
        if(!monthlyBudgetDB.checkBudgetExists() )
        {
            budgetButton.setEnabled(false);
            transactionsButton.setEnabled(false);
            insertTransactionButton.setEnabled(false);
        }

        if(reason.equals("budget"))
            startActivity(budgetScreen);
        else if(reason.equals("insertTransaction"))
            startActivity(insertTransactionScreen);
        else if(reason.equals("transactions"))
            startActivity(transactionsScreen);
        else if(reason.equals("createBudget"))
            startActivity(createBudgetScreen);
        return;
    }

    // File not exists - before creating budget
    if (myLastUpdatedTimeMillis == 0 && lastUpdatedTimeMillis == 0)
    {
        budgetButton.setEnabled(false);
        transactionsButton.setEnabled(false);
        insertTransactionButton.setEnabled(false);
        month = null;
        return;
    }

    if(!isDownloadNeeded)
        return;

    File destinationFile = new File(Environment.DIRECTORY_DOWNLOADS + "/MB/MonthlyBudgetFU.db");
    isDBFileDownloaded = false;
    File localFile = null;
    try {
        localFile = File.createTempFile("MonthlyBudget_FB", "db");//,destinationFile);
        tempFilePath = localFile.getCanonicalPath();
    } catch (IOException e)
    {
        String a = e.getMessage().toString();
        e.printStackTrace();
    }
    boolean isConnected = isConnected();
/*        if(!isConnected) {
            Toast.makeText(MainActivity.this, "נדרש חיבור לאינטרנט!", Toast.LENGTH_LONG).show();
            while (!isConnected());
        }*/
    localFile.deleteOnExit();

    fileRef.getFile(localFile)
            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                {
                    try {
                        // Successfully downloaded data to local file
                        // ...
                        isDBFileDownloaded = true;
                        copyFile(tempFilePath,PROJECT_PATH,DB_FILE_NAME+"_FB", DB_SUFFIX);
                        deleteFiles(tempFilePath);
                        Toast.makeText(MainActivity.this, "טעינת נתונים הסתיימה בהצלחה!", Toast.LENGTH_SHORT).show();
                        backupDB();
                        reloadData();
                        month.setTransChanged(false);
                        myLastUpdatedTimeMillis = lastUpdatedTimeMillis;

                        if(transactionAddSync != null) {
                            categoryUpdateSync.addTransaction(transactionAddSync);
                            month.getCategories().set(month.getCategories().indexOf(categoryUpdateSync),categoryUpdateSync);

                            month.setTransChanged(true);
                            shopsSet.add(transactionAddSync.getShop());
                            month.getTransactions().add(transactionAddSync);
                            reasonCheckFileChanged = "Write";
                            transactionAddSync = null;
                            categoryUpdateSync = null;
                        }

                        budgetButton.setEnabled(true);
                        transactionsButton.setEnabled(true);
                        insertTransactionButton.setEnabled(IS_MONTH_CHANGABLE);
                        // Disabled buttons except creation budget if there is no budget
                        if(!monthlyBudgetDB.checkBudgetExists() )
                        {
                            budgetButton.setEnabled(false);
                            transactionsButton.setEnabled(false);
                            insertTransactionButton.setEnabled(false);
                        }

                        if(reason.equals("budget"))
                            startActivity(budgetScreen);
                        else if(reason.equals("insertTransaction"))
                            startActivity(insertTransactionScreen);
                        else if(reason.equals("transactions"))
                            startActivity(transactionsScreen);
                        else if(reason.equals("createBudget"))
                            startActivity(createBudgetScreen);


                    }catch(Exception e)
                    {
                        Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {

            // Handle failed download
            int errorCode = ((StorageException) exception).getErrorCode();

            // File not exists
            if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
            {
                budgetButton.setEnabled(false);
                transactionsButton.setEnabled(false);
                insertTransactionButton.setEnabled(false);
                month = null;
            }
        }
    });

    //downloadDBFile(reason);// Download to PROJECT_PATH+"/"+DB_FILE_NAME+"_FB"+"."+DB_SUFFIX

    /*    ProcessBuilder iotat = new ProcessBuilder().command("");
    try {
        iotat.wait(3000);
    }
    catch(Exception e){}*/
}

    // Upload DB file to firebase
    // copy real DB file to project path and upload that file to firebase
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void uploadCommonFile()
    {
        //String sourceBackupFilePath = DB_FOLDER_PATH + "/"+DB_FILE_NAME+"."+DB_SUFFIX;
        //String destBackupFilePath = PROJECT_PATH;
        String sourceFilePath = DB_FOLDER_PATH + "/"+DB_FILE_NAME+"."+DB_SUFFIX;
        String destFilePath = PROJECT_PATH;


        //monthlyBudgetDB.myhelper.close();
        boolean isFileExists = isFileExists(sourceFilePath);

        if(isFileExists)
        {
            copyFile(sourceFilePath, destFilePath, DB_FILE_NAME, DB_SUFFIX);//real DB file from cloud
            sourceFilePath = destFilePath + "/" + DB_FILE_NAME + "." + DB_SUFFIX;
            uploadDBFile("file:///" + sourceFilePath,"MB",DB_FILE_NAME);
        }
    }

    public boolean isFileExists(String filePath)
    {
        return new File(filePath).exists();
    }

    public void downloadDBFile()
    {
    }

    private void uploadFile(Uri pdfUri) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName = "MonthlyBudget";//System.currentTimeMillis()+"_MonthlyBudget.db";
        StorageReference storageReference = storage.getReference();// returns root path

        storageReference.child("MB/" + fileName).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String url = downloadUrl.toString();// return the url of your uploaded file..
                // store the urrrl in realtime database..
                DatabaseReference reference = database.getReference();// return the path to root

                reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "שמירת נתונים הסתיימה בהצלחה!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                        else
                            Toast.makeText(MainActivity.this,"File not successfully uploaded*",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this,"File not successfully uploaded**",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                // track the progress of = our upload..
                @SuppressWarnings("VisibleForTests") int currentProgress = (int)(100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    public void uploadDBFile(String from, String toFB,final String fileName)
    {
        monthlyBudgetDB.myhelper.close();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("מעלה קובץ...");
        progressDialog.setProgress(0);
        progressDialog.show();
        StorageReference storageReference = storage.getReference();// returns root path

        pdfUri = Uri.parse(from);
        storageReference.child(toFB + "/" + fileName).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //progressDialog.dismiss();

                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                String url = downloadUrl.toString();// return the url of your uploaded file..
                // store the urrrl in realtime database..
                DatabaseReference reference = database.getReference();// return the path to root

                reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "קובץ הועלה בהצלחה!", Toast.LENGTH_SHORT).show();
                            getTimeLastUpdateFile("Show",false);
                        }
                        else
                            Toast.makeText(MainActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //progressDialog.dismiss();
                Toast.makeText(MainActivity.this,e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this,"תקלה בהעלאת קובץ!",Toast.LENGTH_SHORT).show();
                }
        });
    }

    public void makeBackupDBFile()
    {}

    public void initRefMonthSpinner()
    {

        //global.setCatArrayHebNames();
        ArrayList<String> allMonths = monthlyBudgetDB.getAllMonthesYearMonth("DESC");//getAllMonthes();

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                R.layout.custom_spinner,allMonths);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refMonthSpinner.setAdapter(adapter);

/*        String refMonth = refMonthSpinner.getSelectedItem().toString();
        refMonth = ("01." + refMonth);
        refMonth = refMonth.replace('.','/');
        month = new Month(convertStringToDate(refMonth,dateFormat));
        refMonth = refMonth.replace('/','.').substring(0,refMonth.length() -3);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCloseApp()
    {
        if(month != null &&  month.getTransChanged())
        {
            month.updateMonthData(0);
            uploadCommonFile();
            month.setTransChanged(false);
        }
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume()
    {
        super.onResume();

        if(monthlyBudgetDB == null)
            return;
        if(reasonCheckFileChanged.equals("Write"))
            getTimeLastUpdateFile("", true);
        // After inserted transactions
        else if(month != null && month.getTransChanged() == true)
        {
            int maxIDPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth());
            month.updateMonthData(maxIDPerMonth + 1);
            uploadCommonFile();
        }

        // After creating a new Budget
        if(monthlyBudgetDB.checkBudgetExists() && month == null)
        {
            budgetButton.setEnabled(true);
            transactionsButton.setEnabled(true);
            insertTransactionButton.setEnabled(true);

            monthlyBudgetDB.deleteDataRefMonth(getDateStartMonth());
            month = new Month(getDateStartMonth());
            IS_MONTH_CHANGABLE = true;
            TRAN_ID_PER_MONTH_NUMERATOR = 1;
            setTitle(getYearMonth(month.getMonth(), '.'));
            initRefMonthSpinner();

            if(month.getTransactions().size() == 0 && isDBFileDownloaded)
                //Write frequence transactions
                setFrqTrans();
            month.setTransChanged(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setFrqTrans()
    {
        int maxBudgetNumberBGT = monthlyBudgetDB.getMaxBudgetNumberBGT();
        ArrayList<Budget> allBudget = monthlyBudgetDB.getBudgetDataFromDB(maxBudgetNumberBGT);
        for (Budget budget:allBudget)
        {
            if(!budget.isConstPayment())
                continue;

            String categoryName = budget.getCategory();
            double transactionPrice = Double.valueOf(budget.getValue());
            String shop = budget.getShop();
            int chargeDay = budget.getChargeDay();
            Date payDate = new Date();
            String paymentMethod = "כרטיס אשראי";

            Calendar c = Calendar.getInstance();
// set the calendar to start of today
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            int lastDayInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            if(chargeDay > lastDayInMonth)
                chargeDay = lastDayInMonth;
            c.set(Calendar.DAY_OF_MONTH, chargeDay);
            payDate = c.getTime();

            //Insert data
            Transaction transaction = new Transaction(TRAN_ID_PER_MONTH_NUMERATOR++, categoryName,paymentMethod , shop, payDate, transactionPrice, new Date());
            transaction.setIsStorno(false);
            transaction.setStornoOf(-1);
            month.setTransChanged(true);

            for (Category cat : month.getCategories())
            {
                if (categoryName.equals(cat.getName()))
                {
                    cat.subValRemaining(transactionPrice);
                    cat.addTransaction(transaction);
                }
            }
            shopsSet.add(shop);
        }
        month.setAllTransactions();
        if( month.getTransChanged()) {
            month.updateMonthData(0);
            uploadCommonFile();
        }
        month.setTransChanged(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStop()
    {
        super.onStop();
        if(month != null && month.getTransChanged() == true)
        {
            int maxIDPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth());
            month.updateMonthData(maxIDPerMonth + 1);
            //uploadCommonFile();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        onCloseApp();
        //copyFile(DB_FOLDER_PATH + "/" + DB_FILE_NAME + "." + DB_SUFFIX, PROJECT_PATH, DB_FILE_NAME, DB_SUFFIX);
        finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        //write catArray to the file
    }

/*    public static boolean isSDCARDAvailable(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }*/

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

    public void reloadData()
    {
        monthlyBudgetDB = new myDBAdapter(this);
        boolean isSetFrqTransNeeded = false;
        if(!monthlyBudgetDB.checkCurrentRefMonthExists())
            isSetFrqTransNeeded = true;
        Date todayDate = getDateStartMonth();
        month = new Month(todayDate);
        IS_MONTH_CHANGABLE = true;
        //TRAN_ID_PER_MONTH_NUMERATOR = monthlyBudgetDB.getMaxIDPerMonthTRN(todayDate) + 1;
        setTitle(getYearMonth(month.getMonth(), '.'));
        initRefMonthSpinner();
        if(isSetFrqTransNeeded)
            //Write frequence transactions
            setFrqTrans();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        budgetScreen = null;
        transactionsScreen = null;
        insertTransactionScreen = null;
        createBudgetScreen = null;
        refMonthSpinner = (Spinner) findViewById(monthSpinner);
        //DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        DCIM = getExternalFilesDir(Environment.DIRECTORY_DCIM).toString();
        PROJECT_PATH = DCIM + "/" + FILE_NAME;

        global.setCatPaymentMethodArray();

        insertTransactionButton = (Button) findViewById(R.id.insertTransactionButton);
        budgetButton = (Button) findViewById(R.id.budgetButton);
        transactionsButton = (Button) findViewById(R.id.transactionsButton);
        createBudgetButton = (Button) findViewById(R.id.createBudgetButton);
        closeMainButton = (Button) findViewById(R.id.closeMainButton);
        global.setCatPaymentMethodArray();

        storage = FirebaseStorage.getInstance();// return an object of firebase storage
        database = FirebaseDatabase.getInstance();// return an object of firebase database

        //monthlyBudgetDB = new myDBAdapter(this);

        Toast.makeText(MainActivity.this, "טוען נתונים...", Toast.LENGTH_SHORT).show();

        getTimeLastUpdateFile("",false);

        //downloadCommonFile("");
        //uploadFile(Uri.parse("file:///storage/emulated/0/Download/MonthlyBudget.db"));
        //uploadDBFile(DB_FOLDER_PATH + "/" + DB_FILE_NAME + "." +  DB_SUFFIX,"MB",DB_FILE_NAME);
        //prepareUpload();
        //uploadDBFile("file:///storage/emulated/0/Android/data/com.example.brosh.mba/files/DCIM/Monthly Budget/MonthlyBudget.db","MB",DB_FILE_NAME);
        //uploadDBFile("file:///storage/emulated/0/Download/MonthlyBudget.db","MB",DB_FILE_NAME);

        //downloadDBFile();

        //month = new Month(getTodayDate());
        //IS_MONTH_CHANGABLE = true;
        //setTitle(month.getYearMonth(month.getMonth(), '.'));
        //initRefMonthSpinner();
        //DCIM = getFilesDir().getAbsolutePath();

        //Listening to button event
        budgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (budgetScreen == null)
                    budgetScreen = new Intent(getApplicationContext(), BudgetActivity.class);

                //Sending data to another Activity by key[name] and value[something]
                //nextScreen.putExtra("name", "something");
                //nextScreen.putExtra("email", "something2");
                budgetButton.setEnabled(false);
                transactionsButton.setEnabled(false);
                insertTransactionButton.setEnabled(false);
                getTimeLastUpdateFile("budget",false);
                //downloadCommonFile("budget");
                //startActivity(budgetScreen);
            }
        });

        //Listening to button event
        insertTransactionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (insertTransactionScreen == null)
                    insertTransactionScreen = new Intent(getApplicationContext(), InsertTransactionActivity.class);

                //Sending data to another Activity by key[name] and value[something]
                //nextScreen.putExtra("name", "something");
                //nextScreen.putExtra("email", "something2");
                budgetButton.setEnabled(false);
                transactionsButton.setEnabled(false);
                insertTransactionButton.setEnabled(false);
                getTimeLastUpdateFile("insertTransaction",false);
                //downloadCommonFile("insertTransaction");
                //startActivity(insertTransactionScreen);

            }
        });

        //Listening to button event
        transactionsButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (transactionsScreen == null)
                    transactionsScreen = new Intent(getApplicationContext(), TransactionsActivity.class);

                //Sending data to another Activity by key[name] and value[something]
                //nextScreen.putExtra("name", "something");
                //nextScreen.putExtra("email", "something2");

                budgetButton.setEnabled(false);
                transactionsButton.setEnabled(false);
                insertTransactionButton.setEnabled(false);
                getTimeLastUpdateFile("transactions",false);
                //downloadCommonFile("transactions");

                //startActivity(transactionsScreen);
            }
        });

        //Listening to button event
        createBudgetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Starting a new Intent
                if (createBudgetScreen == null)
                    createBudgetScreen = new Intent(getApplicationContext(), Create_Budget_Activity.class);
                    //initRefMonthSpinner();

                    //Sending data to another Activity by key[name] and value[something]
                    //nextScreen.putExtra("name", "something");
                    //nextScreen.putExtra("email", "something2");
                budgetButton.setEnabled(false);
                transactionsButton.setEnabled(false);
                insertTransactionButton.setEnabled(false);
                getTimeLastUpdateFile("createBudget",false);
                //downloadCommonFile("createBudget");
                //startActivity(createBudgetScreen);
            }
        });

        //Listening to button event
        closeMainButton.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.O)
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View arg0) {
                onCloseApp();
                // finish();
                // System.exit(0);
                // finish();
            }
        });

        refMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String refMonth = refMonthSpinner.getSelectedItem().toString();
                refMonth = (refMonth + ".01");
                refMonth = reverseDateString(refMonth,"\\.");
                refMonth = refMonth.replace('.', '/');
                // Close the current month by writing to files+
                if(month.getTransChanged())
                {
                    int maxIDPerMonth = monthlyBudgetDB.getMaxIDPerMonthTRN(month.getMonth());
                    month.updateMonthData(maxIDPerMonth + 1);
                }

                month = new Month(convertStringToDate(refMonth, dateFormat));
                setTitle(getYearMonth(month.getMonth(), '.'));
                Date d = getSartOfMonth(getTodayDate());
                if(month.getMonth().before(getSartOfMonth(getTodayDate())))
                    IS_MONTH_CHANGABLE = false;

                else
                    IS_MONTH_CHANGABLE = true;
                insertTransactionButton.setEnabled(IS_MONTH_CHANGABLE);
                createBudgetButton.setEnabled(IS_MONTH_CHANGABLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }


        });
    }
}
