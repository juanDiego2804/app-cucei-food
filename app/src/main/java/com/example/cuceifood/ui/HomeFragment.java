package com.example.cuceifood.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cuceifood.R;
import com.example.cuceifood.databinding.FragmentHomeBinding;
public class HomeFragment extends Fragment {
    private EditText searchBar;
    private Button searchButton, filterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar componentes
        searchBar = view.findViewById(R.id.searchBar);
        searchButton = view.findViewById(R.id.searchButton);
        filterButton = view.findViewById(R.id.filterButton);

        // Configurar listeners
        searchButton.setOnClickListener(v -> performSearch());
        filterButton.setOnClickListener(v -> showFiltersDialog());

        return view;
    }

    private void performSearch() {
        String query = searchBar.getText().toString();
        // Lógica de búsqueda (implementarás esto más adelante)
        Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
    }

    private void showFiltersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filtros de búsqueda")
                //.setView(R.layout.dialog_filters) // Crearás este layout después
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    // Lógica para aplicar filtros
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}