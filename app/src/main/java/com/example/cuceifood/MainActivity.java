package com.example.cuceifood;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cuceifood.databinding.ActivityMainBinding;
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Configura el color del ítem seleccionado
        //navView.setItemIconTintList(null); // Para usar los iconos con colores originales

        navView.setItemIconTintList(ContextCompat.getColorStateList(this, R.drawable.bottom_nav_item_color));
        navView.setItemTextColor(ContextCompat.getColorStateList(this, R.drawable.bottom_nav_item_color));
        navView.setOnNavigationItemSelectedListener(item -> {
            navController.navigate(item.getItemId());
            return true;
        });


    }
}