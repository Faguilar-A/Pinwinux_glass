package com.aguilar.pinwinglass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView; // CAMBIO: De EditText a TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import db.AdminSQLiteOpenHelper;

public class ProfileActivity extends AppCompatActivity {

    // CAMBIO: Estos ahora son TextViews
    TextView tvUserName, tvUserEmail, tvUserEdad;
    Button btnEditarPerfil, btnLogout; // CAMBIO: btnActualizar -> btnEditarPerfil
    AdminSQLiteOpenHelper adminDB;
    int loggedInUserId;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // CAMBIO: Vinculamos los TextViews y el nuevo botón
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserEdad = findViewById(R.id.tvUserEdad);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil); // CAMBIO
        btnLogout = findViewById(R.id.btnLogout);

        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        loggedInUserId = sharedPreferences.getInt(LoginActivity.KEY_USER_ID, 0);

        if (loggedInUserId <= 0) {
            Toast.makeText(this, "Error: No se encontró usuario", Toast.LENGTH_SHORT).show();
            finish();
        }

        // CAMBIO: El listener ahora abre la EditProfileActivity
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // El listener de Logout sigue igual
        btnLogout.setOnClickListener(v -> cerrarSesion());
    }

    // MEJORA: Usamos onResume()
    // Este método se llama CADA VEZ que la pantalla se vuelve visible
    // (Incluso cuando volvemos de EditProfileActivity)
    @Override
    protected void onResume() {
        super.onResume();
        // Cargamos (o recargamos) los datos del usuario
        if (loggedInUserId > 0) {
            cargarDatosUsuario(loggedInUserId);
        }
    }

    // Método para cargar datos (READ)
    private void cargarDatosUsuario(int id) {
        SQLiteDatabase db = adminDB.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre, correo, edad FROM usuarios WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            // CAMBIO: Poblamos los TextViews
            tvUserName.setText("Nombre: " + cursor.getString(0));
            tvUserEmail.setText("Email: " + cursor.getString(1));
            tvUserEdad.setText("Edad: " + cursor.getInt(2));
        }
        cursor.close();
        db.close();
    }

    // CAMBIO: El método actualizarUsuario() se ha MOVIDO a EditProfileActivity

    // Método para Cerrar Sesión (Sigue igual)
    private void cerrarSesion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}