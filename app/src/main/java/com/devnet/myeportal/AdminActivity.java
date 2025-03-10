package com.devnet.myeportal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class AdminActivity extends AppCompatActivity {

	private static final String TAG = "AdminActivity";
	private RecyclerView recyclerView;
	private AdminAdapter adminAdapter;
	private FirebaseFirestore firestore;
	private TextView fullNameTextView, rankTextView, svcNumberTextView, tradeTextView, formationTextView;
	private ProgressBar progressBar;
	private ImageView profileImageView;
	private FirebaseAuth mAuth;
	private DatabaseReference databaseReference;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);

		// Initialize Firebase services
		FirebaseApp.initializeApp(this);
		mAuth = FirebaseAuth.getInstance();
		databaseReference = FirebaseDatabase.getInstance().getReference();
		firestore = FirebaseFirestore.getInstance();

		// Initialize views
		initializeViews();

		// Set up RecyclerView
		setUpRecyclerView();

		// Fetch user details and admin data
		fetchUserDetails();
		fetchAdminData();
	}

	private void initializeViews() {
		recyclerView = findViewById(R.id.recyclerview1);
		progressBar = findViewById(R.id.progressBar);
		fullNameTextView = findViewById(R.id.textView4);
		rankTextView = findViewById(R.id.textView);
		svcNumberTextView = findViewById(R.id.textView5);
		tradeTextView = findViewById(R.id.textView6);
		formationTextView = findViewById(R.id.textView7);
		profileImageView = findViewById(R.id.imageView3);
	}

	private void setUpRecyclerView() {
		recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		adminAdapter = new AdminAdapter(this, new ArrayList<>());
		recyclerView.setAdapter(adminAdapter);
	}

	private void fetchUserDetails() {
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser == null) {
			showToast("User not logged in!");
			return;
		}

		String uid = currentUser.getUid();
		DatabaseReference userRef = databaseReference.child("Users").child(uid);

		userRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (!snapshot.exists()) {
					showToast("User not found!");
					return;
				}

				String firstName = snapshot.child("firstName").getValue(String.class);
				String surname = snapshot.child("surname").getValue(String.class);
				String rank = snapshot.child("rank").getValue(String.class);
				String svcNumber = snapshot.child("svcNumber").getValue(String.class);
				String trade = snapshot.child("trade").getValue(String.class);
				String formation = snapshot.child("formation").getValue(String.class);
				String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

				// Update UI
				fullNameTextView.setText(String.format("%s %s", firstName, surname));
				rankTextView.setText(rank != null ? rank : "N/A");
				svcNumberTextView.setText(svcNumber != null ? svcNumber : "N/A");
				tradeTextView.setText(trade != null ? trade : "N/A");
				formationTextView.setText(formation != null ? formation : "N/A");

				// Load profile image with enhanced Glide configurations
				if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
					Glide.with(AdminActivity.this)
							.load(profileImageUrl)
							.placeholder(R.drawable.file_image_o_2)
							.error(R.drawable.roundel_of_zambia_svg) // Fallback image
							.diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized
							.into(profileImageView);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Log.e(TAG, "Database error", error.toException());
				showToast("Failed to load user details.");
			}
		});
	}

	private void fetchAdminData() {
		progressBar.setVisibility(View.VISIBLE);
		CollectionReference adminCollection = firestore.collection("menus/Admin/admin_act");
		adminCollection.get()
				.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
					@Override
					public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
						List<AdminItem> adminItems = new ArrayList<>();
						for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
							String name = document.getString("name");
							String iconUrl = document.getString("iconUrl");

							if (name != null && iconUrl != null) {
								adminItems.add(new AdminItem(name, iconUrl));
							} else {
								Log.w(TAG, "Missing fields in document: " + document.getId());
							}
						}

						adminAdapter.setItems(adminItems);
						progressBar.setVisibility(View.GONE);

						if (adminItems.isEmpty()) {
							showToast("No admin items found.");
						}
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						progressBar.setVisibility(View.GONE);
						Log.e(TAG, "Error fetching admin items", e);
						showToast("Failed to load admin items.");
					}
				});
	}

	private void showToast(String message) {
		Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
	}
}
