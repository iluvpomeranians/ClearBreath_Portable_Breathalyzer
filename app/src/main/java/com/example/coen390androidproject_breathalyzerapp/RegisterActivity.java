package com.example.coen390androidproject_breathalyzerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import app.juky.squircleview.views.SquircleButton;
/// This class is responsible for handling the registration process of the user.
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge, editTextBMI;
    private Spinner spinnerGender;
    private SquircleButton btnRegister;
    private DBHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private int currentUserId = -1;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Lock the app to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("Account Registration");
        }

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        editTextBMI = findViewById(R.id.editTextBMI);
        btnRegister = findViewById(R.id.buttonRegister);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE); // Initialize sharedPreferences here

        setInputRestrictions();

        // Set up the gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> register());
        SettingsUtils.applySettings(this, editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword, editTextAge,editTextBMI, btnRegister);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(RegisterActivity.this, HomeActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(RegisterActivity.this, SettingsActivity.class);
            } else if (id == R.id.nav_bac_data) {
                intent = new Intent(RegisterActivity.this, BACDataActivity.class);
            } else if (id == R.id.nav_account) {
                intent = new Intent(RegisterActivity.this, AccountActivity.class);
            } else {
                return false;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToHome();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        updateMenuItems();
        boolean loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
    }

    private void setInputRestrictions() {
        editTextFullName.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                                return "";
                            }
                        }

                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(20);
                        editTextFullName.setFilters(filterArray);

                        return null;
                    }
                }
        });

        // Username: Only alphanumeric characters
        editTextUsername.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i))) {
                                return "";
                            }
                        }

                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(10);
                        editTextUsername.setFilters(filterArray);

                        return null;
                    }
                }
        });

        // Password: max length 20
        editTextPassword.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20)
        });

        editTextConfirmPassword.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(20)
        });

        // Age: Only numbers between 18 and 120
        editTextAge.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editTextAge.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(3)
        });

        // BMI: Only numbers with a single decimal point
        editTextBMI.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        editTextBMI.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String destText = dest.toString();
                        String sourceText = source.toString();

                        if (destText.contains(".") && sourceText.equals(".")) {
                            return "";
                        }

                        if (destText.contains(".") && (destText.length() - destText.indexOf(".")) > 2) {
                            return "";
                        }

                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(5);
                        editTextBMI.setFilters(filterArray);
                        return null;
                    }
                }
        });
    }

    private void register() {
        String fullName = editTextFullName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String ageStr = editTextAge.getText().toString().trim();
        String bmiStr = editTextBMI.getText().toString().trim();

        // General restrictions and validation
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                ageStr.isEmpty() || gender.isEmpty() || bmiStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        double bmi;
        try {
            bmi = Double.parseDouble(bmiStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid BMI", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 18 || age > 120) {
            Toast.makeText(this, "Age must be between 18 and 120", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bmi < 10.0 || bmi > 200.00) {
            Toast.makeText(this, "BMI must be between 10 and 200", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.insertAccount(fullName, username, password, gender, age, null, bmi);
        if (id > 0) {
            saveLoginState(true, (int) id);
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, AccountActivity.class);
            intent.putExtra("currentUserId", (int) id);
            updateMenuItems();
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginState(boolean loggedIn, int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.putInt("currentUserId", loggedIn ? userId : -1);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean loggedIn = sharedPreferences.getBoolean("loggedIn", false);
        currentUserId = sharedPreferences.getInt("currentUserId", -1);
        updateMenuItems();
    }

    private void updateMenuItems() {
        boolean isLoggedIn = sharedPreferences.getBoolean("loggedIn", false);
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            if (menu != null) {
                menu.findItem(R.id.nav_manage_account).setVisible(isLoggedIn);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateBackToHome() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
