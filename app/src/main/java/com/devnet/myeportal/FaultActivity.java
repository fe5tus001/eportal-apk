package com.devnet.myeportal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FaultActivity extends AppCompatActivity {

    private EditText faultDescription;
    private Spinner categorySpinner;
    private ImageView uploadedImage;
    private ProgressBar progressBar;
    private Button submitButton;
    private Uri imageUri;
    private ListView faultHistoryListView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private List<String> faultHistory;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fault);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("FaultReports");
        storageReference = FirebaseStorage.getInstance().getReference("FaultImages");

        // Initialize UI components
        faultDescription = findViewById(R.id.faultDescription);
        categorySpinner = findViewById(R.id.categorySpinner);
        uploadedImage = findViewById(R.id.uploadedImage);
        progressBar = findViewById(R.id.progressBar);
        submitButton = findViewById(R.id.submitFaultButton);
        faultHistoryListView = findViewById(R.id.faultHistoryListView);

        faultHistory = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, faultHistory);
        faultHistoryListView.setAdapter(adapter);

        // Set up category spinner
        String[] categories = {"Select a category", "Electrical", "Plumbing", "Mechanical", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categorySpinner.setAdapter(categoryAdapter);

        // Image selection on clicking ImageView
        uploadedImage.setOnClickListener(v -> pickImage());

        // Submit fault report
        submitButton.setOnClickListener(v -> submitFaultReport());

        // Load previous fault history
        loadFaultHistory();
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    uploadedImage.setImageURI(imageUri);
                }
            }
    );

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void submitFaultReport() {
        String description = faultDescription.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(description) || category.equals("Select a category")) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String houseNumber = dataSnapshot.child("houseNumber").getValue(String.class);
                String username = dataSnapshot.child("username").getValue(String.class); // Fetch username

                if (TextUtils.isEmpty(houseNumber) || TextUtils.isEmpty(username)) {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    Toast.makeText(FaultActivity.this, "User details incomplete. Please update your profile.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String faultId = houseNumber + "_" + date;

                if (imageUri != null) {
                    uploadImageAndSaveFault(faultId, description, category, userId, userEmail, houseNumber, username);
                } else {
                    saveFaultToDatabase(faultId, description, category, userId, userEmail, houseNumber, username, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                Toast.makeText(FaultActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageAndSaveFault(String faultId, String description, String category,
                                         String userId, String userEmail, String houseNumber, String username) {
        StorageReference imageRef = storageReference.child(faultId + ".jpg");

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveFaultToDatabase(faultId, description, category, userId, userEmail, houseNumber, username, uri.toString());
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            Toast.makeText(FaultActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveFaultToDatabase(String faultId, String description, String category,
                                     String userId, String userEmail, String houseNumber, String username, String imageUrl) {
        Map<String, Object> faultReport = new HashMap<>();
        faultReport.put("faultId", faultId);
        faultReport.put("description", description);
        faultReport.put("category", category);
        faultReport.put("userId", userId);
        faultReport.put("userEmail", userEmail);
        faultReport.put("houseNumber", houseNumber);
        faultReport.put("username", username);
        faultReport.put("imageUri", imageUrl);
        faultReport.put("timestamp", System.currentTimeMillis());
        faultReport.put("status", "Pending");

        databaseReference.child(faultId).setValue(faultReport).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(FaultActivity.this, "Fault reported successfully!", Toast.LENGTH_SHORT).show();
                faultDescription.setText("");
                categorySpinner.setSelection(0);
                uploadedImage.setImageURI(null);
                imageUri = null;
                loadFaultHistory();
            } else {
                Toast.makeText(FaultActivity.this, "Failed to report fault. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFaultHistory() {
        // (Fault history logic remains unchanged)
    }
}
