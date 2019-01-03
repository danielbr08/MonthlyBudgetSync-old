package com.example.brosh.mba;

import java.util.Date;

/**
 * Created by daniel.brosh on 12/26/2017.
 */

public class Budget {
    public String getCategory() {
        return category;
    }

    public String getCategorySon() {
        return categorySon;
    }

    public int getValue() {
        return value;
    }

    private String category;
    private String categorySon;
    private int value;
    private boolean isConstPayment;
    private String shop;
    private int chargeDay;

    public Budget(String category, int value, boolean isConstPayment, String shop, int chargeDay) {
        this.category = category;
        this.value = value;
        this.isConstPayment = isConstPayment;
        this.shop = shop;
        this.chargeDay = chargeDay;

        if(categorySon == null || categorySon.equals(""))
            this.categorySon = "ללא";
    }

    public Budget(String category, String categorySon, int value, boolean isConstPayment, String shop, int chargeDay) {
        this.category = category;
        this.categorySon = categorySon;
        this.value = value;
        this.isConstPayment = isConstPayment;
        this.shop = shop;
        this.chargeDay = chargeDay;

        if(categorySon == null || categorySon.equals(""))
            this.categorySon = "ללא";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategorySon(String categorySon) {
        this.categorySon = categorySon;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isConstPayment() {
        return isConstPayment;
    }

    public void setConstPayment(boolean constPayment) {
        isConstPayment = constPayment;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public int getChargeDay() {
        return chargeDay;
    }

    public void setChargeDay(Date paymentDate) {
        this.chargeDay = chargeDay;
    }
}
