package com.abarska.currencyexchanger;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

/**
 * Created by Dell I5 on 16.04.2017.
 */

public class CurrencyLoader extends AsyncTaskLoader<CurrencyAdapter> {

    private String[] mUrls;

    public CurrencyLoader(Context context, String[] urls) {
        super(context);
        mUrls = urls;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public CurrencyAdapter loadInBackground() {
        if (mUrls.length != 2 || mUrls[0] == null || mUrls[1] == null) return null;

        ArrayList<CurrencyItem> currentList = QueryUtils.extractData(mUrls[0]);
        if (currentList == null || currentList.isEmpty()) return null;

        ArrayList<CurrencyItem> previousList = QueryUtils.extractData(mUrls[1]);
        if (previousList == null || previousList.isEmpty()) return null;

        return new CurrencyAdapter(getContext(), currentList, previousList);
    }
}
