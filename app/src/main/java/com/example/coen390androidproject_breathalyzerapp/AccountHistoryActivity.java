package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountHistoryActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private LineChart chart;
    private DBHelper dbHelper;
    private int currentUserId;
    private Handler handler;
    private Runnable runnable;
    private ChartMode currentMode = ChartMode.HOURLY;
    private List<BACRecord> allBacRecords = new ArrayList<>(); // Global list to store all BAC records
    private Button hourlyButton, dailyButton, weeklyButton;

    private static final long REFRESH_INTERVAL_MS = 2000; // 10 seconds
    private static final int SAMPLE_COUNT = 10;
    private List<BACRecord> bacRecordBuffer = new ArrayList<>();
    private long lastSaveTimestamp = 0;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private enum ChartMode {
        HOURLY, DAILY, WEEKLY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_account_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account History");
        }

        chart = findViewById(R.id.chart1);
        dbHelper = new DBHelper(this);
        currentUserId = getIntent().getIntExtra("currentUserId", -1);

        initializeChart();

        // Set up a handler for real-time updates
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSaveTimestamp >= REFRESH_INTERVAL_MS) {
                    executorService.submit(() -> {
                        fetchAndSaveAverageBACData();
                        lastSaveTimestamp = currentTime;

                        // Update UI on the main thread
                        handler.post(() -> displayBACData());
                    });
                } else {
                    // Update UI on the main thread
                    handler.post(() -> displayBACData());
                }
                handler.postDelayed(this, REFRESH_INTERVAL_MS); // Update every 10 seconds
            }
        };

        SettingsUtils.applySettings(this, hourlyButton, dailyButton, weeklyButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable); // Start the updates
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Stop the updates
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    private void initializeChart() {
        chart.setOnChartValueSelectedListener(this);
        chart.getDescription().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false); // Pinch Zoom Disabled
        chart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(false);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setGranularityEnabled(true);
        xl.setGranularity(1f);
        xl.setLabelCount(5, true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(0.100f);
        leftAxis.setAxisMinimum(0.000f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(0.001f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.ENGLISH, "%.3f", value);
            }
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add buttons for HOURLY, DAILY, WEEKLY
        hourlyButton = findViewById(R.id.hourlyButton);
        dailyButton = findViewById(R.id.dailyButton);
        weeklyButton = findViewById(R.id.weeklyButton);

        hourlyButton.setOnClickListener(v -> setChartMode(ChartMode.HOURLY));
        dailyButton.setOnClickListener(v -> setChartMode(ChartMode.DAILY));
        weeklyButton.setOnClickListener(v -> setChartMode(ChartMode.WEEKLY));
    }

    private void displayBACData() {
        executorService.submit(() -> {
            if (allBacRecords.isEmpty()) {
                Cursor cursor = dbHelper.getBACRecords(currentUserId);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                        double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                        bac = roundToThreeDecimalPlaces(bac);
                        allBacRecords.add(new BACRecord(bac, timestamp));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
            }

            List<Entry> entries = new ArrayList<>();
            switch (currentMode) {
                case HOURLY:
                    entries = calculateAverageBAC(ChartMode.HOURLY);
                    break;
                case DAILY:
                    entries = calculateAverageBAC(ChartMode.DAILY);
                    break;
                case WEEKLY:
                    entries = calculateAverageBAC(ChartMode.WEEKLY);
                    break;
            }

            LineDataSet set = new LineDataSet(entries, "BAC Data");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ColorTemplate.getHoloBlue());
            set.setLineWidth(2f);
            set.setDrawFilled(true);
            set.setFillDrawable(createGradientDrawable());
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setDrawValues(true);

            handler.post(() -> {
                LineData data = chart.getData();
                if (data != null) {
                    data.clearValues();
                    data.addDataSet(set);
                    chart.setData(data);
                    chart.notifyDataSetChanged();
                    chart.invalidate();
                }
            });
        });
    }


    private void fetchAndSaveAverageBACData() {
        executorService.submit(() -> {
            Cursor cursor = dbHelper.getBACRecords(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                List<BACRecord> tempBuffer = new ArrayList<>();
                do {
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                    double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                    tempBuffer.add(new BACRecord(bac, timestamp));

                    if (tempBuffer.size() >= SAMPLE_COUNT) {
                        double averageBac = 0;
                        for (BACRecord record : tempBuffer) {
                            averageBac += record.getBacValue();
                        }
                        averageBac /= SAMPLE_COUNT;

                        String firstTimestamp = tempBuffer.get(0).getTimestamp();
                        dbHelper.insertBACRecord(currentUserId, firstTimestamp, averageBac);
                        tempBuffer.clear();
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        });
    }

    private float convertTimestampToMillis(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(timestamp);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private GradientDrawable createGradientDrawable() {
        int[] colors = {ColorTemplate.getHoloBlue(), Color.TRANSPARENT};
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);
        return gradientDrawable;
    }

    private void setChartMode(ChartMode mode) {
        currentMode = mode;
        displayBACData(); // Refresh the chart with the new mode
    }

    private List<Entry> calculateAverageBAC(ChartMode mode) {
        List<Entry> entries = new ArrayList<>();
        switch (mode) {
            case HOURLY:
                entries = calculateHourlyBAC();
                break;
            case DAILY:
                entries = calculateDailyBAC();
                break;
            case WEEKLY:
                entries = calculateWeeklyBAC();
                break;
        }
        return entries;
    }

    private List<Entry> calculateHourlyBAC() {
        List<Entry> entries = new ArrayList<>();
        double[] sumBAC = new double[12];
        int[] countBAC = new int[12];
        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            int minute = getMinuteFromTimestamp(timestamp);
            int interval = minute / 5;
            sumBAC[interval] += bac;
            countBAC[interval]++;
        }

        for (int i = 0; i < 12; i++) {
            if (countBAC[i] > 0) {
                double averageBAC = sumBAC[i] / countBAC[i];
                entries.add(new Entry(i * 5, (float) averageBAC));
            }
        }
        return entries;
    }

    private List<Entry> calculateDailyBAC() {
        List<Entry> entries = new ArrayList<>();
        double[] sumBAC = new double[24];
        int[] countBAC = new int[24];
        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            int hour = getHourFromTimestamp(timestamp);
            sumBAC[hour] += bac;
            countBAC[hour]++;
        }

        for (int i = 0; i < 24; i++) {
            if (countBAC[i] > 0) {
                double averageBAC = sumBAC[i] / countBAC[i];
                entries.add(new Entry(i, (float) averageBAC));
            }
        }
        return entries;
    }

    private List<Entry> calculateWeeklyBAC() {
        List<Entry> entries = new ArrayList<>();
        double[] sumBAC = new double[7];
        int[] countBAC = new int[7];
        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            int day = getDayFromTimestamp(timestamp);
            sumBAC[day] += bac;
            countBAC[day]++;
        }

        for (int i = 0; i < 7; i++) {
            if (countBAC[i] > 0) {
                double averageBAC = sumBAC[i] / countBAC[i];
                entries.add(new Entry(i, (float) averageBAC));
            }
        }
        return entries;
    }

    private int getMinuteFromTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(timestamp);
            return date != null ? date.getMinutes() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getHourFromTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(timestamp);
            return date != null ? date.getHours() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getDayFromTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(timestamp);
            return date != null ? date.getDay() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double roundToThreeDecimalPlaces(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AccountHistoryActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.toggle_values) {
            toggleValues();
        } else if (itemId == R.id.toggle_filled) {
            toggleFilled();
        } else if (itemId == R.id.animate_x) {
            chart.animateX(1000, Easing.EaseInOutQuad);
        } else if (itemId == R.id.animate_y) {
            chart.animateY(1000, Easing.EaseInOutQuad);
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleValues() {
        List<ILineDataSet> sets = chart.getData().getDataSets();
        for (ILineDataSet iSet : sets) {
            LineDataSet set = (LineDataSet) iSet;
            set.setDrawValues(!set.isDrawValuesEnabled());
        }
        chart.invalidate();
    }

    private void toggleFilled() {
        List<ILineDataSet> sets = chart.getData().getDataSets();
        for (ILineDataSet iSet : sets) {
            LineDataSet set = (LineDataSet) iSet;
            if (set.isDrawFilledEnabled()) {
                set.setDrawFilled(false);
            } else {
                set.setDrawFilled(true);
                set.setFillDrawable(createGradientDrawable());
            }
        }
        chart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(this, "Value Selected: " + e.getY(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {
        Toast.makeText(this, "No Value Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        MenuItem refreshItem = menu.add("Refresh Chart");
        refreshItem.setOnMenuItemClickListener(item -> {
            displayBACData();
            return true;
        });
        return true;
    }
}
