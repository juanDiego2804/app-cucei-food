package com.example.cuceifood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements TextWatcher {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private ProgressDialog progressDialog;
    private TextInputLayout tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);

        // Agregar listeners para validar campos en tiempo real
        etUsername.addTextChangedListener(this);
        etEmail.addTextChangedListener(this);
        etPassword.addTextChangedListener(this);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        if (!validateFields()) return;

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        progressDialog.show();

        // Verificar si el correo ya existe
        checkEmailExists(email, exists -> {
            if (exists) {
                progressDialog.dismiss();
                etEmail.setError("Este correo ya está registrado");
            } else {
                // Crear el usuario en la tabla Usuarios
                Map<String, Object> user = new HashMap<>();
                user.put("nombre", username);
                user.put("correo", email);
                user.put("contrasenia", password);

                Backendless.Data.of("Usuarios").save(user, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map response) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Método para verificar si el correo ya existe
    private void checkEmailExists(String email, EmailCheckCallback callback) {
        String whereClause = "correo = '" + email + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("Usuarios").find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                callback.onResult(!response.isEmpty());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error verificando correo: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                callback.onResult(true); // Asumir que existe para evitar registros duplicados
            }
        });
    }

    // Interface para el callback de verificación de email
    interface EmailCheckCallback {
        void onResult(boolean exists);
    }

    private boolean validateFields() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;

        if (username.isEmpty()) {
            etUsername.setError("El nombre es requerido");
            isValid = false;
        } else if (username.length() < 3) {
            etUsername.setError("El nombre debe tener al menos 3 caracteres");
            isValid = false;
        }

        if (email.isEmpty()) {
            etEmail.setError("El correo es requerido");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            isValid = false;
        }

        if (password.isEmpty()) {
            etPassword.setError("La contraseña es requerida");
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            etPassword.setError("La contraseña debe contener al menos una mayúscula");
            isValid = false;
        } else if (!password.matches(".*[0-9].*")) {
            etPassword.setError("La contraseña debe contener al menos un número");
            isValid = false;
        }

        return isValid;
    }

    // Métodos de TextWatcher para habilitar/deshabilitar el botón
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        boolean allFieldsFilled = !etUsername.getText().toString().isEmpty() &&
                !etEmail.getText().toString().isEmpty() &&
                !etPassword.getText().toString().isEmpty();
        btnRegister.setEnabled(allFieldsFilled);
    }
}