package com.devnet.myeportal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.google.android.material.slider.Slider;

public class SalaryAdvanceActivity extends AppCompatActivity {

    private EditText advanceAmountEditText, reasonEditText, startDateEditText, durationEditText, endDateEditText;
    private TextView advanceAmountValue;
    private Button submitButton;
    private ProgressBar progressBar;
    private DatabaseReference database;
    private Slider advanceAmountSlider;
    private String startDate;  // Start date (month-year) selected by the user
    private String endDate;    // Calculated end date (month-year)
    private String username;   // Username of the logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salary_advance);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        advanceAmountSlider = findViewById(R.id.advance_amount_slider);
        advanceAmountValue = findViewById(R.id.advance_amount_value);
        reasonEditText = findViewById(R.id.reason);
        startDateEditText = findViewById(R.id.startDateEditText);
        durationEditText = findViewById(R.id.durationEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        submitButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progressBar);

        // Fetch the username of the logged-in user
        fetchUsername();

        // Add input filters and text watcher to the duration edit text
        setupDurationInput();

        // Month-Year picker for the start date
        startDateEditText.setOnClickListener(view -> showMonthYearPicker());

        // Automatically calculate the end date based on the duration
        setupAutoEndDateCalculation();

        // Set OnClickListener for the submit button
        submitButton.setOnClickListener(v -> submitApplication());

        advanceAmountSlider.addOnChangeListener((slider, value, fromUser) -> {
            int selectedAmount = (int) value; // Cast float value to integer
            advanceAmountValue.setText("Selected Amount: " + selectedAmount);
        });

        // Submit button click listener
        submitButton.setOnClickListener(v -> {
            int selectedAmount = (int) advanceAmountSlider.getValue(); // Get slider value
            Toast.makeText(this, "Advance Amount: " + selectedAmount, Toast.LENGTH_SHORT).show();
            // Implement form submission logic
        });
    }

    private void fetchUsername() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        database.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.getValue(String.class);
                } else {
                    Toast.makeText(SalaryAdvanceActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalaryAdvanceActivity.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDurationInput() {
        durationEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        durationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.matches("\\d*")) {
                    durationEditText.setText(input.replaceAll("[^\\d]", ""));
                    durationEditText.setSelection(durationEditText.getText().length());
                }
            }
        });
    }

    private void setupAutoEndDateCalculation() {
        durationEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) updateEndDate();
        });
    }

    private void showMonthYearPicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startDate = String.format("%d-%02d", year, month + 1);
            startDateEditText.setText(startDate);
            updateEndDate();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

        // Hide day selection
        datePickerDialog.getDatePicker().findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(android.view.View.GONE);

        datePickerDialog.show();
    }

    private void updateEndDate() {
        if (startDate != null && !durationEditText.getText().toString().trim().isEmpty()) {
            int durationInMonths = Integer.parseInt(durationEditText.getText().toString().replaceAll("[^\\d]", ""));
            endDate = calculateEndDate(startDate, durationInMonths);
            endDateEditText.setText(endDate);
        }
    }

    private String calculateEndDate(String startDate, int durationInMonths) {
        String[] dateParts = startDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);

        Calendar start = new GregorianCalendar(year, month - 1, 1);
        start.add(Calendar.MONTH, durationInMonths);

        return String.format("%d-%02d", start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1);
    }

    private void submitApplication() {
        String amount = advanceAmountEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();

        if (TextUtils.isEmpty(amount) || TextUtils.isEmpty(duration) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(reason)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        AdvanceRequest request = new AdvanceRequest(userId, username, amount, duration, startDate, endDate, reason, "Pending", System.currentTimeMillis());

        database.child(userId).child("AdvanceRequests").push().setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference("UnapprovedRequests").push().setValue(request);
                        Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        advanceAmountEditText.setText("");
        durationEditText.setText("");
        startDateEditText.setText("");
        endDateEditText.setText("");
        reasonEditText.setText("");
    }
}
