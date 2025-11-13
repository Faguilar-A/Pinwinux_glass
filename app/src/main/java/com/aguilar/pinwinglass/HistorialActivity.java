package com.aguilar.pinwinglass;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import db.AdminSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;

public class HistorialActivity extends AppCompatActivity {

    ListView lvHistorial;
    Button btnBorrarHistorial;
    AdminSQLiteOpenHelper adminDB;
    ArrayList<String> listaHistorial;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        lvHistorial = findViewById(R.id.lvHistorial);
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        listaHistorial = new ArrayList<>();

        btnBorrarHistorial = findViewById(R.id.btnBorrarHistorial);

        cargarHistorial();

        btnBorrarHistorial.setOnClickListener(v -> {
            mostrarDialogoConfirmacion();
        });
    }

    private void cargarHistorial() {
        // Limpiamos la lista antes de cargarla
        listaHistorial.clear();

        SQLiteDatabase db = adminDB.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Lee todos los registros, ordenados por ID (o fecha) descendente
            cursor = db.rawQuery("SELECT distancia, fecha FROM detecciones ORDER BY id DESC", null);

            if (cursor.getCount() == 0) {
                Toast.makeText(this, "No hay historial para mostrar", Toast.LENGTH_SHORT).show();
            }

            while (cursor.moveToNext()) {
                // Formateamos el string para mostrarlo en la lista
                String item = "Distancia: " + cursor.getDouble(0) + " cm\n" +
                        "Fecha: " + cursor.getString(1);
                listaHistorial.add(item);
            }

            // Creamos el adapter si no existe, o lo notificamos si ya existe
            if (adapter == null) {
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaHistorial);
                lvHistorial.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar el historial", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    // --- NUEVOS MÉTODOS AÑADIDOS ---

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Borrado");
        builder.setMessage("¿Estás seguro de que deseas borrar TODO el historial? Esta acción no se puede deshacer.");

        builder.setPositiveButton("Sí, Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                borrarHistorial();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void borrarHistorial() {
        SQLiteDatabase db = adminDB.getWritableDatabase();
        try {
            // Borra todas las filas de la tabla "detecciones"
            db.delete("detecciones", null, null);

            // Limpia la lista en la app y notifica al adaptador
            listaHistorial.clear();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Historial borrado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al borrar el historial", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
}