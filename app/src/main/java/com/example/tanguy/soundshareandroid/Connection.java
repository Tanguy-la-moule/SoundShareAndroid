package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        final View v = view;
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Snackbar.make(v, "Connection reussie", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            String merdier = "merdier";
                            Snackbar.make(v, "Connection ratée", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
                        }

                        // ...
                    }
                });
    }

}

//Syntaxe snackbar
//Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//        .setAction("Action", null).show();