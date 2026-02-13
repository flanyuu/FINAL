package com.example.misLugares;

import android.app.Application;

import androidx.preference.PreferenceManager;

import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.presentacion.AdaptadorLugaresBD;

import org.osmdroid.config.Configuration;

public class Aplicacion extends Application {

    public LugaresBDAdapter lugares;
    public AdaptadorLugaresBD adaptador;
    public GeoPunto posicionActual = new GeoPunto(0.0, 0.0);

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        lugares = new LugaresBDAdapter(this);
        adaptador = lugares.getAdaptador();
        lugares.setAdaptador(adaptador);
    }
}
