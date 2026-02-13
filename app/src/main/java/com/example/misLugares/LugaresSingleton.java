package com.example.misLugares;

import android.content.Context;

import com.example.misLugares.datos.LugaresLista;

public class LugaresSingleton {
    private static LugaresSingleton INSTANCIA = new LugaresSingleton();
    private LugaresLista lugares;
    private LugaresSingleton() {
        lugares = new LugaresLista();
    }
    public static LugaresSingleton getInstance() {
        return INSTANCIA;
    }

    public void inicializa(Context contexto) {
        lugares = new LugaresLista();
    }

    public LugaresLista getLugares() {
        return lugares;
    }

    public void setLugares(LugaresLista lugares) {
        this.lugares = lugares;
    }
}
