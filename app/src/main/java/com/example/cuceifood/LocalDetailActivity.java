package com.example.cuceifood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import java.util.Map;

public class LocalDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detail);

        // Botón de regreso
        Button btnBack = findViewById(R.id.btnBack);
        ((android.view.View) btnBack).setOnClickListener(v -> {
            // Regresa a MainActivity y limpia la pila de actividades
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Cierra esta actividad
        });

        // Cargar detalles del local
        String localId = getIntent().getStringExtra("localId");
        if (localId != null) {
            loadLocalDetails(localId);
        } else {
            Toast.makeText(this, "Error: ID no válido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadLocalDetails(String localId) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setRelated("menu");

        Backendless.Data.of("locales").findById(localId, queryBuilder, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map local) {
                try {
                    displayLocalData(local);

                    if (local.get("menu") != null) {
                        Map menu = (Map) local.get("menu");
                        TextView textMenu = findViewById(R.id.textMenu);
                        textMenu.setText(
                                "Menú: " + menu.get("nombre_platillo") + "\n" +
                                        "Precio: $" + menu.get("precio")
                        );
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error al mostrar datos: " + e.getMessage());
                    Toast.makeText(LocalDetailActivity.this, "Error al cargar detalles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("BACKENDLESS", "Error: " + fault.getMessage());
                Toast.makeText(LocalDetailActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayLocalData(Map local) {
        try {
            TextView textNombre = findViewById(R.id.textNombre);
            TextView textTipoComida = findViewById(R.id.textTipoComida);
            TextView textRangoPrecios = findViewById(R.id.textRangoPrecios);
            TextView textHorario = findViewById(R.id.textHorario);
            TextView textUbicacion = findViewById(R.id.textUbicacion);

            // Asegúrate de que los campos no sean nulos
            textNombre.setText(local.get("nombre") != null ? local.get("nombre").toString() : "No disponible");
            textTipoComida.setText("Tipo: " + (local.get("tipo_comida") != null ? local.get("tipo_comida").toString() : "No especificado"));

            int rango = local.get("rango_precios") != null ? Integer.parseInt(local.get("rango_precios").toString()) : 0;
            String rangoTexto = rango == 1 ? "Económico" : rango == 2 ? "Regular" : "Caro";
            textRangoPrecios.setText("Precio: " + rangoTexto);

            textHorario.setText("Horario: " + (local.get("horario") != null ? local.get("horario").toString() : "No definido"));
            textUbicacion.setText("Ubicación: " + (local.get("ubicacion") != null ? local.get("ubicacion").toString() : "No disponible"));
        } catch (Exception e) {
            Log.e("DEBUG", "Error al mostrar datos: " + e.getMessage());
        }
    }
}

