package com.devnet.myeportal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText svc_no, firstName, surname, password, confirmPassword, trade, dateOfBirth, attestationDate;
    private Spinner departmentSpinner, rankSpinner, formationSpinner;
    private Button btnRegister;
    private ProgressBar progressBar;
    private ImageView profileImage, uploadIcon;

    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private StorageReference storage;

    private Uri imageUri;

    // Image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImage.setImageURI(imageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize fields
        initializeFields();

        // Spinner data
        String[] departments = {"-- SELECT YOUR DEPARTMENT --", "OPERATIONS", "AERONAUTICAL ENGINEERING AND A.D.S", "GENERAL ENGINEERING", "ADMINISTRATION", "SUPPLY SERVICES", "FINANCE", "MEDICAL SERVICES", "TRAINING", "AUDIT & INSPECTION"};
        String[] ranks = {"-- SELECT YOUR RANK --", "LIEUTENANT GENERAL", "MAJOR GENERAL", "BRIGADIER GENERAL", "COLONEL", "LIEUTENANT COLONEL", "MAJOR", "CAPTAIN", "LIEUTENANT", "2ND LIEUTENANT", "WARRANT OFFICER CLASS I", "WARRANT OFFICER CLASS II", "FLIGHT SERGEANT", "SERGEANT", "CORPORAL", "SENIOR AIRCRAFTS MAN", "LEADING AIRCRAFTS MAN"};
        String[] formations = {"-- SELECT YOUR FORMATION --", "ZAF AIR HEADQUARTERS", "ZAF LUSAKA", "ZAF LIVINGSTONE", "ZAF KABWE", "ZAF MBALA", "ZAF MUMBWA", "ZAF NDOLA", "ZAF TWIN PALM", "ZAF LOGISTICS CMD", "ZAF IBEX AVIATION TOWN", "NORTHERN PADIC", "EASTERN PADIC", "WESTERN PADIC", "SOUTHERN PADIC"};

        // Setup spinners
        setupSpinner(departmentSpinner, departments);
        setupSpinner(rankSpinner, ranks);
        setupSpinner(formationSpinner, formations);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance().getReference("ProfileImages");

        // Register button click
        btnRegister.setOnClickListener(view -> registerUser());

        // Upload profile image
        uploadIcon.setOnClickListener(view -> openImageSelector());
    }

    private void initializeFields() {
        svc_no = findViewById(R.id.editText2);
        firstName = findViewById(R.id.firstNameEditText);
        surname = findViewById(R.id.editText);
        trade = findViewById(R.id.tradeEditText);
        dateOfBirth = findViewById(R.id.dateOfBirthPicker);
        attestationDate = findViewById(R.id.attestionDatePicker);
        password = findViewById(R.id.passwordEditText);
        confirmPassword = findViewById(R.id.confirmPasswordEditText);
        btnRegister = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        uploadIcon = findViewById(R.id.uploadIcon);
        profileImage = findViewById(R.id.profileImageView);
        departmentSpinner = findViewById(R.id.departmentSpinner);
        rankSpinner = findViewById(R.id.rankSpinner);
        formationSpinner = findViewById(R.id.currentFormationSpinner);

        // Date picker logic
        attestationDate.setOnClickListener(v -> showDatePickerDialog(attestationDate));
        dateOfBirth.setOnClickListener(v -> showDatePickerDialog(dateOfBirth));
    }

    private void setupSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter);
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> editText.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void registerUser() {
        String svcNo = svc_no.getText().toString().trim();
        String firstNameValue = firstName.getText().toString().trim();
        String surnameValue = surname.getText().toString().trim();
        String tradeValue = trade.getText().toString().trim();
        String dobValue = dateOfBirth.getText().toString().trim();
        String attestationValue = attestationDate.getText().toString().trim();
        String departmentValue = departmentSpinner.getSelectedItem().toString();
        String rankValue = rankSpinner.getSelectedItem().toString();
        String rankShortForm = getRankShortForm(rankValue);
        String username = rankShortForm + " " + surnameValue.toLowerCase();
        String email = firstNameValue + "." + surnameValue + "@eportal.mil.zm";
        String passwordValue = password.getText().toString().trim();
        String confirmPasswordValue = confirmPassword.getText().toString().trim();

        if (svcNo.isEmpty()) {
            Toast.makeText(this, "Service Number is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (firstNameValue.isEmpty()) {
            Toast.makeText(this, "First Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (surnameValue.isEmpty()) {
            Toast.makeText(this, "Surname is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tradeValue.isEmpty()) {
            Toast.makeText(this, "Trade is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (attestationValue.isEmpty()) {
            Toast.makeText(this, "Date of Attestation is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (departmentValue.isEmpty()) {
            Toast.makeText(this, "Department is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rankValue.isEmpty()) {
            Toast.makeText(this, "Rank is required.", Toast.LENGTH_SHORT).show();
            return;
        }


        if (passwordValue.isEmpty() || !passwordValue.equals(confirmPasswordValue)) {
            Toast.makeText(this, "Passwords do not match or are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please upload a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, passwordValue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadProfileImage(svcNo, firstNameValue, surnameValue, tradeValue, dobValue, attestationValue, departmentValue, rankValue, username, email);
                    } else {
                        Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                    }
                });
    }

    private String getRankShortForm(String rank) {
        HashMap<String, String> rankMap = new HashMap<>();
        rankMap.put("LIEUTENANT GENERAL", "LT GEN");
        rankMap.put("MAJOR GENERAL", "MAJ GEN");
        rankMap.put("BRIGADIER GENERAL", "BRIG GEN");
        rankMap.put("COLONEL", "COL");
        rankMap.put("LIEUTENANT COLONEL", "LT COL");
        rankMap.put("MAJOR", "MAJ");
        rankMap.put("CAPTAIN", "CAPT");
        rankMap.put("LIEUTENANT", "LT");
        rankMap.put("2ND LIEUTENANT", "2LT");
        rankMap.put("WARRANT OFFICER CLASS I", "WOI");
        rankMap.put("WARRANT OFFICER CLASS II", "WOII");
        rankMap.put("FLIGHT SERGEANT", "FSGT");
        rankMap.put("SERGEANT", "SGT");
        rankMap.put("CORPORAL", "CPL");
        rankMap.put("SENIOR AIRCRAFTS MAN", "SAC");
        rankMap.put("LEADING AIRCRAFTS MAN", "LAC");

        return rankMap.getOrDefault(rank, "UNKNOWN");
    }

    private void uploadProfileImage(String svcNo, String firstName, String surname, String trade, String dob, String attestation, String department, String rank, String username, String email) {
        StorageReference fileReference = storage.child(svcNo + ".jpg");
        fileReference.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> saveUserDataToFirebase(svcNo, firstName, surname, trade, dob, attestation, department, rank, username, email, uri.toString()));
                    } else {
                        Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                    }
                });
    }

    private void saveUserDataToFirebase(String svcNo, String firstName, String surname, String trade, String dob, String attestation, String department, String rank, String username, String email, String imageUrl) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("svcNumber", svcNo);
        userMap.put("firstName", firstName);
        userMap.put("surname", surname);
        userMap.put("trade", trade);
        userMap.put("dateOfBirth", dob);
        userMap.put("attestationDate", attestation);
        userMap.put("department", department);
        userMap.put("rank", rank);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("profileImageUrl", imageUrl);

        database.child(svcNo).setValue(userMap)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
