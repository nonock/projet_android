package com.example.alexi.b_ubble;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextUsername, editTextPassword, editTextEmail;
    ProgressBar progBar;

    private FirebaseAuth mAuth;
    private DatabaseReference myDb;
    private final ArrayList<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        progBar = (ProgressBar) findViewById(R.id.pBar);

        mAuth = FirebaseAuth.getInstance();

        progBar.setVisibility(View.GONE);

        findViewById(R.id.RegisterButton).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
        myDb = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email est obligatoire !");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email non valide");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Mot de passe est obligatoire !");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Minimum de 6 caractères pour le mot de passe");
            editTextPassword.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            editTextUsername.setError("Nom d'utilisateur est obligatoire !");
            editTextUsername.requestFocus();
            return;
        }

        progBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final DatabaseReference newUser = myDb.push();
                    //We store in the Firebase Database a user's ID and his username
                    String user_id = mAuth.getCurrentUser().getUid();
                    newUser.child("id").setValue(user_id);
                    newUser.child("name").setValue(username);
                    newUser.child("mail").setValue(email);

                    myDb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.child("name").getValue() != null) {
                                    list.add(ds.child("name").getValue().toString());
                                    list.remove(username);

                                    Intent intent = new Intent(RegisterActivity.this, sea.class);
                                    intent.putStringArrayListExtra("list_user", list);
                                    intent.putExtra("usernameR",username);
                                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    progBar.setVisibility(View.GONE);
                                    startActivity(intent);


                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    Toast.makeText(getApplicationContext(), "Inscription réussie", Toast.LENGTH_SHORT).show();

                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "Compte déjà enregistré", Toast.LENGTH_SHORT).show();
                        progBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progBar.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.RegisterButton:

                registerUser();

                break;

            case R.id.textViewLogin:

                startActivity(new Intent(this, MainActivity.class));

                break;

        }
    }
}
