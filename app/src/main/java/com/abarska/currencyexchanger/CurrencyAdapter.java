package com.abarska.currencyexchanger;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CurrencyAdapter extends BaseAdapter {

    private ArrayList<CurrencyItem> mCurrentList;
    private ArrayList<CurrencyItem> mPreviousList;
    private Context mContext;

    public CurrencyAdapter(@NonNull Context context, ArrayList<CurrencyItem> currentList, ArrayList<CurrencyItem> previousList) {
        mCurrentList = currentList;
        mPreviousList = previousList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCurrentList.size();
    }

    @Override
    public CurrencyItem getItem(int i) {
        return mCurrentList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.currency_item, parent, false);
        }
        CurrencyItem currentItem = getItem(position);

        TextView tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
        tvDescription.setText(currentItem.displayInfo());

        ImageView ivDynamics = (ImageView) convertView.findViewById(R.id.ivDynamics);

        double currRate = Double.valueOf(mCurrentList.get(position).getRate());
        double prevRate = Double.valueOf(mPreviousList.get(position).getRate());

        if (currRate > prevRate) {
            ivDynamics.setImageResource(R.drawable.ic_trending_up_black_36dp);
            ivDynamics.setBackgroundColor(Color.RED);
        } else if (currRate < prevRate) {
            ivDynamics.setImageResource(R.drawable.ic_trending_down_black_36dp);
            ivDynamics.setBackgroundColor(Color.GREEN);
        } else {
            ivDynamics.setImageResource(R.drawable.ic_trending_flat_black_36dp);
            ivDynamics.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }
}
