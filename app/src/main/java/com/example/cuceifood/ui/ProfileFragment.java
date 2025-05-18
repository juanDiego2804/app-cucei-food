package com.example.cuceifood.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.cuceifood.R;
import com.example.cuceifood.LoginActivity;

import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private TextView userName, userEmail;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar vistas
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Cargar datos del usuario
        loadUserData();

        // Configurar el botón de cerrar sesión
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }
    private void loadUserData() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cargando datos...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Obtener el email del usuario actual desde las preferencias de login
        String currentEmail = Backendless.UserService.CurrentUser().getEmail();

        if (currentEmail != null && !currentEmail.isEmpty()) {
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause("correo = '" + currentEmail + "'");

            Backendless.Data.of("Usuarios").find(queryBuilder, new AsyncCallback<List<Map>>() {
                @Override
                public void handleResponse(List<Map> response) {
                    progressDialog.dismiss();
                    if (response != null && !response.isEmpty()) {
                        Map userData = response.get(0);
                        updateUI(
                                (String) userData.get("nombre"),
                                (String) userData.get("correo")
                        );
                    } else {
                        showError("Datos de usuario no encontrados");
                    }
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    progressDialog.dismiss();
                    showError("Error al cargar datos: " + fault.getMessage());
                }
            });
        } else {
            progressDialog.dismiss();
            showError("No se pudo obtener el email del usuario");
        }
    }

    private void updateUI(String name, String email) {
        requireActivity().runOnUiThread(() -> {
            userName.setText(name != null ? name : "Nombre no disponible");
            userEmail.setText(email != null ? email : "Correo no disponible");
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void logoutUser() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Cerrando sesión...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error al cerrar sesión: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        // Limpiar el back stack para evitar volver atrás
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}