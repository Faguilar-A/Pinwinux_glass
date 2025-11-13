package com.aguilar.pinwinglass;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import db.AdminSQLiteOpenHelper;
import android.content.Intent;

public class EditProfileActivity extends AppCompatActivity {

    EditText etNombreDetalle, etCorreoDetalle, etEdadDetalle;
    Button btnGuardarCambios;
    Button btnEliminarCuenta;
    AdminSQLiteOpenHelper adminDB;
    int loggedInUserId;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        etNombreDetalle = findViewById(R.id.etNombreDetalle);
        etCorreoDetalle = findViewById(R.id.etCorreoDetalle);
        etEdadDetalle = findViewById(R.id.etEdadDetalle);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getInt(LoginActivity.KEY_USER_ID, 0);

        if (loggedInUserId > 0) {
            // 1. Cargamos los datos actuales en los campos
            cargarDatosUsuario(loggedInUserId);
        } else {
            Toast.makeText(this, "Error: No se encontró usuario", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 2. Listener para guardar
        btnGuardarCambios.setOnClickListener(v -> actualizarUsuario(loggedInUserId));

        btnEliminarCuenta.setOnClickListener(v -> {
            // No eliminamos directamente, mostramos el diálogo
            mostrarDialogoConfirmacion();
        });
    }

    // Método para cargar datos (READ) - Para pre-llenar los campos
    private void cargarDatosUsuario(int id) {
        SQLiteDatabase db = adminDB.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, correo, edad FROM usuarios WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            etNombreDetalle.setText(cursor.getString(0));
            etCorreoDetalle.setText(cursor.getString(1));
            etEdadDetalle.setText(String.valueOf(cursor.getInt(2)));
        }
        cursor.close();
        db.close();
    }

    // Método para actualizar (UPDATE) - Esta es la lógica que movimos
    private void actualizarUsuario(int id) {
        String nombre = etNombreDetalle.getText().toString();
        String correo = etCorreoDetalle.getText().toString();
        String edadStr = etEdadDetalle.getText().toString();

        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = adminDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("edad", Integer.parseInt(edadStr));

        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int count = db.update("usuarios", values, selection, selectionArgs);
        db.close();

        if (count > 0) {
            Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();

            // Actualizamos el nombre en SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(LoginActivity.KEY_USER_NAME, nombre);
            editor.apply();

            finish(); // ¡Importante! Cerramos esta activity y volvemos al Perfil
        } else {
            Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
        }
    }

        //Muestra un diálogo de Alerta para confirmar la eliminación.
        private void mostrarDialogoConfirmacion() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmar Eliminación");
            builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.");

            // Botón "Confirmar"
            builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Si confirma, llamamos al método para eliminar
                    eliminarUsuario(loggedInUserId);
                }
            });

            // Botón "Cancelar"
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Si cancela, solo cerramos el diálogo
                    dialog.dismiss();
                }
            });

            // Creamos y mostramos el diálogo
            AlertDialog dialog = builder.create();
            dialog.show();
        }


         //Lógica para Eliminar (DELETE) el usuario de la BD.
         //Adaptado del PDF [cite: 323]

        private void eliminarUsuario(int id) {
            SQLiteDatabase db = adminDB.getWritableDatabase(); // [cite: 324]

            // Cláusula WHERE para eliminar solo el ID correcto [cite: 326]
            String selection = "id = ?";
            String[] selectionArgs = { String.valueOf(id) }; // [cite: 327]

            // Ejecutamos el delete [cite: 328]
            int deletedRows = db.delete("usuarios", selection, selectionArgs); // [cite: 328]

            db.close(); // [cite: 329]

            if (deletedRows > 0) { // [cite: 330]
                Toast.makeText(this, "Cuenta eliminada con éxito", Toast.LENGTH_SHORT).show(); // [cite: 331]
                // Si se eliminó, también cerramos la sesión y redirigimos
                limpiarSesionYRedirigir();
            } else {
                Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show(); // [cite: 333]
            }
        }

        /**
         * Limpia SharedPreferences y redirige al Login.
         */
        private void limpiarSesionYRedirigir() {
            // Limpiamos SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Enviamos al usuario a LoginActivity
            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
            // Estas flags limpian el historial de actividades
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }