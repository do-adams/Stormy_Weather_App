package com.naesala.stormy.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.naesala.stormy.R;
import com.naesala.stormy.adapters.DayAdapter;
import com.naesala.stormy.weather.Day;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DailyForecastActivity extends ListActivity {
    //This class is binded to the ListView and an Empty TextView through XML
    private Day[] mDays;
    private String mCityName;

    @Bind(R.id.locationLabel) TextView mLocationDayLabel;
    //XML Activity hierarchy retrieves the correct "locationLabel" id
    //We can have views that have the same ids as long as they are on different activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

        mCityName = intent.getStringExtra(getResources().getString(R.string.city_key));
        mLocationDayLabel.setText(mCityName);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String conditions = mDays[position].getSummary();
        String highTemp = String.valueOf(mDays[position].getTemperatureMax());
        String message = String.format("On %s the high will be %s and it will be %s",
                dayOfTheWeek, highTemp, conditions);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
