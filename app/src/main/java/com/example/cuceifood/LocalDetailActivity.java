package com.example.cuceifood;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocalDetailActivity extends AppCompatActivity {

    private TableLayout tableMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detail);

        // Inicializar vistas
        tableMenu = findViewById(R.id.tableMenu);

        // Configurar botones
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());




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
        queryBuilder.setPageSize(100);

        Backendless.Data.of("locales").findById(localId, queryBuilder, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map local) {
                try {
                    displayLocalData(local);

                    // Obtener los IDs del menú como string JSON
                    String menuIdsJson = (String) local.get("menu");
                    if (menuIdsJson != null && !menuIdsJson.isEmpty()) {
                        try {
                            JSONArray jsonArray = new JSONArray(menuIdsJson);
                            List<Integer> menuIds = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                menuIds.add(jsonArray.getInt(i));
                            }
                            loadMenuItems(menuIds);
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing menu IDs", e);
                            showNoMenuAvailable();
                        }
                    } else {
                        showNoMenuAvailable();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Error al mostrar datos", e);
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

            // Nombre del local
            textNombre.setText(local.get("nombre") != null ? local.get("nombre").toString() : "No disponible");

            // Ubicación (solo texto, el ícono ya está en el XML)
            textUbicacion.setText(local.get("ubicacion") != null ? local.get("ubicacion").toString() : "No disponible");

            // Horario
            textHorario.setText(local.get("horario") != null ? local.get("horario").toString() : "No definido");

            // Rango de precios
            int rango = local.get("rango_precios") != null ? Integer.parseInt(local.get("rango_precios").toString()) : 0;
            String rangoTexto = getRangoPreciosTexto(rango);
            textRangoPrecios.setText(rangoTexto);

            // Tipo de comida
            textTipoComida.setText(local.get("tipo_comida") != null ? local.get("tipo_comida").toString() : "No especificado");

        } catch (Exception e) {
            Log.e("DEBUG", "Error al mostrar datos: " + e.getMessage());
        }
    }

    private String getRangoPreciosTexto(int rango) {
        switch (rango) {
            case 1: return "Económico";
            case 2: return "Regular";
            case 3: return "Caro";
            default: return "No especificado";
        }
    }

    //nuevo
    private void loadMenuItems(List<Integer> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            showNoMenuAvailable();
            return;
        }

        // Construir cláusula WHERE para obtener los menús específicos
        StringBuilder whereClause = new StringBuilder("id in (");
        for (int i = 0; i < menuIds.size(); i++) {
            whereClause.append(menuIds.get(i));
            if (i < menuIds.size() - 1) {
                whereClause.append(",");
            }
        }
        whereClause.append(")");

        DataQueryBuilder menuQuery = DataQueryBuilder.create();
        menuQuery.setWhereClause(whereClause.toString());
        menuQuery.setPageSize(100);

        Backendless.Data.of("menus").find(menuQuery, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> menuItems) {
                if (menuItems != null && !menuItems.isEmpty()) {
                    displayMenuItems(menuItems);
                } else {
                    showNoMenuAvailable();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("MENU_ERROR", "Error loading menu: " + fault.getMessage());
                showNoMenuAvailable();
            }
        });
    }
    private void showNoMenuAvailable() {
        runOnUiThread(() -> {
            TextView textMenuTitle = findViewById(R.id.textMenuTitle);
            textMenuTitle.setText("Menú no disponible");
            tableMenu.setVisibility(View.GONE);
        });
    }
    private void displayMenuItems(List<Map> menuItems) {
        runOnUiThread(() -> {
            // Limpiar tabla (excepto la fila de encabezado)
            int childCount = tableMenu.getChildCount();
            if (childCount > 1) {
                tableMenu.removeViews(1, childCount - 1);
            }

            // Mostrar título del menú
            TextView textMenuTitle = findViewById(R.id.textMenuTitle);
            textMenuTitle.setText("Menú");
            tableMenu.setVisibility(View.VISIBLE);

            // Ordenar por ID para mantener el orden original
            Collections.sort(menuItems, (o1, o2) -> {
                Integer id1 = (Integer) o1.get("id");
                Integer id2 = (Integer) o2.get("id");
                return id1.compareTo(id2);
            });

            // Agregar cada ítem del menú
            for (Map item : menuItems) {
                TableRow row = new TableRow(LocalDetailActivity.this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                // Nombre del platillo
                TextView tvName = new TextView(LocalDetailActivity.this);
                tvName.setLayoutParams(new TableRow.LayoutParams(
                        0, TableRow.LayoutParams.WRAP_CONTENT, 3));

                String platillo = item.get("nombre_platillo") != null ?
                        item.get("nombre_platillo").toString() : "Platillo no disponible";
                tvName.setText(platillo);
                tvName.setPadding(16, 12, 16, 12);
                row.addView(tvName);

                // Precio
                TextView tvPrice = new TextView(LocalDetailActivity.this);
                tvPrice.setLayoutParams(new TableRow.LayoutParams(
                        0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                tvPrice.setGravity(Gravity.END);

                double precio = 0;
                try {
                    Object precioObj = item.get("precio");
                    if (precioObj instanceof Number) {
                        precio = ((Number) precioObj).doubleValue();
                    } else if (precioObj != null) {
                        precio = Double.parseDouble(precioObj.toString());
                    }
                } catch (Exception e) {
                    Log.e("PRICE_ERROR", "Error parsing price", e);
                }

                tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", precio));
                tvPrice.setPadding(16, 12, 16, 12);
                row.addView(tvPrice);

                // Alternar colores de fondo
                if (tableMenu.getChildCount() % 2 == 1) {
                    row.setBackgroundColor(ContextCompat.getColor(LocalDetailActivity.this, R.color.light_gray));
                }

                tableMenu.addView(row);
            }
        });
    }
}