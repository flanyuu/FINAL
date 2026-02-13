package com.example.misLugares.datos;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.preference.PreferenceManager;

import com.example.misLugares.Aplicacion;
import com.example.misLugares.modelo.GeoPunto;
import com.example.misLugares.modelo.Lugar;
import com.example.misLugares.modelo.TipoLugar;

public class LugaresBD extends SQLiteOpenHelper implements RepositorioLugares {
    private static final int VERSION = 2;
    Context contexto;
    public LugaresBD(Context contexto) {
        this(contexto, "lugares");
    }
    public LugaresBD(Context contexto, String nombreBD) {
        super(contexto, nombreBD, null, VERSION);
        this.contexto = contexto;
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
}

