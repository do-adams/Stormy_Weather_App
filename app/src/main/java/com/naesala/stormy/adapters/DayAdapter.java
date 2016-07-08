package com.naesala.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.naesala.stormy.R;
import com.naesala.stormy.weather.Day;

import org.w3c.dom.Text;

public class DayAdapter extends BaseAdapter {
    private Context mContext;
    private Day[] mDays;

    public DayAdapter(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDays[position];
    }

    @Override
    public long getItemId(int position) {
        return 0; //we aren't going to use this. Tag items for easy reference.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            //sets a tag (stores the holder) for the view that we will re-use in a moment.
            convertView.setTag(holder);
        }
        else {
            //returns the holder object that was stored in convertView
            holder = (ViewHolder) convertView.getTag();
        }
        Day day = mDays[position];
        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(String.valueOf(day.getTemperatureMax()));

        if (position == 0) //The current day
            holder.dayLabel.setText("Today");
        else
            holder.dayLabel.setText(day.getDayOfTheWeek());

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView; //apparently, public by default, according to Ben
        TextView temperatureLabel;
        TextView dayLabel;
    }
}
