package com.devnet.myeportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

	private EditText emailEditText, passwordEditText;
	private Button loginButton;
	private TextView registerTextView;
	private ProgressBar loginProgressBar;
	private FirebaseAuth mAuth;

	@Override
	public void onStart() {
		super.onStart();
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mAuth = FirebaseAuth.getInstance();

		emailEditText = findViewById(R.id.emailEditText);
		passwordEditText = findViewById(R.id.passwordEditText);
		loginButton = findViewById(R.id.btn_login);
		registerTextView = findViewById(R.id.registerTextView);
		loginProgressBar = findViewById(R.id.progressBar);

		loginButton.setOnClickListener(v -> loginUser());
		registerTextView.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
	}

	private void loginUser() {
		String email = emailEditText.getText().toString().trim();
		String password = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
			Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
			return;
		}

		showLoading();

		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
					}
					hideLoading();
				});
	}

	private void showLoading() {
		loginProgressBar.setVisibility(View.VISIBLE);
		loginButton.setEnabled(false); // Disable button to prevent multiple clicks
	}

	private void hideLoading() {
		loginProgressBar.setVisibility(View.GONE);
		loginButton.setEnabled(true); // Re-enable button
	}
}
