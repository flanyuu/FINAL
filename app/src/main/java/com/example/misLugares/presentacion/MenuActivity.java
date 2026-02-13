package com.example.misLugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.misLugares.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnMisLugares = findViewById(R.id.btnMisLugares);
        btnMisLugares.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button btnRutas = findViewById(R.id.btnRutas);
        btnRutas.setOnClickListener(v -> {
            Intent intent = new Intent(this, RutasActivity.class);
            startActivity(intent);
        });
    }
}
