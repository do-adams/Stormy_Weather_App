package com.naesala.stormy.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.naesala.stormy.R;
import com.naesala.stormy.weather.Hour;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {
    private Hour[] mHours;
    private Context mContext;

    public HourAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;
    }

    //method will be called whenever a new ViewHolder is needed
    //this is the spot where we create new views when they are needed
    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_list_item, parent, false);

        HourViewHolder viewHolder = new HourViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder holder, int position) {
        holder.bindHour(mHours[position]);
    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    public class HourViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener {
        public TextView mTimeLabel;
        public TextView mSummaryLabel;
        public TextView mTemperatureLabel;
        public ImageView mIconImageView;
        //We could use ButterKnife here as well

        public HourViewHolder(View itemView) {
            super(itemView);

            mTimeLabel = (TextView) itemView.findViewById(R.id.timeLabel);
            mSummaryLabel = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTemperatureLabel = (TextView) itemView.findViewById(R.id.temperatureLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);
        }

        public void bindHour(Hour hour) {
            mTimeLabel.setText(hour.getHour());
            mSummaryLabel.setText(hour.getSummary());
            mTemperatureLabel.setText(String.valueOf(hour.getTemperature()));
            mIconImageView.setImageResource(hour.getIconId());

            //itemView is a parent field set during the call to super(itemView)
            //we can use it anywhere in this class
            itemView.setOnClickListener(this); //sets the on click listener
        }

        @Override
        public void onClick(View v) {
            String time = mTimeLabel.getText().toString(); //getText returns a charSequence, we need to convert it
            String temperature = mTemperatureLabel.getText().toString();
            String summary = mSummaryLabel.getText().toString();
            String message = String.format("At %s it will be %s and %s",
                    time, temperature, summary);
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }
}
