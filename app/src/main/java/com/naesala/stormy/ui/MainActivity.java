package com.naesala.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.naesala.stormy.BuildConfig;
import com.naesala.stormy.R;
import com.naesala.stormy.weather.Current;
import com.naesala.stormy.weather.Day;
import com.naesala.stormy.weather.Forecast;
import com.naesala.stormy.weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Locator.LocatorCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private Forecast mForecast;
    private Locator mLocator;
    private double mLatitude;
    private double mLongitude;
    private String mCityName;

    //Default level of visibility of member variables is protected (which works for ButterKnife)
    @Bind(R.id.timeLabel)
    TextView mTimeLabel;
    @Bind(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @Bind(R.id.humidityValue)
    TextView mHumidityValue;
    @Bind(R.id.precipValue)
    TextView mPrecipValue;
    @Bind(R.id.summaryLabel)
    TextView mSummaryLabel;
    @Bind(R.id.iconImageView)
    ImageView mIconImageView;
    @Bind(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    @Bind(R.id.locationLabel)
    TextView mLocationLabel; //Label for location string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    getForecast(mLatitude, mLongitude);
                    getAddress();
                } else {
                    Toast.makeText(MainActivity.this, R.string.network_unavailable_message,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        Log.d(TAG, "Main UI Code is running");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocator = new Locator(this, this);
        mLocator.connect();
    }

    @Override
    protected void onPause() {
        mLocator.disconnect();
        super.onPause();
    }

    //Grabs the latitude and longitude from the Locator class
    @Override
    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        if (isNetworkAvailable()) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            getForecast(mLatitude, mLongitude);
            getAddress();

        } else {
            Toast.makeText(MainActivity.this, R.string.network_unavailable_message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getAddress() {
        mCityName = "";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(mLatitude,
                    mLongitude, 1);
            // Log.e(TAG, "addr : " + addresses.toString());
            if (addresses.size() > 0)
                mCityName = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLocationLabel.setText(mCityName); //Placeholder for location string
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = BuildConfig.DARK_SKY_FORECAST_API;
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;
        toggleRefresh();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(forecastUrl)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() { //Call on a new thread
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleRefresh();
                    }
                });
                alertUserAboutError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleRefresh();
                    }
                });
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        mForecast = parseForecastDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });
                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Exception caught: ", e);
                }
            }
        });
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        mTemperatureLabel.setText(String.valueOf(current.getTemperature()));
        //You could just add an empty string or use Double.toString() in this case
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumidityValue.setText(Double.toString(current.getHumidity()));
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());

        Drawable drawable =
                ResourcesCompat.getDrawable(getResources(), current.getIconId(), null);
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < days.length; i++) {
            JSONObject jsonDay = data.getJSONObject(i);

            days[i] = new Day();
            days[i].setTime(jsonDay.getLong("time"));
            days[i].setSummary(jsonDay.getString("summary"));
            days[i].setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            days[i].setIcon(jsonDay.getString("icon"));
            days[i].setTimezone(timezone);
        }
        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < hours.length; i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            hours[i] = new Hour();
            hours[i].setTime(jsonHour.getLong("time"));
            hours[i].setSummary(jsonHour.getString("summary"));
            hours[i].setTemperature(jsonHour.getDouble("temperature"));
            hours[i].setIcon(jsonHour.getString("icon"));
            hours[i].setTimezone(timezone);
        }
        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());
        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //This method requires permission ACCESS_NETWORK_STATE
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            //Checks if a network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view) {
        //We can use this as the context since this is a new method
        //called by our OnClickListener
        if (mForecast != null) {
            Intent intent = new Intent(this, DailyForecastActivity.class);
            intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
            intent.putExtra(getResources().getString(R.string.city_key), mCityName);
            startActivity(intent);
        }
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        if (mForecast != null) {
            Intent intent = new Intent(this, HourlyForecastActivity.class);
            intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
            startActivity(intent);
        }
    }
}


