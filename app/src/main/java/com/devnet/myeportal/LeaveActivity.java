package com.devnet.myeportal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

public class LeaveActivity extends AppCompatActivity {

    private TextView vacationLeaveDaysText, occasionalLeaveDaysText, lastLeaveDateText, nextVacationDueText, daysText;
    private EditText destinationText, durationEditText, startDateEditText, endDateEditText, reasonEditText;
    private Spinner leaveTypeSpinner, provinceSpinner;
    private Button submitButton;
    private ProgressBar progressBar;
    private DatabaseReference database;

    private String startDate;  // Start date selected by the user
    private String endDate;    // Calculated end date
    private String username;   // Username of the logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_leave);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views
        vacationLeaveDaysText = findViewById(R.id.vacationLeaveDays);
        occasionalLeaveDaysText = findViewById(R.id.occasionalLeaveDays);
        lastLeaveDateText = findViewById(R.id.lastLeaveDate);
        nextVacationDueText = findViewById(R.id.nextVacationDue);
        destinationText = findViewById(R.id.destinationText);
        durationEditText = findViewById(R.id.durationEditText);
        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        provinceSpinner = findViewById(R.id.provinceSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
        daysText = findViewById(R.id.daysText);

        // Set up the leave type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.leave_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.provinces, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(adapter2);

        // Fetch the username of the logged-in user
        fetchUsername();

        // Add input filters and text watcher to the duration edit text
        setupDurationInput();

        // Date selection logic
        startDateEditText.setOnClickListener(view -> showDatePicker());

        // Automatically calculate the end date
        setupAutoEndDateCalculation();

        fetchLeaveData();

        // Set OnClickListener for the submit button
        submitButton.setOnClickListener(v -> submitApplication());
    }

    private void fetchUsername() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        database.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.getValue(String.class);
                } else {
                    Toast.makeText(LeaveActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaveActivity.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLeaveData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child(userId).child("leaveDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String vacationLeaveDays = snapshot.child("vacationLeaveDays").getValue(String.class);
                    String occasionalLeaveDays = snapshot.child("occasionalLeaveDays").getValue(String.class);
                    String lastLeaveDate = snapshot.child("lastLeaveDate").getValue(String.class);
                    String nextVacationDue = snapshot.child("nextVacationDue").getValue(String.class);

                    vacationLeaveDaysText.setText(vacationLeaveDays != null ? vacationLeaveDays : "0");
                    occasionalLeaveDaysText.setText(occasionalLeaveDays != null ? occasionalLeaveDays : "0");
                    lastLeaveDateText.setText(lastLeaveDate != null ? lastLeaveDate : "N/A");
                    nextVacationDueText.setText(nextVacationDue != null ? nextVacationDue : "N/A");
                } else {
                    Toast.makeText(LeaveActivity.this, "No leave details found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LeaveActivity.this, "Failed to fetch leave details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDurationInput() {
        durationEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

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

    private void showDatePicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            startDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            startDateEditText.setText(startDate);
            updateEndDate();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateEndDate() {
        if (startDate != null && !durationEditText.getText().toString().isEmpty()) {
            int duration = Integer.parseInt(durationEditText.getText().toString().replaceAll("[^\\d]", ""));
            endDate = calculateEndDate(startDate, duration);
            endDateEditText.setText(endDate);
        }
    }

    private String calculateEndDate(String startDate, int duration) {
        String[] dateParts = startDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int day = Integer.parseInt(dateParts[2]);

        Calendar start = new GregorianCalendar(year, month, day);
        start.add(Calendar.DAY_OF_MONTH, duration);

        return String.format("%d-%02d-%02d", start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH));
    }

    private void submitApplication() {
        String destination = destinationText.getText().toString();
        String duration = durationEditText.getText().toString();
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString();

        if (destination.isEmpty() || duration.isEmpty() || startDate == null || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LeaveRequest request = new LeaveRequest(userId, username, destination, duration, leaveType, startDate, endDate, reason, "Pending", System.currentTimeMillis());

        database.child(userId).child("LeaveRequests").push().setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference("UnapprovedRequests").push().setValue(request);
                        Toast.makeText(this, "Leave request submitted!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        destinationText.setText("");
        durationEditText.setText("");
        leaveTypeSpinner.setSelection(0);
        startDateEditText.setText("");
        endDateEditText.setText("");
        reasonEditText.setText("");
    }
}
