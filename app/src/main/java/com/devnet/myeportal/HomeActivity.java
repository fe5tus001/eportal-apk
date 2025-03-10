package com.devnet.myeportal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private MenuAdapter menuAdapter;
    private List<Menu> menuItemList = new ArrayList<>();

    private RecyclerView recyclerView;
    private ViewPager viewPager;

    private List<SlideItem> slideItems = new ArrayList<>();

    private Handler handler = new Handler();
    private Runnable slideRunnable;
    private ProgressBar progressBar;
    private ImageView btnLogout;
    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        menuAdapter = new MenuAdapter(this, menuItemList);
        recyclerView.setAdapter(menuAdapter);

        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
        btnLogout = findViewById(R.id.btn_logout);
        usernameTextView = findViewById(R.id.usernameTextView);

        // Fetch data
        fetchMenuItems();
        fetchSlideImages();

        // Logout button click listener with confirmation dialog
        btnLogout.setOnClickListener(view -> showLogoutConfirmationDialog());

        fetchUsername();
    }

    private void fetchUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            DatabaseReference userRef = databaseReference.child("Users").child(uid).child("username");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.getValue(String.class); // Get the username
                        usernameTextView.setText("Welcome, " + username + "!"); // Display the username
                    } else {
                        Toast.makeText(HomeActivity.this, "Username not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load username.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void fetchMenuItems() {
        showLoading();
        db.collection("menus")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        menuItemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String iconUrl = document.getString("iconUrl");
                            menuItemList.add(new Menu(name, iconUrl));
                        }
                        menuAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("Firestore", "Error getting menus.", task.getException());
                    }
                    hideLoading();
                });
    }

    private void fetchSlideImages() {
        db.collection("news_images")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        slideItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("image_url");
                            String headline = document.getString("headline");
                            String story = document.getString("story");
                            slideItems.add(new SlideItem(imageUrl, headline, story));
                        }

                        SliderAdapter adapter = new SliderAdapter(HomeActivity.this, slideItems);
                        viewPager.setAdapter(adapter);
                        startAutoSlide();
                    } else {
                        Log.w("Firestore", "Error getting slides.", task.getException());
                    }
                });
    }

    private void startAutoSlide() {
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                if (!slideItems.isEmpty()) {
                    int currentItem = viewPager.getCurrentItem();
                    int nextItem = (currentItem + 1) % slideItems.size();
                    viewPager.setCurrentItem(nextItem, true);
                    handler.postDelayed(this, 5000);
                }
            }
        };
        handler.postDelayed(slideRunnable, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(slideRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!slideItems.isEmpty()) {
            startAutoSlide();
        }
    }
}
