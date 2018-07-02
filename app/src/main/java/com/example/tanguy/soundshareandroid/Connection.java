package com.example.tanguy.soundshareandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Class for signing in the application
 */
public class Connection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * For returning to main page
     * @param view Current view
     */
    public void backHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * For going to registration page
     * @param view Current view
     */
    public void sendToRegistration(View view){
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    /**
     * For signing in the application
     * @param view Current view
     */
    public void signIn(View view) {
        // Getting the connection information
        EditText editEmail = findViewById(R.id.editText6);
        EditText editPassword = findViewById(R.id.editText7);
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // Intent for going to the user's home page
        final Intent intent = new Intent(this, Home.class);
        final View currentView = view;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(email.length() > 0 && password.length() > 0){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    // Notify user of completion
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("CONNECTION", "Logged in successfully");
                                Snackbar.make(currentView, "Your inscription succeeded", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                startActivity(intent);
                            } else {
                                Log.w("CONNECTION", task.getException());
                                Snackbar.make(currentView, "Try again ...", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
        }
    }
}