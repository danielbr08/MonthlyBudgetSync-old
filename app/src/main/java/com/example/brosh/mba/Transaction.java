package com.example.brosh.mba;
import java.util.Date;

/**
 * Created by daniel.brosh on 7/16/2017.
 */

public class Transaction
{
    private int ID;//Per month
    private String category;
    private String subCategory;
    private String paymentMethod;
    private String shop;
    private Date payDate;
    private double price;
    private Date registrationDate;
    private boolean isStorno;
    private int stornoOf;

    public Transaction( int ID, String category, String paymentMethod, String shop, Date payDate, double price, Date registrationDate)
    {
        this.ID = ID;
        this.category = category;
        this.subCategory = "ללא";
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.registrationDate = registrationDate;
    }



    public Transaction( int ID, String category, String paymentMethod, String shop, Date payDate, double price, Date registrationDate, boolean isStorno, int stornoOf)
    {
        this.ID = ID;
        this.category = category;
        this.subCategory = "ללא";
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.isStorno = isStorno;
        this.stornoOf = stornoOf;
        this.registrationDate = registrationDate;
    }

    public Transaction( int ID, String category, String subCategory, String paymentMethod, String shop, Date payDate, double price, Date registrationDate, boolean isStorno, int stornoOf)
    {
        this.ID = ID;
        this.category = category;
        this.subCategory = subCategory;
        this.paymentMethod = paymentMethod;
        this.shop = shop;
        this.payDate = payDate;
        this.price = price;
        this.isStorno = isStorno;
        this.stornoOf = stornoOf;
        this.registrationDate = registrationDate;
    }

    public boolean isStorno(Transaction tran)
    {
        return ( (this.getID() != tran.getID()) &&
                this.getCategory().equals(tran.getCategory()) &&
                (this.getPayDate().compareTo(tran.getPayDate()) == 0) &&
                this.getPrice() == -tran.getPrice() &&
                this.getShop().equals(tran.getShop()) &&

                ( (this.getIsStorno() == false)));
//                        ||
 //                  (this.getIsStorno() == true) && this.getStornoOf() == tran.getStornoOf()));
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }

    public int getID()
    {
        return ID;
    }

    public void setPayDate(Date payDate)
    {
        this.payDate = payDate;
    }

    public Date getPayDate()
    {
        return payDate;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public double getPrice()
    {
        return price;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getCategory()
    {
        return category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public void setShop(String shop)
    {
        this.shop = shop;
    }

    public String getShop()
    {
        return shop;
    }


    public void setIsStorno(boolean isStorno)
    {
        this.isStorno = isStorno;
    }

    public boolean getIsStorno()
    {
        return isStorno;
    }

    public void setStornoOf(int stornoOf)
    {
        this.stornoOf = stornoOf;
    }

    public int getStornoOf()
    {
        return stornoOf;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public boolean isStorno() {
        return isStorno;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setStorno(boolean storno) {
        isStorno = storno;
    }

}
