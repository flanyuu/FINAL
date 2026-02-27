package com.example.misLugares.presentacion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.CasosUsoLocalizacion;
import com.example.misLugares.R;
import com.example.misLugares.datos.GeneradorRutas;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.Ruta;
import com.example.misLugares.presentacion.AdaptadorRutasRecomendadas;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MenuPrincipalActivity extends AppCompatActivity {

    private TextView textHora;
    private TextView textSaludo;
    private TextView iconoHora;
    private View rootLayout;
    private Timer timer;
    private AdaptadorRutasRecomendadas adaptadorRutas;
    private List<Ruta> rutasRecomendadas = new ArrayList<>();
    private static final String PREFS_VALORACION = "valoracion_rutas";
    private static final String PREFS_AJUSTES = "ajustes";
    private static final String KEY_RANGO_RUTAS_KM = "rango_rutas_km";
    private static final double DEFAULT_RANGO_KM = 30.0;
    private Handler carruselHandler;
    private Runnable carruselAutoAdvance;
    private static final long CARRUSEL_INTERVAL_MS = 4500;
    private static final int SOLICITUD_PERMISO_LOCALIZACION = 101;
    private CasosUsoLocalizacion usoLocalizacion;

    private static final int[][] COLORES_CIELO = {
            {0xFF0A0E1A, 0xFF0D1525},
            {0xFF0A0E1A, 0xFF0D1525},
            {0xFF0A0E1A, 0xFF0D1525},
            {0xFF0C1020, 0xFF101828},
            {0xFF0E1428, 0xFF141E35},
            {0xFF1A1A3A, 0xFF2E2050},
            {0xFFE8936A, 0xFFB05A8A},
            {0xFFEDAA72, 0xFFD4956A},
            {0xFF87CEEB, 0xFF5BAED4},
            {0xFF72C8EF, 0xFF4AAEE8},
            {0xFF5ABDE8, 0xFF3A9ED8},
            {0xFF3FB0E8, 0xFF2090D0},
            {0xFF2FA8E0, 0xFF1880C0},
            {0xFF2FA8E0, 0xFF1880C0},
            {0xFF3AAAE0, 0xFF1E88C8},
            {0xFF5BB8E0, 0xFF3A9EC8},
            {0xFF6EC0DC, 0xFF4AACC4},
            {0xFFE8B080, 0xFFD08060},
            {0xFFE8986A, 0xFFCC7050},
            {0xFFCC6844, 0xFFB84A38},
            {0xFF5A3A6A, 0xFF3A2850},
            {0xFF1A2440, 0xFF0E1628},
            {0xFF0E1830, 0xFF0A1020},
            {0xFF0A0E1A, 0xFF080C18},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_principal);

        textHora   = findViewById(R.id.textHora);
        textSaludo = findViewById(R.id.textSaludo);
        iconoHora  = findViewById(R.id.iconoHora);
        rootLayout = findViewById(R.id.rootLayout);

        actualizarHoraYFondo();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                runOnUiThread(MenuPrincipalActivity.this::actualizarHoraYFondo);
            }
        }, 1000, 1000);

        animarEntrada();

        LinearLayout btnSenderismo = findViewById(R.id.btnSenderismo);
        btnSenderismo.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> {
                Intent i = new Intent(this, MapaActivity.class);
                i.putExtra("solo_inicios_sendero", true);
                startActivity(i);
            }, 130);
        });

        LinearLayout btnTrazarRutas = findViewById(R.id.btnTrazarRutas);
        btnTrazarRutas.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> {
                Intent i = new Intent(this, MapaActivity.class);
                i.putExtra("modo", "TRAZAR_RUTA");
                startActivity(i);
            }, 130);
        });

        LinearLayout btnListaRutas = findViewById(R.id.btnListaRutas);
        btnListaRutas.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() ->
                    startActivity(new Intent(this, MainActivity.class)), 130);
        });

        LinearLayout btnAjustes = findViewById(R.id.btnAjustes);
        btnAjustes.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(this::mostrarDialogoAjustesRango, 130);
        });

        LinearLayout btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            animarClick(v);
            v.postDelayed(() -> {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }, 130);
        });

        usoLocalizacion = new CasosUsoLocalizacion(this, SOLICITUD_PERMISO_LOCALIZACION);
        usoLocalizacion.ultimaLocalizacion();
        usoLocalizacion.activar();

        cargarRutasRecomendadas();

        SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefreshMenu);
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                if (usoLocalizacion != null) usoLocalizacion.ultimaLocalizacion();
                cargarRutasRecomendadas();
                swipeRefresh.setRefreshing(false);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRutasRecomendadas();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION && usoLocalizacion != null) {
            usoLocalizacion.permisoConcedido();
            cargarRutasRecomendadas();
        }
    }

    private void cargarRutasRecomendadas() {
        RecyclerView recyclerRutas = findViewById(R.id.recyclerRutasRecomendadas);
        if (recyclerRutas.getLayoutManager() == null) {
            recyclerRutas.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            new PagerSnapHelper().attachToRecyclerView(recyclerRutas);
            recyclerRutas.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        resetCarruselTimer();
                    }
                }
            });
        }
        if (adaptadorRutas == null) {
            adaptadorRutas = new AdaptadorRutasRecomendadas();
            recyclerRutas.setAdapter(adaptadorRutas);
        }

        com.example.misLugares.LugaresBDAdapter lugaresAdapter = ((Aplicacion) getApplication()).lugares;
        List<Ruta> rutas = lugaresAdapter.listarRutasSendero();

        GeoPunto posUsuario = ((Aplicacion) getApplication()).posicionActual;
        android.content.SharedPreferences prefsVal = getSharedPreferences(PREFS_VALORACION, MODE_PRIVATE);

        for (Ruta r : rutas) {
            float val = prefsVal.getFloat("ruta_val_" + r.rutaKey(), 0f);
            r.setValoracion(val);
        }

        rutas = filtrarRutasCercanas(rutas, posUsuario);

        rutasRecomendadas.clear();
        rutasRecomendadas.addAll(rutas);
        adaptadorRutas.setLugares(lugaresAdapter);
        adaptadorRutas.setRutas(rutasRecomendadas);

        TextView avisoRutas = findViewById(R.id.textMochilazoAviso);
        if (avisoRutas != null) {
            int n = rutasRecomendadas.size();
            if (n == 0)
                avisoRutas.setText("Ninguna ruta cercana");
            else if (n == 1)
                avisoRutas.setText("1 ruta cerca de ti");
            else
                avisoRutas.setText(n + " rutas cerca de ti");
        }

        adaptadorRutas.setOnRutaClickListener(ruta -> {
            Intent i = new Intent(this, LugaresDeRutaActivity.class);
            i.putExtra(LugaresDeRutaActivity.KEY_NOMBRE_RUTA, ruta.getNombre());
            i.putExtra(LugaresDeRutaActivity.KEY_DISTANCIA_KM, ruta.getDistanciaKm());
            i.putExtra(LugaresDeRutaActivity.KEY_VALORACION_RUTA, ruta.getValoracion());
            i.putIntegerArrayListExtra(LugaresDeRutaActivity.KEY_LUGAR_IDS, new ArrayList<>(ruta.getLugarIds()));
            if (ruta.getId() >= 0)
                i.putExtra(LugaresDeRutaActivity.KEY_RUTA_ID, ruta.getId());
            startActivity(i);
        });

        recyclerRutas.post(() -> {
            int paddingPx = recyclerRutas.getPaddingStart() + recyclerRutas.getPaddingEnd();
            int marginPx = (int) (24 * getResources().getDisplayMetrics().density);
            int cardW = recyclerRutas.getWidth() - paddingPx - marginPx;
            if (cardW > 0) adaptadorRutas.setCardWidthPx(cardW);
            int count = adaptadorRutas.getItemCount();
            if (count > 0) recyclerRutas.scrollToPosition(count / 2);
        });

        iniciarCarruselAutoAdvance(recyclerRutas);
    }

    private void iniciarCarruselAutoAdvance(RecyclerView recycler) {
        if (carruselHandler != null) {
            carruselHandler.removeCallbacks(carruselAutoAdvance);
        }
        carruselHandler = new Handler(Looper.getMainLooper());
        carruselAutoAdvance = new Runnable() {
            @Override
            public void run() {
                if (adaptadorRutas == null || adaptadorRutas.getRealCount() == 0) {
                    carruselHandler.postDelayed(this, CARRUSEL_INTERVAL_MS);
                    return;
                }
                RecyclerView.LayoutManager lm = recycler.getLayoutManager();
                if (lm instanceof LinearLayoutManager) {
                    int current = ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
                    if (current >= 0) recycler.smoothScrollToPosition(current + 1);
                }
                carruselHandler.postDelayed(this, CARRUSEL_INTERVAL_MS);
            }
        };
        carruselHandler.postDelayed(carruselAutoAdvance, CARRUSEL_INTERVAL_MS);
    }

    private void resetCarruselTimer() {
        if (carruselHandler != null && carruselAutoAdvance != null) {
            carruselHandler.removeCallbacks(carruselAutoAdvance);
            carruselHandler.postDelayed(carruselAutoAdvance, CARRUSEL_INTERVAL_MS);
        }
    }

    private List<Ruta> filtrarRutasCercanas(List<Ruta> rutas, GeoPunto posUsuario) {
        if (posUsuario == null || posUsuario.equals(GeoPunto.SIN_POSICION))
            return rutas;
        if (posUsuario.getLatitud() == 0.0 && posUsuario.getLongitud() == 0.0)
            return rutas;
        double rangoKm = getRangoRutasKm();
        double umbral = rangoKm * 1000;
        List<Ruta> out = new ArrayList<>();
        com.example.misLugares.LugaresBDAdapter lugares = ((Aplicacion) getApplication()).lugares;
        for (Ruta r : rutas) {
            boolean cercana = false;
            for (int id : r.getLugarIds()) {
                try {
                    Lugar l = lugares.elemento(id);
                    if (l != null && l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)
                            && l.getPosicion().distancia(posUsuario) <= umbral) {
                        cercana = true;
                        break;
                    }
                } catch (Exception ignored) { }
            }
            if (cercana) out.add(r);
        }
        return out;
    }

    private double getRangoRutasKm() {
        float km = getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE).getFloat(KEY_RANGO_RUTAS_KM, (float) DEFAULT_RANGO_KM);
        return Math.max(5, Math.min(80, km));
    }

    private void mostrarDialogoAjustesRango() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_ajustes_rango, null);
        SeekBar seek = vista.findViewById(R.id.seekRangoKm);
        TextView textRango = vista.findViewById(R.id.textRangoKm);

        double actual = getRangoRutasKm();
        int progress = (int) Math.round(actual - 5);
        progress = Math.max(0, Math.min(75, progress));
        seek.setProgress(progress);
        textRango.setText((int) (5 + progress) + " km");

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int km = 5 + progress;
                textRango.setText(km + " km");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(vista)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    int km = 5 + seek.getProgress();
                    getSharedPreferences(PREFS_AJUSTES, MODE_PRIVATE).edit()
                            .putFloat(KEY_RANGO_RUTAS_KM, km).apply();
                    cargarRutasRecomendadas();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void actualizarHoraYFondo() {
        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);

        textHora.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

        int colorTop    = COLORES_CIELO[hora][0];
        int colorBottom = COLORES_CIELO[hora][1];

        int colorMedio = blendColors(colorTop, colorBottom, 0.5f);
        rootLayout.setBackgroundColor(colorTop);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String nombreUsuario = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
                ? user.getDisplayName() : null;
        String saludoBase;
        if (hora >= 5 && hora < 7) {
            saludoBase = "Buenos días ✨";
            iconoHora.setText("🌄");
        } else if (hora >= 7 && hora < 12) {
            saludoBase = "Buenos días";
            iconoHora.setText("☀");
        } else if (hora >= 12 && hora < 15) {
            saludoBase = "Buenas tardes";
            iconoHora.setText("☀");
        } else if (hora >= 15 && hora < 18) {
            saludoBase = "Buenas tardes";
            iconoHora.setText("🌤");
        } else if (hora >= 18 && hora < 20) {
            saludoBase = "Buenas tardes";
            iconoHora.setText("🌅");
        } else if (hora >= 20 && hora < 21) {
            saludoBase = "Buenas noches";
            iconoHora.setText("🌆");
        } else {
            saludoBase = "Buenas noches";
            iconoHora.setText("🌙");
        }
        textSaludo.setText(nombreUsuario != null ? saludoBase + ", " + nombreUsuario : saludoBase);

        if (hora >= 7 && hora < 18) {
            textHora.setTextColor(0xFFFFFFFF);
            textSaludo.setTextColor(0xEEFFFFFF);
        } else {
            textHora.setTextColor(0xFFFFFFFF);
            textSaludo.setTextColor(0xDDFFFFFF);
        }
    }

    private int blendColors(int c1, int c2, float ratio) {
        float ir = 1f - ratio;
        int a = (int)((c1 >> 24 & 0xff) * ir + (c2 >> 24 & 0xff) * ratio);
        int r = (int)((c1 >> 16 & 0xff) * ir + (c2 >> 16 & 0xff) * ratio);
        int g = (int)((c1 >> 8  & 0xff) * ir + (c2 >> 8  & 0xff) * ratio);
        int b = (int)((c1       & 0xff) * ir + (c2       & 0xff) * ratio);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void animarEntrada() {
        int[] ids = {
                R.id.textSaludo,
                R.id.textHora,
                R.id.iconoHora,
                R.id.recyclerRutasRecomendadas,
                R.id.btnSenderismo,
                R.id.btnTrazarRutas,
                R.id.btnListaRutas,
                R.id.btnAjustes,
                R.id.btnCerrarSesion
        };
        for (int i = 0; i < ids.length; i++) {
            View v = findViewById(ids[i]);
            if (v == null) continue;
            AnimationSet set = new AnimationSet(true);
            set.setInterpolator(new DecelerateInterpolator(2f));

            AlphaAnimation fade = new AlphaAnimation(0f, 1f);
            fade.setDuration(450);

            TranslateAnimation slide = new TranslateAnimation(0, 0, 40, 0);
            slide.setDuration(450);

            set.addAnimation(fade);
            set.addAnimation(slide);
            set.setStartOffset(i * 90L);
            set.setFillAfter(true);
            v.startAnimation(set);
        }
    }

    private void animarClick(View v) {
        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90)
                .withEndAction(() ->
                        v.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                )                .start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (timer != null) { timer.cancel(); timer = null; }
        if (carruselHandler != null && carruselAutoAdvance != null) {
            carruselHandler.removeCallbacks(carruselAutoAdvance);
            carruselHandler = null;
        }
        if (usoLocalizacion != null) usoLocalizacion.desactivar();
    }
}