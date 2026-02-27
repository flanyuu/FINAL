package com.example.misLugares.datos;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import java.io.File;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.TipoLugar;

//import java.sql.SQLException;

public class LugaresBD extends SQLiteOpenHelper implements RepositorioLugares{
    Context contexto;
    public LugaresBD(Context contexto) {
        super(contexto, "lugares", null, 2);
        this.contexto = contexto;
    }
    /**
     * Comprueba la versión actual de la base de datos en el dispositivo.
     * Evita downgrade. Instalaciones nuevas usan versión 2 con tablas rutas.
     */
    private static int obtenerVersionBD(Context context) {
        File dbFile = context.getDatabasePath("lugares");
        if (dbFile.exists()) {
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                int versionActual = db.getVersion();
                return Math.max(1, versionActual);
            } finally {
                if (db != null) db.close();
            }
        }
        return 1;
    }
    @Override
    public void onCreate(SQLiteDatabase bd) {
        bd.execSQL("CREATE TABLE lugares (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "direccion TEXT, " +
                "longitud REAL, " +
                "latitud REAL, " +
                "tipo INTEGER, " +
                "foto TEXT, " +
                "telefono INTEGER, " +
                "url TEXT, " +
                "comentario TEXT, " +
                "fecha BIGINT, " +
                "valoracion REAL)");
        crearTablasRutas(bd);
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Facultad de Ingeniería Mecánica y Eléctrica', " +
                "'Pedro de Alba SN, Niños Héroes, Ciudad Universitaria, 66455 San Nicolás de los Garza, N.L.', " +
                "-100.31341321450944, 25.725519976662998, " +
                TipoLugar.EDUCACION.ordinal() + ", '', 818329402, " +
                "'https://www.fime.uanl.mx/', " +
                "'Uno de los mejores lugares para formarse.', " +
                System.currentTimeMillis() + ", 5.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Parque Fundidora', " +
                "'Adolfo Prieto S/N, Obrera, 64010 Monterrey, N.L.', " +
                "-100.2839212417356, 25.679147030907377, " +
                TipoLugar.NATURALEZA.ordinal() + ", '', 818126850, " +
                "'https://www.parquefundidora.org/', " +
                "'Es un parque muy bonito, ideal para pasar momentos agradables. " +
                "Tiene museo de cera, un lago en el cuál puedes pasear abordo de la lancha y varias atracciones más.\n', " +
                System.currentTimeMillis() + ", 3.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Estadio Universitario de la UANL', " +
                "'Niños Héroes, Ciudad Universitaria, 66451 San Nicolás de los Garza, N.L.', " +
                "-100.3120158371825, 25.72261699514273, " +
                TipoLugar.DEPORTE.ordinal() + ", '', 818158645, " +
                "'https://www.tigres.com.mx/es/', " +
                "'Hogar del equipo de de futbol los Tigres.', " +
                System.currentTimeMillis() + ", 5.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Alchemy Coffee Lab', " +
                "'Bruselas 902, Mirador, 64070 Monterrey, N.L.', " +
                "-100.3334355467595, 25.672397419465106, " +
                TipoLugar.RESTAURANTE.ordinal() + ", '', 813269570, " +
                "'https://www.facebook.com/alchemycoffeelab', " +
                "'El frappe fue verdaderamente delicioso, y la comida también resultó ser increíble.', " +
                System.currentTimeMillis() + ", 3.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Museo De Arte Contemporáneo De Monterrey (MARCO)', " +
                "'Juan Zuazua, Padre Raymundo Jardón y, Centro, 64000 Monterrey, N.L.', " +
                "-100.30974848806704, 25.66485082179439, " +
                TipoLugar.ESPECTACULO.ordinal() + ", '', 818262450, " +
                "'https://www.marco.org.mx/', " +
                "'Buenísimo lugar. Sus actuales exposiciones me dejaron maravillado.', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Biblioteca Universitaria Raúl Rangel Frías', " +
                "'Av. Alfonso Reyes, Ciudad Universitaria, San Nicolás de los Garza, N.L.', " +
                "-100.3142, 25.7260, " +
                TipoLugar.EDUCACION.ordinal() + ", '', 818329400, " +
                "'https://www.uanl.mx/', " +
                "'Biblioteca central del campus.', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Cafetería Centro de Ingeniería', " +
                "'Pedro de Alba, Ciudad Universitaria, San Nicolás de los Garza, N.L.', " +
                "-100.3128, 25.7250, " +
                TipoLugar.RESTAURANTE.ordinal() + ", '', 0, " +
                "'', " +
                "'Punto de encuentro para estudiantes.', " +
                System.currentTimeMillis() + ", 3.5)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Centro de Cómputo FIME', " +
                "'Pedro de Alba SN, Ciudad Universitaria, San Nicolás de los Garza, N.L.', " +
                "-100.3118, 25.7242, " +
                TipoLugar.EDUCACION.ordinal() + ", '', 0, " +
                "'', " +
                "'Laboratorios de cómputo.', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Pabellón La Pastora', " +
                "'Av. Alfonso Reyes, Ciudad Universitaria, San Nicolás de los Garza, N.L.', " +
                "-100.3130, 25.7235, " +
                TipoLugar.DEPORTE.ordinal() + ", '', 0, " +
                "'', " +
                "'Área deportiva del campus.', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Horno 3 Parque Fundidora', " +
                "'Parque Fundidora, Obrera, Monterrey, N.L.', " +
                "-100.2845, 25.6795, " +
                TipoLugar.ESPECTACULO.ordinal() + ", '', 818126850, " +
                "'https://www.parquefundidora.org/', " +
                "'Museo del Acero en el Parque Fundidora.', " +
                System.currentTimeMillis() + ", 4.5)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Plaza de los Soldados', " +
                "'Parque Fundidora, Obrera, Monterrey, N.L.', " +
                "-100.2832, 25.6788, " +
                TipoLugar.NATURALEZA.ordinal() + ", '', 0, " +
                "'', " +
                "'Plaza conmemorativa en el parque.', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, " +
                "'Escuela Politécnica Superior de Gandía', " +
                "'C/ Paranimf, 1 46730 Gandia (SPAIN)', -0.166093, 38.995656, " +
                TipoLugar.EDUCACION.ordinal() + ", '', 962849300, " +
                "'http://www.epsg.upv.es', " +
                "'Uno de los mejores lugares para formarse.', " +
                System.currentTimeMillis() + ", 3.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, 'Al de siempre', " +
                "'P.Industrial Junto Molí Nou - 46722, Benifla (Valencia)', " +
                "-0.190642, 38.925857, " + TipoLugar.BAR.ordinal() + ", '', " +
                "636472405, '', 'No te pierdas el arroz en calabaza.', " +
                System.currentTimeMillis() + ", 3.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, 'androidcurso.com', " +
                "'ciberespacio', 0.0, 0.0, " + TipoLugar.EDUCACION.ordinal() + ", '', " +
                "962849300, 'http://androidcurso.com', 'Amplia tus conocimientos sobre Android.', " +
                System.currentTimeMillis() + ", 5.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, 'Barranco del Infierno', " +
                "'Vía Verde del río Serpis. Villalonga (Valencia)', -0.295058, 38.867180, " +
                TipoLugar.NATURALEZA.ordinal() + ", '', 0, " +
                "'http://sosegaos.blogspot.com.es/2009/02/lorcha-villalonga-via-verde-del-rio.html', " +
                "'Espectacular ruta para bici o andar', " +
                System.currentTimeMillis() + ", 4.0)");
        bd.execSQL("INSERT INTO lugares VALUES (null, 'La Vital', " +
                "'Avda. La Vital,0 46701 Gandia (Valencia)', -0.1720092, 38.9705949, " +
                TipoLugar.COMPRAS.ordinal() + ", '', 962881070, " +
                "'http://www.lavital.es', 'El típico centro comercial.', " +
                System.currentTimeMillis() + ", 2.0)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            crearTablasRutas(db);
        }
    }

    private static void crearTablasRutas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS rutas (_id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, distancia_km REAL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ruta_paradas (_id INTEGER PRIMARY KEY AUTOINCREMENT, ruta_id INTEGER, lugar_id INTEGER, orden INTEGER)");
    }
    public static Lugar extraeLugar(Cursor cursor) {
        Lugar lugar = new Lugar();
        lugar.setNombre(cursor.getString(1));
        lugar.setDireccion(cursor.getString(2));
        lugar.setPosicion(new GeoPunto(cursor.getDouble(3),
                cursor.getDouble(4)));
        lugar.setTipo(TipoLugar.values()[cursor.getInt(5)]);
        lugar.setFoto(cursor.getString(6));
        lugar.setTelefono(cursor.getInt(7));
        lugar.setUrl(cursor.getString(8));
        lugar.setComentario(cursor.getString(9));
        lugar.setFecha(cursor.getLong(10));
        lugar.setValoracion(cursor.getFloat(11));
        return lugar;
    }
    public Cursor extraeCursor() {
        //String consulta = "SELECT * FROM lugares"; //
        //String consulta = "SELECT * FROM lugares WHERE valoracion>2.0 ORDER BY valoracion DESC LIMIT 4";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(contexto);
        String max = pref.getString("maximo","12");
        String consulta;
        switch (pref.getString("orden", "0")) {
            case "0":
                consulta = "SELECT * FROM lugares LIMIT " + max;
                break;
            case "1":
                consulta = "SELECT * FROM lugares ORDER BY valoracion DESC LIMIT " + max;
                break;
            default:
                Aplicacion aplicacion = (Aplicacion) contexto.getApplicationContext();
                double lon = aplicacion.posicionActual.getLongitud();
                double lat = aplicacion.posicionActual.getLatitud();
                consulta = "SELECT * FROM lugares ORDER BY " +
                        "(" + lon + "-longitud)*(" + lon + "-longitud) + " +
                        "(" + lat + "-latitud)*(" + lat + "-latitud) LIMIT " + max;
                break;
        }
        SQLiteDatabase bd = getReadableDatabase();
        return bd.rawQuery(consulta, null);
    }

    public Cursor extraeCursorCompleto() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(contexto);
        String consulta;
        switch (pref.getString("orden", "0")) {
            case "0":
                consulta = "SELECT * FROM lugares LIMIT 500";
                break;
            case "1":
                consulta = "SELECT * FROM lugares ORDER BY valoracion DESC LIMIT 500";
                break;
            default:
                Aplicacion aplicacion = (Aplicacion) contexto.getApplicationContext();
                double lon = aplicacion.posicionActual.getLongitud();
                double lat = aplicacion.posicionActual.getLatitud();
                consulta = "SELECT * FROM lugares ORDER BY " +
                        "(" + lon + "-longitud)*(" + lon + "-longitud) + " +
                        "(" + lat + "-latitud)*(" + lat + "-latitud) LIMIT 500";
                break;
        }
        return getReadableDatabase().rawQuery(consulta, null);
    }

    public java.util.List<Lugar> listarTodosConPosicion() {
        java.util.List<Lugar> out = new java.util.ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM lugares", null);
        while (c != null && c.moveToNext()) {
            Lugar l = extraeLugar(c);
            if (l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)
                    && (l.getPosicion().getLatitud() != 0 || l.getPosicion().getLongitud() != 0))
                out.add(l);
        }
        if (c != null) c.close();
        return out;
    }

    public void listarTodosConPosicionYIds(java.util.List<Integer> ids, java.util.List<Lugar> lugares) {
        ids.clear();
        lugares.clear();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM lugares", null);
        if (c == null) return;
        while (c.moveToNext()) {
            Lugar l = extraeLugar(c);
            if (l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)
                    && (l.getPosicion().getLatitud() != 0 || l.getPosicion().getLongitud() != 0)) {
                ids.add(c.getInt(0));
                lugares.add(l);
            }
        }
        c.close();
    }

    /** Lugares de senderos (GPX): comentario = 'gpx_sendero' o comentarios típicos del seed v2. No se mezclan con el resto. */
    public void listarSenderoConPosicionYIds(java.util.List<Integer> ids, java.util.List<Lugar> lugares) {
        ids.clear();
        lugares.clear();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM lugares WHERE comentario = 'gpx_sendero' OR comentario IN ('Inicio de ruta','Primer tramo','Mitad del recorrido','Último tramo de subida','Final de ruta','Tramo 25%','Tramo 75%','Cumbre','Punto intermedio','Tramo de regreso')", null);
        if (c == null) return;
        while (c.moveToNext()) {
            Lugar l = extraeLugar(c);
            if (l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)
                    && (l.getPosicion().getLatitud() != 0 || l.getPosicion().getLongitud() != 0)) {
                ids.add(c.getInt(0));
                lugares.add(l);
            }
        }
        c.close();
    }

    /** Lugares que no son de sendero GPX (para rutas urbanas/otras). No se mezclan con senderos. */
    public void listarOtrosConPosicionYIds(java.util.List<Integer> ids, java.util.List<Lugar> lugares) {
        ids.clear();
        lugares.clear();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM lugares WHERE (comentario IS NULL OR (comentario <> 'gpx_sendero' AND comentario NOT IN ('Inicio de ruta','Primer tramo','Mitad del recorrido','Último tramo de subida','Final de ruta','Tramo 25%','Tramo 75%','Cumbre','Punto intermedio','Tramo de regreso')))", null);
        if (c == null) return;
        while (c.moveToNext()) {
            Lugar l = extraeLugar(c);
            if (l.getPosicion() != null && !l.getPosicion().equals(GeoPunto.SIN_POSICION)
                    && (l.getPosicion().getLatitud() != 0 || l.getPosicion().getLongitud() != 0)) {
                ids.add(c.getInt(0));
                lugares.add(l);
            }
        }
        c.close();
    }

    public java.util.List<com.example.misLugares.modelo.Ruta> listarRutasSendero() {
        java.util.List<com.example.misLugares.modelo.Ruta> out = new java.util.ArrayList<>();
        Cursor cRutas = getReadableDatabase().rawQuery("SELECT _id, nombre, distancia_km FROM rutas ORDER BY _id", null);
        if (cRutas == null) return out;
        while (cRutas.moveToNext()) {
            long rutaId = cRutas.getLong(0);
            String nombre = cRutas.getString(1);
            double distKm = cRutas.getDouble(2);
            java.util.List<Integer> lugarIds = new java.util.ArrayList<>();
            Cursor cParadas = getReadableDatabase().rawQuery("SELECT lugar_id FROM ruta_paradas WHERE ruta_id = " + rutaId + " ORDER BY orden", null);
            if (cParadas != null) {
                while (cParadas.moveToNext()) lugarIds.add(cParadas.getInt(0));
                cParadas.close();
            }
            com.example.misLugares.modelo.Ruta r = new com.example.misLugares.modelo.Ruta(nombre, distKm, lugarIds);
            r.setId(rutaId);
            out.add(r);
        }
        cRutas.close();
        return out;
    }

    public java.util.List<Integer> listarIdsIniciosSendero() {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT lugar_id FROM ruta_paradas WHERE orden = 0 ORDER BY ruta_id", null);
        if (c != null) {
            while (c.moveToNext()) out.add(c.getInt(0));
            c.close();
        }
        return out;
    }

    public void actualizarRutaSendero(long rutaId, String nombre, java.util.List<Integer> lugarIds) {
        if (lugarIds == null) return;
        SQLiteDatabase db = getWritableDatabase();
        String nom = (nombre != null ? nombre : "").replace("'", "''");
        double distKm = calcularDistanciaRuta(db, lugarIds);
        db.execSQL("UPDATE rutas SET nombre = '" + nom + "', distancia_km = " + distKm + " WHERE _id = " + rutaId);
        db.execSQL("DELETE FROM ruta_paradas WHERE ruta_id = " + rutaId);
        for (int i = 0; i < lugarIds.size(); i++)
            db.execSQL("INSERT INTO ruta_paradas (ruta_id, lugar_id, orden) VALUES (" + rutaId + ", " + lugarIds.get(i) + ", " + i + ")");
    }

    public com.example.misLugares.modelo.Ruta obtenerRutaSendero(long rutaId) {
        Cursor cRutas = getReadableDatabase().rawQuery("SELECT _id, nombre, distancia_km FROM rutas WHERE _id = " + rutaId, null);
        if (cRutas == null || !cRutas.moveToFirst()) {
            if (cRutas != null) cRutas.close();
            return null;
        }
        String nombre = cRutas.getString(1);
        double distKm = cRutas.getDouble(2);
        cRutas.close();
        java.util.List<Integer> lugarIds = new java.util.ArrayList<>();
        Cursor cParadas = getReadableDatabase().rawQuery("SELECT lugar_id FROM ruta_paradas WHERE ruta_id = " + rutaId + " ORDER BY orden", null);
        if (cParadas != null) {
            while (cParadas.moveToNext()) lugarIds.add(cParadas.getInt(0));
            cParadas.close();
        }
        com.example.misLugares.modelo.Ruta r = new com.example.misLugares.modelo.Ruta(nombre, distKm, lugarIds);
        r.setId(rutaId);
        return r;
    }

    @Override
    public Lugar elemento(int id) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM lugares WHERE _id = "+id, null);
        try {
            if (cursor.moveToNext())
                return extraeLugar(cursor);
            else
                throw new SQLException("Error al acceder al elemento _id = "+id);
        } catch (Exception e) {
            throw e;
        } finally {
            if (cursor!=null) cursor.close();
        }
    }
    @Override
    public void añade(Lugar lugar) {

    }
    @Override
    public int nuevo() {
        int _id = -1;
        Lugar lugar = new Lugar();
        getWritableDatabase().execSQL("INSERT INTO lugares (nombre, " +
                "direccion, longitud, latitud, tipo, foto, telefono, url, " +
                "comentario, fecha, valoracion) VALUES ('', '', " +
                lugar.getPosicion().getLongitud() + "," +
                lugar.getPosicion().getLatitud() + "," + lugar.getTipo().ordinal() +
                ", '', 0, '', '', " + lugar.getFecha() + ", 0)");
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT _id FROM lugares WHERE fecha = " + lugar.getFecha(), null);
        if (c.moveToNext()) _id = c.getInt(0);
        c.close();
        return _id;
    }
    @Override
    public void borrar(int id) {
        getWritableDatabase().execSQL("DELETE FROM lugares WHERE _id = " + id);
    }
    @Override
    public int tamaño() {
        return 0;
    }
    @Override
    public void actualiza(int id, Lugar lugar) {
        getWritableDatabase().execSQL("UPDATE lugares SET" +
                " nombre = '" + lugar.getNombre() + "'" +
                ", direccion = '" + lugar.getDireccion() + "'" +
                ", longitud = " + lugar.getPosicion().getLongitud() +
                ", latitud = " + lugar.getPosicion().getLatitud() +
                ", tipo = " + lugar.getTipo().ordinal() +
                ", foto = '" + lugar.getFoto() + "'" +
                ", telefono = " + lugar.getTelefono() +
                ", url = '" + lugar.getUrl() + "'" +
                ", comentario = '" + lugar.getComentario() + "'" +
                ", fecha = " + lugar.getFecha() +
                ", valoracion = " + lugar.getValoracion() +
                " WHERE _id = " + id);
    }

    public void seedRutasGpxSiNecesario() {
        android.content.SharedPreferences prefs = contexto.getSharedPreferences("rutas_app", Context.MODE_PRIVATE);
        if (prefs.getBoolean("gpx_seed_v2", false) && !prefs.getBoolean("gpx_seed_v3", false)) {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM ruta_paradas");
            db.execSQL("DELETE FROM rutas");
            db.execSQL("DELETE FROM lugares WHERE comentario = 'gpx_sendero' OR comentario IN ('Inicio de ruta','Primer tramo','Mitad del recorrido','Último tramo de subida','Final de ruta','Tramo 25%','Tramo 75%','Cumbre','Punto intermedio','Tramo de regreso')");
            prefs.edit().putBoolean("gpx_seed_v2", false).putBoolean("rutas_sendero_seed", false).apply();
        }
        if (prefs.getBoolean("gpx_seed_v2", false)) {
            SQLiteDatabase db = getWritableDatabase();
            seedRutasSenderoEnTabla(db, prefs);
            actualizarNombresParadasSendero(db);
            return;
        }

        long t = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("DELETE FROM lugares WHERE comentario IN ('Inicio','Inicio sendero','Tramo 1','Tramo 2')");
            db.execSQL("DELETE FROM lugares WHERE comentario = 'gpx_sendero'");

            insertarLugarGpx(db, t, "Cerro el Chupón", -100.278070, 25.608977, "Inicio de ruta");
            insertarLugarGpx(db, t, "Sendero ascenso", -100.283747, 25.610879, "Primer tramo");
            insertarLugarGpx(db, t, "Sendero ascenso", -100.289424, 25.612781, "Primer tramo");
            insertarLugarGpx(db, t, "Mirador intermedio", -100.292438, 25.609568, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Mirador intermedio", -100.295452, 25.606355, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Cerca de cumbre", -100.296574, 25.607614, "Último tramo de subida");
            insertarLugarGpx(db, t, "Cerca de cumbre", -100.297695, 25.608272, "Último tramo de subida");
            insertarLugarGpx(db, t, "Cumbre", -100.296578, 25.608801, "Final de ruta");

            insertarLugarGpx(db, t, "Volcán An", -100.606972, 25.745207, "Inicio de ruta");
            insertarLugarGpx(db, t, "Primer descenso", -100.605999, 25.745288, "Tramo 25%");
            insertarLugarGpx(db, t, "Primer descenso", -100.605025, 25.745369, "Tramo 25%");
            insertarLugarGpx(db, t, "Zona baja", -100.604623, 25.744516, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Zona baja", -100.604220, 25.743662, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Camino de regreso", -100.601904, 25.743832, "Tramo 75%");
            insertarLugarGpx(db, t, "Camino de regreso", -100.599747, 25.744002, "Tramo 75%");
            insertarLugarGpx(db, t, "Llegada", -100.601060, 25.744643, "Final de ruta");

            insertarLugarGpx(db, t, "Los Quemados", -100.989948, 25.336398, "Inicio de ruta");
            insertarLugarGpx(db, t, "Subida inicial", -100.985396, 25.337395, "Primer tramo");
            insertarLugarGpx(db, t, "Subida inicial", -100.980844, 25.338392, "Primer tramo");
            insertarLugarGpx(db, t, "Zona alta", -100.972005, 25.337860, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Zona alta", -100.963146, 25.337325, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Cima", -100.962404, 25.336120, "Cumbre");
            insertarLugarGpx(db, t, "Cima", -100.960862, 25.334914, "Cumbre");
            insertarLugarGpx(db, t, "Regreso", -100.989967, 25.336430, "Final de ruta");

            insertarLugarGpx(db, t, "Cerro de las Mitras", -100.402206, 25.721746, "Inicio de ruta");
            insertarLugarGpx(db, t, "Sendero Kilimanjaro", -100.405060, 25.720528, "Primer tramo");
            insertarLugarGpx(db, t, "Sendero Kilimanjaro", -100.407913, 25.719309, "Primer tramo");
            insertarLugarGpx(db, t, "Cruce", -100.411562, 25.715503, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Cruce", -100.415211, 25.711697, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Bajada", -100.414463, 25.711546, "Tramo de regreso");
            insertarLugarGpx(db, t, "Bajada", -100.413715, 25.711394, "Tramo de regreso");
            insertarLugarGpx(db, t, "Llegada", -100.402199, 25.721741, "Final de ruta");

            insertarLugarGpx(db, t, "Mirador Azores", -100.359142, 25.687852, "Inicio de ruta");
            insertarLugarGpx(db, t, "Subida", -100.360258, 25.688492, "Primer tramo");
            insertarLugarGpx(db, t, "Subida", -100.361373, 25.689131, "Primer tramo");
            insertarLugarGpx(db, t, "Mirador", -100.362719, 25.690244, "Punto intermedio");
            insertarLugarGpx(db, t, "Mirador", -100.364065, 25.691357, "Punto intermedio");
            insertarLugarGpx(db, t, "Descenso", -100.365469, 25.692360, "Tramo 75%");
            insertarLugarGpx(db, t, "Descenso", -100.366274, 25.693362, "Tramo 75%");
            insertarLugarGpx(db, t, "Paradero Rangel Frías", -100.366701, 25.695717, "Final de ruta");

            insertarLugarGpx(db, t, "Picacho Lomas de Lourdes", -100.986941, 25.362670, "Inicio de ruta");
            insertarLugarGpx(db, t, "Sendero", -100.986839, 25.361547, "Primer tramo");
            insertarLugarGpx(db, t, "Sendero", -100.986737, 25.360424, "Primer tramo");
            insertarLugarGpx(db, t, "Puerto", -100.985814, 25.359241, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Puerto", -100.985299, 25.358058, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Cima", -100.985574, 25.358225, "Cumbre");
            insertarLugarGpx(db, t, "Cima", -100.985849, 25.358392, "Cumbre");
            insertarLugarGpx(db, t, "Final de ruta", -100.983550, 25.356551, "Final de ruta");

            insertarLugarGpx(db, t, "Picacho Los Timones", -100.998033, 25.312485, "Inicio de ruta");
            insertarLugarGpx(db, t, "Sendero El Recreo", -100.997286, 25.312427, "Primer tramo");
            insertarLugarGpx(db, t, "Sendero El Recreo", -100.996538, 25.312369, "Primer tramo");
            insertarLugarGpx(db, t, "Cruce", -100.996398, 25.312860, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Cruce", -100.996258, 25.313350, "Mitad del recorrido");
            insertarLugarGpx(db, t, "Subida final", -100.996044, 25.313684, "Tramo 75%");
            insertarLugarGpx(db, t, "Subida final", -100.995829, 25.314018, "Tramo 75%");
            insertarLugarGpx(db, t, "Cumbre", -100.995350, 25.314903, "Final de ruta");

            prefs.edit().putBoolean("gpx_seed_v2", true).putBoolean("gpx_seed_v3", true).apply();
            seedRutasSenderoEnTabla(db, prefs);
            actualizarNombresParadasSendero(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
        prefs.edit().remove("rutas_guardadas").remove("rutas_place_count").apply();
    }

    private void actualizarNombresParadasSendero(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT _id FROM lugares WHERE comentario = 'gpx_sendero' OR comentario IN ('Inicio de ruta','Primer tramo','Mitad del recorrido','Último tramo de subida','Final de ruta','Tramo 25%','Tramo 75%','Cumbre','Punto intermedio','Tramo de regreso') ORDER BY _id ASC", null);
        if (c == null || c.getCount() < 56) { if (c != null) c.close(); return; }
        int[] ids = new int[56];
        int i = 0;
        while (c.moveToNext() && i < 56) { ids[i++] = c.getInt(0); }
        c.close();
        if (i < 56) return;
        String[] nombres = {
            "Villa Sol", "La pedrera", "Mirador del Chupón", "La cama de piedra", "Cuesta del viento", "Cima Chupón", "Roca del sur", "Llegada Villa Sol",
            "Cráter An", "Bajada del lava", "Valle de ceniza", "Camino del cráter", "Cima An", "Cresta norte", "Sendero del regreso", "Llegada An",
            "Puerto de la Virgen", "Peña colorada", "Los Quemados", "Zona del cañón", "Cima San Lorenzo", "Bajada del cañón", "Cruce los pinos", "Llegada Quemados",
            "Minas", "Kilimanjaro", "La cruz", "Cuesta Mitras", "Cima Mitras", "Cúspide Mitras", "Bajada al valle", "Llegada Mitras",
            "Azores", "Subida al mirador", "Balcón Azores", "Descenso Rangel", "Cima Azores", "Paradero Rangel", "Cruce Rangel Frías", "Llegada Azores",
            "Lourdes", "Sendero norte", "Puerto Lourdes", "Roca del picacho", "Cima Lourdes", "Bajada sur", "Cruce Lourdes", "Llegada Lourdes",
            "El Recreo", "Sendero Timones", "Cruce Recreo", "Subida Timones", "Cima Timones", "Cumbre Timones", "Bajada Recreo", "Llegada Timones"
        };
        for (int j = 0; j < 56; j++) {
            String nom = nombres[j].replace("'", "''");
            db.execSQL("UPDATE lugares SET nombre = '" + nom + "' WHERE _id = " + ids[j]);
        }
    }

    private void seedRutasSenderoEnTabla(SQLiteDatabase db, android.content.SharedPreferences prefs) {
        if (prefs.getBoolean("rutas_sendero_seed", false)) return;
        Cursor c = db.rawQuery("SELECT _id FROM lugares WHERE comentario = 'gpx_sendero' OR comentario IN ('Inicio de ruta','Primer tramo','Mitad del recorrido','Último tramo de subida','Final de ruta','Tramo 25%','Tramo 75%','Cumbre','Punto intermedio','Tramo de regreso') ORDER BY _id ASC", null);
        if (c == null || c.getCount() < 56) { if (c != null) c.close(); return; }
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        while (c.moveToNext() && ids.size() < 56) ids.add(c.getInt(0));
        c.close();
        if (ids.size() < 56) return;

        String[] nombresRutas = {
            "Cerro el Chupón - Ruta Villa Sol",
            "Volcán An",
            "Los Quemados - Puerto de la Virgen",
            "Minas - Cerro de las Mitras",
            "Mirador Azores - Paradero Rangel Frías",
            "Picacho Lomas de Lourdes (Opción 2)",
            "Picacho Los Timones o El Recreo (Opción 1)"
        };
        final int PARADAS_POR_RUTA = 8;
        for (int r = 0; r < 7; r++) {
            java.util.List<Integer> paradaIds = new java.util.ArrayList<>();
            for (int i = 0; i < PARADAS_POR_RUTA; i++) paradaIds.add(ids.get(r * PARADAS_POR_RUTA + i));
            double distKm = calcularDistanciaRuta(db, paradaIds);
            String nom = nombresRutas[r].replace("'", "''");
            db.execSQL("INSERT INTO rutas (nombre, distancia_km) VALUES ('" + nom + "', " + distKm + ")");
            long rutaId = getLastInsertId(db, "rutas");
            for (int i = 0; i < PARADAS_POR_RUTA; i++)
                db.execSQL("INSERT INTO ruta_paradas (ruta_id, lugar_id, orden) VALUES (" + rutaId + ", " + paradaIds.get(i) + ", " + i + ")");
        }
        prefs.edit().putBoolean("rutas_sendero_seed", true).apply();
    }

    private static long getLastInsertId(SQLiteDatabase db, String table) {
        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        long id = -1;
        if (c != null && c.moveToFirst()) id = c.getLong(0);
        if (c != null) c.close();
        return id;
    }

    private double calcularDistanciaRuta(SQLiteDatabase db, java.util.List<Integer> lugarIds) {
        if (lugarIds == null || lugarIds.size() < 2) return 0;
        double metros = 0;
        GeoPunto prev = null;
        for (int id : lugarIds) {
            Cursor cur = db.rawQuery("SELECT longitud, latitud FROM lugares WHERE _id = " + id, null);
            if (cur == null || !cur.moveToFirst()) { if (cur != null) cur.close(); continue; }
            GeoPunto p = new GeoPunto(cur.getDouble(0), cur.getDouble(1));
            cur.close();
            if (prev != null) metros += prev.distancia(p);
            prev = p;
        }
        return Math.round(metros / 10.0) / 100.0;
    }

    private void insertarLugarGpx(SQLiteDatabase db, long fecha, String nombre, double lon, double lat, String comentario) {
        String nom = nombre.replace("'", "''");
        db.execSQL("INSERT INTO lugares (nombre, direccion, longitud, latitud, tipo, foto, telefono, url, comentario, fecha, valoracion) VALUES (" +
                "'" + nom + "', '', " + lon + ", " + lat + ", " + TipoLugar.NATURALEZA.ordinal() + ", '', 0, '', 'gpx_sendero', " + fecha + ", 4.0)");
    }
}

