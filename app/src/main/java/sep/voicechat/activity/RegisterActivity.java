package sep.voicechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import sep.voicechat.R;
import sep.voicechat.activity.channel.ChannelActivity;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth firebaseAuth;

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordRepeat;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonRegister = findViewById(R.id.buttonRegister);
        editTextEmail = findViewById(R.id.registerEditTextEmail);
        editTextPassword = findViewById(R.id.registerEditTextPassword);
        editTextPasswordRepeat = findViewById(R.id.registerEditTextPasswordRepeat);
        progressDialog = new ProgressDialog(this);

        buttonRegister.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegister) {
            register();
        }
    }

    private void register() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repeatPassword = editTextPasswordRepeat.getText().toString().trim();

        if (!password.equals(repeatPassword)) { //If the passwords don't match
            Toast.makeText(this, "The passwords dont match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.hide();
                    Log.i("Registration response", "Account successfully registered.");
                    Toast.makeText(RegisterActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), ChannelActivity.class));

                } else {
                    progressDialog.hide();
                    Log.i("Registration response", "Failed to register: " + task.getException().getMessage());
                    Toast.makeText(RegisterActivity.this, "Failed to register: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
