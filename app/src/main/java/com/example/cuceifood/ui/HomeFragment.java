package com.example.cuceifood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.cuceifood.LocalDetailActivity;
import com.example.cuceifood.LocalesAdapter;
import com.example.cuceifood.R;
import com.example.cuceifood.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private EditText searchBar;
    private Button searchButton, filterButton;
    private RecyclerView recyclerView;
    private LocalesAdapter adapter;

    private String selectedFoodType = "";
    private int selectedPriceRange = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchBar = view.findViewById(R.id.searchBar);
        searchButton = view.findViewById(R.id.searchButton);
        filterButton = view.findViewById(R.id.filterButton);
        recyclerView = view.findViewById(R.id.recyclerViewLocales);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocalesAdapter(new ArrayList<>(), local -> {
            // Navegación a detalles del local
            Intent intent = new Intent(getContext(), LocalDetailActivity.class);
            intent.putExtra("localId", local.get("objectId").toString());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> performSearch());
        filterButton.setOnClickListener(v -> showFiltersDialog());

        // Cargar todos los locales al iniciar
        performSearch();

        return view;
    }

    private void performSearch() {
        String dishName = searchBar.getText().toString().trim();

        StringBuilder whereClause = new StringBuilder();

        if (!dishName.isEmpty()) {
            // Nueva consulta para buscar en menús relacionados
            whereClause.append("id in (SELECT local_id FROM menus WHERE nombre_platillo LIKE '%")
                    .append(dishName)
                    .append("%')");
        }

        if (!selectedFoodType.isEmpty() && !selectedFoodType.equals("Todos")) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("tipo_comida = '").append(selectedFoodType).append("'");
        }

        if (selectedPriceRange != -1) {
            if (whereClause.length() > 0) whereClause.append(" AND ");
            whereClause.append("rango_precios = ").append(selectedPriceRange);
        }

        searchLocales(whereClause.toString());
    }

    private void searchLocales(String whereClause) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause.isEmpty() ? null : whereClause);
        queryBuilder.setPageSize(100);

        Backendless.Data.of("locales").find(queryBuilder,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> locales) {
                        if (locales.isEmpty()) {
                            Toast.makeText(getContext(), "No se encontraron locales", Toast.LENGTH_SHORT).show();
                        }

                        // Convertimos List<Map> a List<Map<String, Object>> para el adapter
                        List<Map<String, Object>> typedLocales = new ArrayList<>();
                        for (Map map : locales) {
                            typedLocales.add((Map<String, Object>) map);
                        }

                        // Si estamos buscando por nombre de platillo, necesitamos verificar los menús
                        if (!searchBar.getText().toString().trim().isEmpty()) {
                            verifyMenusForSearch(typedLocales);
                        } else {
                            adapter.updateData(typedLocales);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getContext(), "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyMenusForSearch(List<Map<String, Object>> locales) {
        List<Map<String, Object>> filteredLocales = new ArrayList<>();
        List<String> menuIdsToSearch = new ArrayList<>();

        // Primero recolectamos todos los IDs de menús a buscar
        for (Map<String, Object> local : locales) {
            try {
                String menuJson = (String) local.get("menu");
                if (menuJson != null && !menuJson.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(menuJson);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        menuIdsToSearch.add(jsonArray.getString(i));
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON_ERROR", "Error parsing menu JSON", e);
            }
        }

        if (menuIdsToSearch.isEmpty()) {
            adapter.updateData(filteredLocales);
            return;
        }

        // Construimos la cláusula WHERE para buscar en menús
        String whereClause = "id in ('" + TextUtils.join("','", menuIdsToSearch) + "') AND " +
                "nombre_platillo LIKE '%" + searchBar.getText().toString().trim() + "%'";

        DataQueryBuilder menuQuery = DataQueryBuilder.create();
        menuQuery.setWhereClause(whereClause);
        menuQuery.setPageSize(100);

        Backendless.Data.of("menus").find(menuQuery, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> matchingMenus) {
                // Creamos un set de IDs de locales que tienen menús que coinciden
                Set<String> matchingLocalIds = new HashSet<>();
                for (Map menu : matchingMenus) {
                    // Aquí necesitarías una forma de relacionar el menú con el local
                    // Esto depende de cómo esté estructurada tu base de datos
                    // Puedes necesitar una tabla de relación o incluir local_id en menus
                }

                // Filtramos los locales originales
                for (Map<String, Object> local : locales) {
                    try {
                        String menuJson = (String) local.get("menu");
                        if (menuJson != null && !menuJson.isEmpty()) {
                            JSONArray jsonArray = new JSONArray(menuJson);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String menuId = jsonArray.getString(i);
                                for (Map menu : matchingMenus) {
                                    if (menu.get("id").toString().equals(menuId)) {
                                        filteredLocales.add(local);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error parsing menu JSON", e);
                    }
                }

                adapter.updateData(filteredLocales);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), "Error buscando menús: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.updateData(new ArrayList<>());
            }
        });
    }
    private void showFiltersDialog() {
        try {
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            View dialogView = inflater.inflate(R.layout.dialog_filters, null);

            Spinner spinnerFoodType = dialogView.findViewById(R.id.spinnerFoodType);
            RadioGroup radioPriceRange = dialogView.findViewById(R.id.radioPriceRange);

            // Configurar valores actuales
            if (!selectedFoodType.isEmpty()) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerFoodType.getAdapter();
                int position = adapter.getPosition(selectedFoodType);
                spinnerFoodType.setSelection(position);
            }

            if (selectedPriceRange != -1) {
                int radioId = selectedPriceRange == 1 ? R.id.radioEconomico :
                        selectedPriceRange == 2 ? R.id.radioRegular : R.id.radioCaro;
                radioPriceRange.check(radioId);
            }

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Filtros de búsqueda")
                    .setView(dialogView)
                    .setPositiveButton("Aplicar", (dialogInterface, which) -> {
                        selectedFoodType = spinnerFoodType.getSelectedItem().toString();

                        int checkedId = radioPriceRange.getCheckedRadioButtonId();
                        if (checkedId != -1) {
                            selectedPriceRange = checkedId == R.id.radioEconomico ? 1 :
                                    checkedId == R.id.radioRegular ? 2 : 3;
                        } else {
                            selectedPriceRange = -1;
                        }
                        performSearch();
                    })
                    .setNegativeButton("Cancelar", null)
                    .setNeutralButton("Limpiar", (dialogInterface, which) -> {
                        selectedFoodType = "";
                        selectedPriceRange = -1;
                        spinnerFoodType.setSelection(0);
                        radioPriceRange.clearCheck();
                        performSearch();
                    })
                    .create();

            dialog.show();

        } catch (Exception e) {
            Log.e("DialogError", "Error al mostrar diálogo", e);
            Toast.makeText(getContext(), "Error al cargar filtros", Toast.LENGTH_SHORT).show();
        }
    }
}