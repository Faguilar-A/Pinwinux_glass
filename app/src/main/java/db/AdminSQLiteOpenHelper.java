package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

// Adaptamos la clase del PDF [cite: 9]
public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    // Constructor [cite: 16]
    public AdminSQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Método onCreate: Se ejecuta para crear las tablas [cite: 19]
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creamos la tabla de "usuarios", no de "productos" [cite: 22]
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "correo TEXT UNIQUE," + // Usamos el correo como campo único
                "edad INTEGER," +
                "password TEXT)");

        db.execSQL("CREATE TABLE detecciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "distancia REAL," +
                "fecha TEXT DEFAULT CURRENT_TIMESTAMP)"); // Guarda la fecha automáticamente
    }

    // Método onUpgrade: Para futuras actualizaciones de la BD [cite: 30]
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Borramos la tabla si existe y la volvemos a crear [cite: 35]
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS detecciones");
        onCreate(db); // [cite: 36]
    }
}