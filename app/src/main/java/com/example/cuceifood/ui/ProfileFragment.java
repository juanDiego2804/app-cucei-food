package com.example.cuceifood.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cuceifood.R;
import com.example.cuceifood.databinding.FragmentProfileBinding;
public class ProfileFragment extends Fragment {
    private TextView userName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userName = view.findViewById(R.id.userName);


        // Configurar datos del usuario (implementarás después con Firebase Auth)
        // userName.setText(...);
        // Glide.with(this).load(...).into(userPhoto);

        return view;
    }
}