package com.abarska.currencyexchanger;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by Dell I5 on 10.04.2017.
 */

public class CurrencyItem {

    private String mRate;
    private String mCode;
    private String mDescription;

    public CurrencyItem(double rate, String code, String description) {

        DecimalFormat formatter = new DecimalFormat("0.00");
        formatter.setMinimumFractionDigits(2);
        formatter.setRoundingMode(RoundingMode.HALF_EVEN);

        mRate = formatter.format(rate);

        mCode = code;

        mDescription = description;
    }

    public String displayInfo() {
        return "1 " + mCode + " = " + mRate + " UAH";
    }

    public String getRate() {
        return mRate;
    }

    public String getDescription() {
        return mDescription;
    }
}
