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

import app.juky.squircleview.views.SquircleButton;

public class AccountHistoryActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private LineChart chart;
    private DBHelper dbHelper;
    private int currentUserId;
    private Handler handler;
    private Runnable runnable;
    private ChartMode currentMode = ChartMode.DEFAULT;
    private List<BACRecord> allBacRecords = new ArrayList<>();
    private SquircleButton sec15Button, minutelyButton, defaultButton;
    private static final long REFRESH_INTERVAL_MS = 3000;
    private static final int SAMPLE_COUNT = 10;
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
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        initializeChart();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long refreshInterval = REFRESH_INTERVAL_MS;
                if (currentMode == ChartMode.SEC_15) {
                    refreshInterval = 15000;
                } else if (currentMode == ChartMode.MINUTELY) {
                    refreshInterval = 60000;
                }

                if (currentTime - lastSaveTimestamp >= refreshInterval) {
                    executorService.submit(() -> {
                        fetchAndSaveAverageBACData();
                        handler.post(() -> {
                            displayBACData();
                            lastSaveTimestamp = currentTime;
                        });
                    });
                } else {
                    handler.post(() -> displayBACData());
                }
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };

        SettingsUtils.applySettings(this, sec15Button, minutelyButton, defaultButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        // Retrieve the saved list of average BAC values
        String json = sharedPreferences.getString("averageBACList", "[]");
        List<Float> savedAverageBACList = Utils.convertJsonToList(json);

        // Update the chart with the saved values
        LineData data = chart.getData();
        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }
        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        for (Float bac : savedAverageBACList) {
            data.addEntry(new Entry(set.getEntryCount(), bac), 0);
        }
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(50);
        chart.moveViewToX(data.getEntryCount());
        chart.invalidate();

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
        xl.setLabelCount(5, true);
        xl.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                switch (currentMode) {
                    case SEC_15:
                        return String.format(Locale.ENGLISH, "%.0f sec", value * 15);
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
                return String.format(Locale.ENGLISH, "%.3f BAC", value);
            }
        });

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        sec15Button = findViewById(R.id.sec15Button);
        minutelyButton = findViewById(R.id.minutelyButton);
        defaultButton = findViewById(R.id.defaultButton);

        sec15Button.setOnClickListener(v -> setChartMode(ChartMode.SEC_15));
        minutelyButton.setOnClickListener(v -> setChartMode(ChartMode.MINUTELY));
        defaultButton.setOnClickListener(v -> setChartMode(ChartMode.DEFAULT));
    }

    private void displayBACData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        // Retrieve the average BAC list from shared preferences
        String json = sharedPreferences.getString("averageBACList", "[]");
        List<Float> averageBacList = Utils.convertJsonToList(json);

        List<Entry> entries = calculateAverageBAC(currentMode);

        handler.post(() -> {
            LineData data = new LineData();
            LineDataSet set = createSet();
            set.setValues(entries);
            data.addDataSet(set);

            chart.setData(data);
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(50);
            chart.moveViewToX(data.getEntryCount());
            chart.invalidate();
        });
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(new ArrayList<>(), "BAC Data");
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
                return String.format(Locale.ENGLISH, "%.3f", entry.getY());
            }
        });

        return set;
    }

    private void fetchAndSaveAverageBACData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);

        executorService.submit(() -> {
            synchronized (dbHelper) {
                Cursor cursor = null;
                try {
                    cursor = dbHelper.getBACRecords(currentUserId);
                    if (cursor != null && cursor.moveToFirst()) {
                        List<BACRecord> tempBuffer = new ArrayList<>();
                        double totalBac = 0;
                        int recordCount = 0;

                        do {
                            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_TIMESTAMP));
                            double bac = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BAC_VALUE));
                            tempBuffer.add(new BACRecord(bac, timestamp));
                            totalBac += bac;
                            recordCount++;

                            if (tempBuffer.size() >= SAMPLE_COUNT) {
                                break;
                            }
                        } while (cursor.moveToNext());

                        if (recordCount > 0) {
                            double averageBac = totalBac / recordCount;

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putFloat("averageBAC", (float) averageBac);
                            editor.apply();

                            String json = sharedPreferences.getString("averageBACList", "[]");
                            List<Float> averageBacList = Utils.convertJsonToList(json);

                            averageBacList.add((float) averageBac);

                            editor.putString("averageBACList", Utils.convertListToJson(averageBacList));
                            editor.apply();
                        }
                    }
                } catch (Exception e) {
                    Log.e("fetchAndSaveAverageBACData", "Error accessing database", e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    private List<Entry> calculateAverageBAC(ChartMode mode) {
        List<Entry> entries = new ArrayList<>();
        switch (mode) {
            case SEC_15:
                entries = calculateSec15BAC();
                break;
            case MINUTELY:
                entries = calculateMinutelyBAC();
                break;
            case DEFAULT:
                entries = calculateDefaultBAC();
                break;
        }
        return entries;
    }

    private List<Entry> calculateSec15BAC() {
        List<Entry> entries = new ArrayList<>();
        double sumBAC = 0;
        int countBAC = 0;
        long startTime = System.currentTimeMillis() - 15000;
        for (BACRecord record : allBacRecords) {
            long timestamp = convertTimestampToMillis(record.getTimestamp());
            if (timestamp >= startTime) {
                sumBAC += record.getBacValue();
                countBAC++;
            }
        }

        if (countBAC > 0) {
            double averageBAC = sumBAC / countBAC;
            entries.add(new Entry(0, (float) averageBAC));
        }
        return entries;
    }

    private List<Entry> calculateMinutelyBAC() {
        List<Entry> entries = new ArrayList<>();
        double sumBAC = 0;
        int countBAC = 0;
        long startTime = System.currentTimeMillis() - 60000;
        for (BACRecord record : allBacRecords) {
            long timestamp = convertTimestampToMillis(record.getTimestamp());
            if (timestamp >= startTime) {
                sumBAC += record.getBacValue();
                countBAC++;
            }
        }

        if (countBAC > 0) {
            double averageBAC = sumBAC / countBAC;
            entries.add(new Entry(0, (float) averageBAC));
        }
        return entries;
    }

    private List<Entry> calculateDefaultBAC() {
        List<Entry> entries = new ArrayList<>();
        for (BACRecord record : allBacRecords) {
            String timestamp = record.getTimestamp();
            double bac = record.getBacValue();
            entries.add(new Entry(convertTimestampToMillis(timestamp) / 1000, (float) bac));
        }
        return entries;
    }

    private long convertTimestampToMillis(String timestamp) {
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
        if (e.getY() >= 0.27) {
            Intent intent = new Intent(AccountHistoryActivity.this, EmergencyActivity.class);
            startActivity(intent);
            showAlertDialog();
        }
    }

    @Override
    public void onNothingSelected() {
        Toast.makeText(this, "No Value Selected", Toast.LENGTH_SHORT).show();
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("High Level of Alcohol Detected")
                .setMessage("High level of alcohol detected, please act with caution and ask for help if necessary.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
