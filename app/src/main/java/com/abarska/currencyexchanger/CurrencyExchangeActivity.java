package com.abarska.currencyexchanger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.abarska.currencyexchanger.QueryUtils.getCurrentResponse;

public class CurrencyExchangeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private CurrencyAdapter mAdapter;
    private ListView mLvCurrencies;
    private TextView tvEmptyView;
    private ProgressBar pbLoading;
    private SimpleDateFormat dateFormat;

    public final static String BASE_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange";
    private final static int CURRENCY_LOADER_ID = 0;

    // string keys for shared preferences
    private SharedPreferences sharedPref;
    private final static String KEY_DATE = "current_date";
    private final static String KEY_JSON_CURRENT = "current_json_response";
    private final static String KEY_JSON_PREVIOUS = "previous_json_response";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_exchange);

        tvEmptyView = (TextView) findViewById(R.id.tvEmptyView);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        mLvCurrencies = (ListView) findViewById(R.id.lvCurrencies);

        mAdapter = new CurrencyAdapter(this, new ArrayList<CurrencyItem>(), new ArrayList<CurrencyItem>());
        mLvCurrencies.setAdapter(mAdapter);
        mLvCurrencies.setEmptyView(tvEmptyView);

        mLvCurrencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getBaseContext(), mAdapter.getItem(position).getDescription(), Toast.LENGTH_SHORT).show();
            }
        });

        dateFormat = new SimpleDateFormat("yyyyMMdd");

        sharedPref = getPreferences(MODE_PRIVATE);

        if (dateFormat.format(new Date()).equals(sharedPref.getString(KEY_DATE, ""))) {
            // if today's rates have been uploaded already, UI will be filled with saved data
            restoreSavedData(sharedPref.getString(KEY_JSON_CURRENT, ""), sharedPref.getString(KEY_JSON_PREVIOUS, ""));
        } else {
            // if not, need to make an HTTP request
            fetchDataFromInternet();
        }
    }

    private void restoreSavedData(String currentJson, String previousJson) {
        if (TextUtils.isEmpty(currentJson) || TextUtils.isEmpty(previousJson)) return;

        ArrayList<CurrencyItem> currentList = QueryUtils.extractFeatureFromJson(currentJson);
        if (currentList == null || currentList.isEmpty()) return;

        ArrayList<CurrencyItem> previousList = QueryUtils.extractFeatureFromJson(previousJson);
        if (previousList == null || previousList.isEmpty()) return;

        mAdapter = new CurrencyAdapter(this, currentList, previousList);
        mLvCurrencies.setAdapter(mAdapter);
        pbLoading.setVisibility(View.GONE);
    }

    private void fetchDataFromInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getSupportLoaderManager().initLoader(CURRENCY_LOADER_ID, null, this);
        } else {
            pbLoading.setVisibility(View.GONE);
            tvEmptyView.setText(getString(R.string.no_internet));
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        Date current = new Date();
        String formattedUrlCurrent = QueryUtils.buildUrl(dateFormat.format(current));

        Date previous = QueryUtils.setPreviousDate();
        String formattedUrlPrevious = QueryUtils.buildUrl(dateFormat.format(previous));

        return new CurrencyLoader(this, new String[]{formattedUrlCurrent, formattedUrlPrevious});
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        if (data == null) return;

        pbLoading.setVisibility(View.GONE);
        tvEmptyView.setText(getString(R.string.no_data));

        mAdapter = (CurrencyAdapter) data;
        mLvCurrencies.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mLvCurrencies.setAdapter(new CurrencyAdapter(this, new ArrayList<CurrencyItem>(), new ArrayList<CurrencyItem>()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_DATE, QueryUtils.getToday());
        editor.putString(KEY_JSON_CURRENT, getCurrentResponse());
        editor.putString(KEY_JSON_PREVIOUS, QueryUtils.getPreviousResponse());
        editor.commit();
    }
}
