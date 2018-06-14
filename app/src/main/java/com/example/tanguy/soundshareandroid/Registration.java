package com.example.tanguy.soundshareandroid;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registration extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
    }

    public void backHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void sendToConnection(View view){
        Intent intent = new Intent(this, Connection.class);
        startActivity(intent);
    }

    public void signIn(View view){
        EditText editEmail = (EditText) findViewById(R.id.editText6);
        EditText editPassword = (EditText) findViewById(R.id.editText7);
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        final View v = view;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.e("Merider", "quel est e task ?", task.getException());
                        if (task.isSuccessful()){
                            Snackbar.make(v, "Inscription reussie", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        } else {
                            Snackbar.make(v, "Inscription ratée", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }

        });
    }
}
