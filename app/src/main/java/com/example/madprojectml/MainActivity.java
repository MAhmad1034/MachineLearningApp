package com.example.madprojectml;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    FragmentManager manager;
    private FirebaseAuth mAuth;
    Fragment loginFrag, signupFrag;
    View LoginFragView, SignupFragView;
    TextView tvLogin, tvSignup;
    TextInputEditText etEmailS, etPassS, etRePassS, etEmailL, etPassL;
    AppCompatButton btnSignup, btnLogin, btnCancelS, btnCancelL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.beginTransaction().hide(loginFrag).show(signupFrag).commit();
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.beginTransaction().hide(signupFrag).show(loginFrag).commit();
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailS.getText().toString().trim();
                String password = etPassS.getText().toString();
                String cPassword = etRePassS.getText().toString();

                if (email.isEmpty() || password.isEmpty() || cPassword.isEmpty()) {
                    if (email.isEmpty()) {
                        etEmailS.setError("Email is required");
                    }
                    if (password.isEmpty()) {
                        etPassS.setError("Password is required");
                    }
                    if (cPassword.isEmpty()) {
                        etRePassS.setError("Confirm Password is required");
                    }
                    Toast.makeText(MainActivity.this, "Something is missing", Toast.LENGTH_SHORT).show();
                } else {
                    if (password.equals(cPassword)) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);
                                        startActivity(new Intent(MainActivity.this, Home.class));
                                        finish();
                                    } else {
                                        // If sign-in fails, display a message to the user.
                                        String errorMessage = getFirebaseAuthErrorMessage(task.getException());
                                        Toast.makeText(MainActivity.this, "Authentication failed: " + errorMessage,
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }
                                });
                    } else {
                        etRePassS.setError("Passwords do not match");
                        Toast.makeText(MainActivity.this, "Password mismatched", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailL.getText().toString().trim();
                String password = etPassL.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    if (email.isEmpty()) {
                        etEmailL.setError("Email is required");
                    }
                    if (password.isEmpty()) {
                        etPassL.setError("Password is required");
                    }
                    Toast.makeText(MainActivity.this, "Something is missing", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                    startActivity(new Intent(MainActivity.this, Home.class));
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    String errorMessage = getFirebaseAuthErrorMessage(task.getException());
                                    Toast.makeText(MainActivity.this, "Authentication failed: " + errorMessage,
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            });
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        // Navigate to home screen or update the UI with user info
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        manager = getSupportFragmentManager();
        loginFrag = manager.findFragmentById(R.id.fragLogin);
        signupFrag = manager.findFragmentById(R.id.fragSignUp);
        LoginFragView = loginFrag.getView();
        SignupFragView = signupFrag.getView();

        tvSignup = LoginFragView.findViewById(R.id.tvSignUp);
        etEmailL = LoginFragView.findViewById(R.id.etEmail);
        etPassL = LoginFragView.findViewById(R.id.etPassword);
        btnCancelL = LoginFragView.findViewById(R.id.btnCancel);
        btnLogin = LoginFragView.findViewById(R.id.btnLogin);

        tvLogin = SignupFragView.findViewById(R.id.tvLogin);
        etEmailS = SignupFragView.findViewById(R.id.etEmail);
        etPassS = SignupFragView.findViewById(R.id.etPassword);
        etRePassS = SignupFragView.findViewById(R.id.etRePassword);
        btnCancelS = SignupFragView.findViewById(R.id.btnCancel);
        btnSignup = SignupFragView.findViewById(R.id.btnSignup);

        manager.beginTransaction().show(loginFrag).hide(signupFrag).commit();
    }

    private String getFirebaseAuthErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            return ((FirebaseAuthException) exception).getErrorCode();
        } else {
            return exception != null ? exception.getMessage() : "Unknown error occurred";
        }
    }
}
