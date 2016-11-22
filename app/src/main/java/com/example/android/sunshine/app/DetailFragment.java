package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.utils.Constants;
import com.example.android.sunshine.app.utils.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Батинчук on 13.09.2016.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private String mForecastString;
    private Uri mUri;

    private ShareActionProvider mShareActionProvider;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,

    };

    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_ID = 9;

    @BindView(R.id.detail_date_textview)
    TextView mDateView;
    @BindView(R.id.detail_date_full_textview)
    TextView mFullDateView;
    @BindView(R.id.detail_high_temp_textview)
    TextView mHighTempView;
    @BindView(R.id.detail_low_temp_textview)
    TextView mLowTempView;
    @BindView(R.id.detail_icon)
    ImageView mIconView;
    @BindView(R.id.detail_humidity_textview)
    TextView mHumidityView;
    @BindView(R.id.detail_description)
    TextView mDescriptionView;
    @BindView(R.id.detail_wind_textview)
    TextView mWindView;
    @BindView(R.id.detail_pressure_textview)
    TextView mPressureView;

    private Unbinder unbinder;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if (mForecastString != null) {
            setShareIntent(createShareIntent());
        }
    }

    public Intent createShareIntent() {
        // populate the share intent with data
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastString + Constants.forecastHashTag);
        return shareIntent;
    }


    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        } else {
            Log.d("LOG SHARE", "Share Action Provider is null?");
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor
            data) {
        Log.v("LOG " + getActivity(), "In onLoadFinished");

        if (!data.moveToFirst()) {
            return;
        }

        long dateInMillis = data.getLong(COL_WEATHER_DATE);
        String dateString = Utility.getDayName(getActivity(), dateInMillis);
        String fullDateString = Utility.getFormattedMonthDay(getActivity(), dateInMillis);

        String weatherDescription = data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP));
        String low = Utility.formatTemperature(
                getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP));

        Double humidityValue = data.getDouble(COL_WEATHER_HUMIDITY);
        String humidity = Utility.getHumidityString(getActivity(), humidityValue);


        // String humidity = data.getString(COL_WEATHER_HUMIDITY);

        float windDegrees = data.getFloat(COL_WEATHER_DEGREES);
        float windSpeed = data.getFloat(COL_WEATHER_WIND);
        String wind = Utility.getFormattedWind(getActivity(), windSpeed, windDegrees);

        Double pressureValue = data.getDouble(COL_WEATHER_PRESSURE);
        String pressure = Utility.getPressureString(getActivity(), pressureValue);

        int weatherId = data.getInt(COL_WEATHER_ID);

        mDateView.setText(dateString);
        mFullDateView.setText(fullDateString);
        mHighTempView.setText(high);
        mLowTempView.setText(low);
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        mDescriptionView.setText(weatherDescription);
        mHumidityView.setText(humidity);
        mWindView.setText(wind);
        mPressureView.setText(pressure);

//        detailDate.setText(mForecastString);

        mForecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }
}