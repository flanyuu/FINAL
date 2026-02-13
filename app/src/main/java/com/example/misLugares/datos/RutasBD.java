package com.example.misLugares.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.misLugares.modelo.Ruta;
import com.example.misLugares.modelo.WaypointRuta;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de datos de rutas guardadas (cada ruta tiene al menos 2 waypoints: punto A y B).
 */
public class RutasBD extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "rutas";
    private static final int VERSION = 7;

    private static final String TABLA_RUTAS = "rutas";
    private static final String TABLA_WAYPOINTS = "ruta_waypoints";

    public RutasBD(Context context) {
        super(context, NOMBRE_BD, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_RUTAS + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "fecha BIGINT NOT NULL, " +
                "distancia_km REAL DEFAULT 0, " +
                "valoracion REAL DEFAULT 0, " +
                "tipo_transporte TEXT DEFAULT 'car')");
        db.execSQL("CREATE TABLE " + TABLA_WAYPOINTS + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ruta_id INTEGER NOT NULL, " +
                "orden INTEGER NOT NULL, " +
                "latitud REAL NOT NULL, " +
                "longitud REAL NOT NULL, " +
                "nombre TEXT, " +
                "FOREIGN KEY (ruta_id) REFERENCES " + TABLA_RUTAS + "(_id) ON DELETE CASCADE)");
        insertarRutasDefault(db);
    }

    private void insertarRutasDefault(SQLiteDatabase db) {
        long fecha = System.currentTimeMillis();

        // Ruta a pie – Urbano (Fundidora + Santa Lucía + Macroplaza)
        long r1 = insert(db, "Urbano Fundidora, Santa Lucía y Macroplaza", fecha, "foot");
        insertWaypoint(db, r1, 0, 25.67870, -100.28430, "Parque Fundidora");
        insertWaypoint(db, r1, 1, 25.67890, -100.28470, "Horno 3 / Museo del Acero");
        insertWaypoint(db, r1, 2, 25.67950, -100.28280, "Lago de Aceración, Parque Fundidora");
        insertWaypoint(db, r1, 3, 25.67172, -100.30643, "Paseo Santa Lucía");
        insertWaypoint(db, r1, 4, 25.66923, -100.30992, "Macroplaza");

        // Ruta en auto – Escénica y naturaleza
        long r2 = insert(db, "Escénica y naturaleza", fecha, "car");
        insertWaypoint(db, r2, 0, 25.66923, -100.30992, "Macroplaza");
        insertWaypoint(db, r2, 1, 25.4167, -100.1170, "Presa La Boca (Rodrigo Gómez)");
        insertWaypoint(db, r2, 2, 25.36244, -100.3170, "Centro de Santiago");
        insertWaypoint(db, r2, 3, 25.4210, -100.1810, "Cascada Cola de Caballo");
        insertWaypoint(db, r2, 4, 25.6481, -100.3740, "Grutas de García");

        // Ruta en bicicleta – Naturaleza y ciclismo
        long r3 = insert(db, "Naturaleza y ciclismo", fecha, "bike");
        insertWaypoint(db, r3, 0, 25.64619, -100.45978, "Entrada a La Huasteca");
        insertWaypoint(db, r3, 1, 25.6440, -100.4560, "Cañón de la Huasteca");
        insertWaypoint(db, r3, 2, 25.6455, -100.4588, "Zona de escalada en La Huasteca");
        insertWaypoint(db, r3, 3, 25.6500, -100.4600, "Regreso hacia Santa Catarina");
        insertWaypoint(db, r3, 4, 25.67870, -100.28430, "Parque Fundidora");

        // Ruta a pie – Cerro de las Mitras (5 puntos)
        long r4 = insert(db, "Cerro de las Mitras", fecha, "foot");
        insertWaypoint(db, r4, 0, 25.7020, -100.3680, "Inicio sendero");
        insertWaypoint(db, r4, 1, 25.7080, -100.3620, "Mirador La V");
        insertWaypoint(db, r4, 2, 25.7120, -100.3580, "Pico Muela");
        insertWaypoint(db, r4, 3, 25.7150, -100.3550, "Cerca de cumbre");
        insertWaypoint(db, r4, 4, 25.7180, -100.3520, "Pico Piloto / regreso");
    }

    private long insert(SQLiteDatabase db, String nombre, long fecha, String tipoTransporte) {
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("fecha", fecha);
        cv.put("distancia_km", 0.0);
        cv.put("valoracion", 0.0f);
        cv.put("tipo_transporte", tipoTransporte != null ? tipoTransporte : "car");
        return db.insert(TABLA_RUTAS, null, cv);
    }

    private void insertWaypoint(SQLiteDatabase db, long rutaId, int orden, double latitud, double longitud, String nombre) {
        ContentValues cv = new ContentValues();
        cv.put("ruta_id", rutaId);
        cv.put("orden", orden);
        cv.put("latitud", latitud);
        cv.put("longitud", longitud);
        cv.put("nombre", nombre != null ? nombre : "");
        db.insert(TABLA_WAYPOINTS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Quienes ya tenían la app: insertar rutas por defecto si la tabla está vacía
            Cursor c = db.query(TABLA_RUTAS, new String[]{"_id"}, null, null, null, null, null);
            boolean vacia = (c.getCount() == 0);
            c.close();
            if (vacia) {
                insertarRutasDefault(db);
            }
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLA_RUTAS + " ADD COLUMN distancia_km REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLA_RUTAS + " ADD COLUMN valoracion REAL DEFAULT 0");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLA_RUTAS + " ADD COLUMN tipo_transporte TEXT DEFAULT 'car'");
        }
        if (oldVersion < 6) {
            // Por si la BD llegó a v5 sin esta columna (p. ej. otro build)
            if (!tieneColumna(db, TABLA_RUTAS, "tipo_transporte")) {
                db.execSQL("ALTER TABLE " + TABLA_RUTAS + " ADD COLUMN tipo_transporte TEXT DEFAULT 'car'");
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si se instala una versión antigua sobre una nueva, recrear la BD para evitar crash.
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_WAYPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_RUTAS);
        onCreate(db);
    }

    private boolean tieneColumna(SQLiteDatabase db, String tabla, String columna) {
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + tabla + ")", null)) {
            while (c.moveToNext()) {
                if (columna.equals(c.getString(1))) return true;
            }
        }
        return false;
    }

    /** Inserta una ruta con sus waypoints. Requiere al menos 2 waypoints. tipoTransporte: "car", "foot", "bike". */
    public long insertarRuta(String nombre, List<WaypointRuta> waypoints, String tipoTransporte) {
        if (waypoints == null || waypoints.size() < 2) return -1;
        SQLiteDatabase db = getWritableDatabase();
        long fecha = System.currentTimeMillis();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("fecha", fecha);
        cv.put("distancia_km", 0.0);
        cv.put("valoracion", 0.0f);
        cv.put("tipo_transporte", tipoTransporte != null ? tipoTransporte : "car");
        long rutaId = db.insert(TABLA_RUTAS, null, cv);
        if (rutaId == -1) return -1;
        for (int i = 0; i < waypoints.size(); i++) {
            WaypointRuta w = waypoints.get(i);
            ContentValues cvw = new ContentValues();
            cvw.put("ruta_id", rutaId);
            cvw.put("orden", i);
            cvw.put("latitud", w.getLatitud());
            cvw.put("longitud", w.getLongitud());
            cvw.put("nombre", w.getNombre());
            db.insert(TABLA_WAYPOINTS, null, cvw);
        }
        return rutaId;
    }

    /** Obtiene todas las rutas (sin waypoints; se cargan bajo demanda). */
    public List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLA_RUTAS, new String[]{"_id", "nombre", "fecha", "distancia_km", "valoracion", "tipo_transporte"},
                null, null, null, null, "fecha DESC");
        while (c.moveToNext()) {
            Ruta r = new Ruta();
            r.setId(c.getInt(0));
            r.setNombre(c.getString(1));
            r.setFecha(c.getLong(2));
            r.setDistanciaKm(c.getDouble(3));
            r.setValoracion((float) c.getDouble(4));
            r.setTipoTransporte(c.isNull(5) ? "car" : c.getString(5));
            r.setWaypoints(obtenerWaypoints(r.getId()));
            lista.add(r);
        }
        c.close();
        return lista;
    }

    public List<WaypointRuta> obtenerWaypoints(int rutaId) {
        List<WaypointRuta> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLA_WAYPOINTS,
                new String[]{"orden", "latitud", "longitud", "nombre"},
                "ruta_id = ?", new String[]{String.valueOf(rutaId)}, null, null, "orden ASC");
        while (c.moveToNext()) {
            lista.add(new WaypointRuta(c.getInt(0), c.getDouble(1), c.getDouble(2), c.getString(3)));
        }
        c.close();
        return lista;
    }

    public Ruta obtenerRuta(int id) {
        Cursor c = getReadableDatabase().query(TABLA_RUTAS, new String[]{"_id", "nombre", "fecha", "distancia_km", "valoracion", "tipo_transporte"},
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (!c.moveToNext()) { c.close(); return null; }
        Ruta r = new Ruta();
        r.setId(c.getInt(0));
        r.setNombre(c.getString(1));
        r.setFecha(c.getLong(2));
        r.setDistanciaKm(c.getDouble(3));
        r.setValoracion((float) c.getDouble(4));
        r.setTipoTransporte(c.isNull(5) ? "car" : c.getString(5));
        r.setWaypoints(obtenerWaypoints(r.getId()));
        c.close();
        return r;
    }

    /** Actualiza la distancia en km de una ruta (p. ej. tras obtenerla de Directions API). */
    public void actualizarDistancia(int rutaId, double distanciaKm) {
        ContentValues cv = new ContentValues();
        cv.put("distancia_km", distanciaKm);
        getWritableDatabase().update(TABLA_RUTAS, cv, "_id = ?", new String[]{String.valueOf(rutaId)});
    }

    /** Actualiza la valoración en estrellas de una ruta. */
    public void actualizarValoracion(int rutaId, float valoracion) {
        ContentValues cv = new ContentValues();
        cv.put("valoracion", valoracion);
        getWritableDatabase().update(TABLA_RUTAS, cv, "_id = ?", new String[]{String.valueOf(rutaId)});
    }

    public void eliminarRuta(int id) {
        getWritableDatabase().delete(TABLA_WAYPOINTS, "ruta_id = ?", new String[]{String.valueOf(id)});
        getWritableDatabase().delete(TABLA_RUTAS, "_id = ?", new String[]{String.valueOf(id)});
    }
}
