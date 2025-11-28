package com.example.prueba.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.prueba.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // Aquí tus medicamentos o datos
    String[] datos = {
            "Ibuprofeno",
            "Paracetamol",
            "Omeprazol",
            "Amoxicilina",
            "Aspirina",
            "Enantyum",
            "Nolotil",
            "Metamizol"
    };

    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, datos);

        binding.listView.setAdapter(adapter);

        // Barra de búsqueda
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });

        return root;
    }

    private void filterList(String text) {
        if (text == null || text.trim().isEmpty()) {
            // No mostrar nada si el usuario no escribe
            binding.listView.setVisibility(View.GONE);
        } else {
            // Sí mostrar resultados
            binding.listView.setVisibility(View.VISIBLE);
            adapter.getFilter().filter(text);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
