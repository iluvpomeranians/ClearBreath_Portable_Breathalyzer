package com.example.coen390androidproject_breathalyzerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.juky.squircleview.views.SquircleButton;
import java.util.concurrent.CopyOnWriteArrayList;

public class AccountHistoryActivity extends AppCompatActivity implements OnChartValueSelectedListener{
    private LineChart chart;
    private DBHelper dbHelper;
    private int currentUserId;
    private Handler handler;
    private Runnable runnable;
    private ChartMode currentMode = ChartMode.DEFAULT;
    private CopyOnWriteArrayList<BACRecord> allBacRecords = new CopyOnWriteArrayList<>();
    private SquircleButton sec15Button, minutelyButton, defaultButton;
    private static final long REFRESH_INTERVAL_MS = 3000;
    private long lastSaveTimestamp = 0;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private enum ChartMode {
        SEC_15, MINUTELY, DEFAULT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_account_history);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account History");
        }

        chart = findViewById(R.id.chart1);
        dbHelper = new DBHelper(this);
        Log.d("AccountHistoryActivity", "DBHelper initialized: " + (dbHelper != null));
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        initializeChart();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSaveTimestamp >= REFRESH_INTERVAL_MS) {
                    executorService.submit(() -> {
                        try {
                            readBACDataFromDatabase();
                            handler.post(() -> {
                                displayBACData();
                                lastSaveTimestamp = currentTime;
                                checkForHighBAC();
                            });
                        } catch (Exception e) {
                            Log.e("Runnable", "Error reading BAC data from database", e);
                        }
                    });
                } else {
                    handler.post(() -> {
                        displayBACData();
                        checkForHighBAC();
                    });
                }
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };

        sec15Button = findViewById(R.id.sec15Button);
        minutelyButton = findViewById(R.id.minutelyButton);
        defaultButton = findViewById(R.id.defaultButton);

        SettingsUtils.applySettings(this, sec15Button, minutelyButton, defaultButton);
        sec15Button.setOnClickListener(v -> setChartMode(ChartMode.SEC_15));
        minutelyButton.setOnClickListener(v -> setChartMode(ChartMode.MINUTELY));
        defaultButton.setOnClickListener(v -> setChartMode(ChartMode.DEFAULT));

        handler.post(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
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
        chart.setPinchZoom(false);
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
        xl.setLabelCount(4, true);
        xl.setAxisMaximum(45); // Setting the maximum x-axis range to 45 seconds for "15 SEC" mode
        xl.setAxisMinimum(0);  // Setting the minimum x-axis range to 0 seconds for "15 SEC" mode
        xl.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch (currentMode) {
                    case SEC_15:
                        return String.format(Locale.ENGLISH, "%.0f sec", value);
                    case MINUTELY:
                        return String.format(Locale.ENGLISH, "%.0f min", value);
                    case DEFAULT:
                        return String.format(Locale.ENGLISH, "%.0f sec", value * 3);
                    default:
                        return String.valueOf(value);
                }
            }
        });

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
    }

    private void readBACDataFromDatabase() {
        Cursor cursor = dbHelper.getBACRecords(currentUserId);
        if (cursor != null) {
            allBacRecords.clear();
            while (cursor.moveToNext()) {
                double bacValue = cursor.getDouble(cursor.getColumnIndexOrThrow("bac_value"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                BACRecord record = new BACRecord(bacValue, timestamp);
                allBacRecords.add(record);
            }
            cursor.close();
        }
    }

    private void displayBACData() {
        switch (currentMode) {
            case SEC_15:
                displaySec15BACData();
                break;
            case MINUTELY:
                displayMinutelyBACData();
                break;
            case DEFAULT:
                displayDefaultBACData();
                break;
        }
    }

    private void displayDefaultBACData() {
        LineData data = chart.getData();
        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet("Default Mode");
            data.addDataSet(set);
        }

        set.clear();
        int xIndex = 0;
        for (BACRecord record : allBacRecords) {
            float formattedBac = (float) Math.round(record.getBacValue() * 1000) / 1000f;
            data.addEntry(new Entry(xIndex++, formattedBac), 0);
        }

        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(50);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();
    }

    private void displaySec15BACData() {
        List<Entry> entries = calculateSec15BAC();

        LineData data = chart.getData();
        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet("15 Sec Mode");
            data.addDataSet(set);
        }

        set.clear();
        for (Entry entry : entries) {
            data.addEntry(entry, 0);
        }

        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(45);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();
    }

    private void displayMinutelyBACData() {
        List<Entry> entries = calculateMinutelyBAC();

        LineData data = chart.getData();
        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet("Minutely Mode");
            data.addDataSet(set);
        }

        set.clear();
        int xIndex = 0;
        for (Entry entry : entries) {
            data.addEntry(new Entry(xIndex++, entry.getY()), 0);
        }

        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(50);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();

    }

    private List<Entry> calculateSec15BAC() {
        List<Entry> entries = new ArrayList<>();
        double[] sumBAC = new double[4]; // 15-sec intervals (0-15, 15-30, 30-45, 45-60)
        int[] countBAC = new int[4];

        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            int second = getSecondFromTimestamp(timestamp);
            int interval = (second % 60) / 15; // 0-15 -> 0, 15-30 -> 1, 30-45 -> 2, 45-60 -> 3
            sumBAC[interval] += bac;
            countBAC[interval]++;
        }

        for (int i = 0; i < 4; i++) {
            if (countBAC[i] > 0) {
                double averageBAC = sumBAC[i] / countBAC[i];
                entries.add(new Entry(i * 15, (float) averageBAC));
            }
        }
        return entries;
    }

    private List<Entry> calculateMinutelyBAC() {
        List<Entry> entries = new ArrayList<>();
        double[] sumBAC = new double[60]; // Minute intervals
        int[] countBAC = new int[60];
        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            int minute = getMinuteFromTimestamp(timestamp);
            sumBAC[minute] += bac;
            countBAC[minute]++;
        }

        for (int i = 0; i < 60; i++) {
            if (countBAC[i] > 0) {
                double averageBAC = sumBAC[i] / countBAC[i];
                entries.add(new Entry(i, (float) averageBAC));
            }
        }
        return entries;
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

    private void checkForHighBAC() {
        int highBACCount = 0;
        long currentTime = System.currentTimeMillis();
        long thresholdTime = currentTime - 1000;

        for (int i = allBacRecords.size() - 1; i >= 0; i--) {
            BACRecord record = allBacRecords.get(i);
            long timestamp = (long) convertTimestampToMillis(record.getTimestamp());
            if (timestamp >= thresholdTime) {
                if (record.getBacValue() >= 0.20) {
                    highBACCount++;
                    if (highBACCount >= 2) {
                        runOnUiThread(() -> showHighBACDialog());
                        return;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void showHighBACDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HIGH LEVEL OF BAC DETECTED")
                .setMessage("PLEASE PROCEED WITH CAUTION AND GET HELP IF NEEDED")
                .setPositiveButton("OK", (dialog, which) -> navigateToEmergencyActivity())
                .setCancelable(false)
                .show();
    }

    private void navigateToEmergencyActivity() {
        Intent intent = new Intent(this, EmergencyActivity.class);
        intent.putExtra("showHighBACDialog", true);
        startActivity(intent);
        finish();
    }

    private int getSecondFromTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(timestamp);
            return date != null ? date.getSeconds() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
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

    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(new ArrayList<>(), label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setDrawFilled(true);
        set.setFillDrawable(createGradientDrawable());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawValues(true);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format(Locale.ENGLISH, "%.3f", entry.getY()); // 3 decimal places
            }
        });
        return set;
    }

    private GradientDrawable createGradientDrawable() {
        int[] colors = {ColorTemplate.getHoloBlue(), Color.TRANSPARENT};
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
    }

    private void setChartMode(ChartMode mode) {
        currentMode = mode;
        displayBACData();
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

