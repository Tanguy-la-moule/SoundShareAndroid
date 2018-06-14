package com.example.tanguy.soundshareandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Connection extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        mAuth = FirebaseAuth.getInstance();
    }

  /*  @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }*/

    public void backHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void sendToRegistration(View view){
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

    public void signUp(View view) {
        EditText editEmail = (EditText) findViewById(R.id.editText6);
        EditText editPassword = (EditText) findViewById(R.id.editText7);
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        final Intent intent = new Intent(this, Home.class);
        final View currentView = view;

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(email.length() > 0 && password.length() > 0){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("CONNECTION", "logged in successfully");
                                Snackbar.make(currentView, "Your inscription succeded", Snackbar.LENGTH_LONG)
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