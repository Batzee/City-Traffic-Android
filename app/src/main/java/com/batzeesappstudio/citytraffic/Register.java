package com.batzeesappstudio.citytraffic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPsWordField;
    private Button signupButton;

    private String userName;
    private String userEmail;
    private String userPassword;
    private View mainView;
    private View mProgressView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainView = findViewById(R.id.signupcoordinate);
        mProgressView = findViewById(R.id.progressBarSignUp);
        mProgressView.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("Login", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("Login", "onAuthStateChanged:signed_out");
                }
            }
        };

        nameField = (EditText) findViewById(R.id.editTextName);
        emailField = (EditText) findViewById(R.id.editTextEmail);
        passwordField = (EditText) findViewById(R.id.editTextPassword);
        confirmPsWordField = (EditText) findViewById(R.id.editTextConfirmPassword);
        signupButton = (Button) findViewById(R.id.buttonSignUp);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isConnectingToInternet(Register.this)) {
                    if (passwordField.getText().toString().equals("") || confirmPsWordField.getText().toString().equals("") || nameField.getText().toString().equals("") || emailField.getText().toString().equals("")) {
                        Snackbar.make(mainView, "Please fill all the fields", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (checkPassword(passwordField.getText().toString(), confirmPsWordField.getText().toString())) {
                            userName = nameField.getText().toString();
                            userEmail = emailField.getText().toString();
                            userPassword = passwordField.getText().toString();

                            createAccount(userEmail, userPassword);

                        } else {
                            Snackbar.make(mainView, "Passwords does not match", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
                else {
                    Snackbar.make(mainView, "No Internet Connectivity", Snackbar.LENGTH_SHORT).setAction("Settings", new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            turnOnInternet();
                        }
                    }).show();
                }
            }
        });
    }

    private void createAccount(final String email, String password) {
        Log.d("Register", "createAccount:" + email);

        showProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Snackbar.make(mainView, "This email is already available, forgot Password?", Snackbar.LENGTH_LONG).setAction("Reset Password", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mAuth.sendPasswordResetEmail(email);
                                    Toast.makeText(Register.this,"A Password Reset mail is sent to your email, please check",Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                        }
                        else {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nameField.getText().toString())
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                finish();
                                                Log.d("Registration", "User profile updated.");
                                            }
                                            else
                                            Log.d("Registration", "User profile update failed.");
                                        }
                                    });
                        }
                        showProgress(false);
                    }
                });
    }

    private boolean checkPassword(String password, String passwrdConfirmation){
        if(password.equals(passwrdConfirmation)){
            return true;
        }
        else{
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public static boolean isConnectingToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void turnOnInternet(){
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        startActivityForResult(settingsIntent, 9003);
    }
}
