package com.devnet.myeportal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FinanceActivity extends AppCompatActivity {

    private static final String TAG = "FinanceActivity";
    private RecyclerView recyclerView;
    private FinanceAdapter financeAdapter;
    private FirebaseFirestore firestore;
    private DatabaseReference database;
    private TextView payPoint, payMethod, notch, tax;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finance);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        firestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Set up RecyclerView
        setUpRecyclerView();

        // Fetch data
        fetchAccountsData();
        fetchFinanceData();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerview1);
        progressBar = findViewById(R.id.progressBar);
        payPoint = findViewById(R.id.payPointData);
        payMethod = findViewById(R.id.payMethodData);
        notch = findViewById(R.id.notchData);
        tax = findViewById(R.id.taxData);
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        financeAdapter = new FinanceAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(financeAdapter);
    }

    private void fetchFinanceData() {
        progressBar.setVisibility(View.VISIBLE);
        CollectionReference adminCollection = firestore.collection("menus/Finance/finance_act");
        adminCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FinanceItem> financeItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String iconUrl = document.getString("iconUrl");

                        if (name != null && iconUrl != null) {
                            financeItems.add(new FinanceItem(name, iconUrl));
                        } else {
                            Log.w(TAG, "Missing fields in document: " + document.getId());
                        }
                    }

                    financeAdapter.setItems(financeItems);
                    progressBar.setVisibility(View.GONE);

                    if (financeItems.isEmpty()) {
                        showToast("No admin items found.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching admin items", e);
                    showToast("Failed to load admin items.");
                });
    }

    private void fetchAccountsData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child("Users").child(userId).child("financeDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Snapshot: " + snapshot.toString());
                    String fetchedPayPoint = snapshot.child("payPoint").getValue(String.class);
                    String fetchedPayMethod = snapshot.child("payMethod").getValue(String.class);
                    String fetchedNotch = snapshot.child("notch").getValue(String.class);
                    String fetchedTax = snapshot.child("tax").getValue(String.class);

                    Log.d(TAG, "Fetched data: payPoint=" + fetchedPayPoint + ", payMethod=" + fetchedPayMethod
                            + ", notch=" + fetchedNotch + ", tax=" + fetchedTax);

                    payPoint.setText(fetchedPayPoint != null ? fetchedPayPoint : "0");
                    payMethod.setText(fetchedPayMethod != null ? fetchedPayMethod : "0");
                    notch.setText(fetchedNotch != null ? fetchedNotch : "N/A");
                    tax.setText(fetchedTax != null ? fetchedTax : "N/A");
                } else {
                    Log.d(TAG, "Snapshot does not exist.");
                    Toast.makeText(FinanceActivity.this, "No Data found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(FinanceActivity.this, "Failed to fetch Data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(FinanceActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
