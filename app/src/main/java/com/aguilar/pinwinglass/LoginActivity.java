package com.aguilar.pinwinglass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import db.AdminSQLiteOpenHelper;

public class LoginActivity extends AppCompatActivity {

    EditText etCorreoLogin, etPasswordLogin;
    Button btnIngresar;
    TextView tvIrARegistro;
    AdminSQLiteOpenHelper adminDB;
    SharedPreferences sharedPreferences;

    // Constantes para SharedPreferences
    public static final String PREFS_NAME = "PinwinuxGlassPrefs";
    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_USER_NAME = "USER_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCorreoLogin = findViewById(R.id.etCorreoLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);

        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        btnIngresar.setOnClickListener(v -> loginUsuario());

        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void loginUsuario() {
        SQLiteDatabase db = adminDB.getReadableDatabase(); // Abrimos en modo lectura

        String correo = etCorreoLogin.getText().toString().trim();
        String passwordIngresada = etPasswordLogin.getText().toString().trim();

        if (correo.isEmpty() || passwordIngresada.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- PASO CLAVE ---
        // El usuario escribió "123456", pero en la BD tenemos "8d969..."
        // Así que encriptamos lo que escribió para ver si el resultado coincide.
        String passwordHashAComparar = Seguridad.hashPassword(passwordIngresada);

        // Consulta SQL: Buscamos donde coincida el correo Y el hash de la contraseña
        Cursor cursor = db.rawQuery("SELECT id, nombre FROM usuarios WHERE correo = ? AND password = ?",
                new String[]{correo, passwordHashAComparar});

        if (cursor.moveToFirst()) {
            // ¡Coincidencia encontrada! Login exitoso
            int userId = cursor.getInt(0);
            String userName = cursor.getString(1);

            // Guardamos la sesión
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_USER_ID, userId);
            editor.putString(KEY_USER_NAME, userName);
            editor.apply();

            Toast.makeText(this, "¡Bienvenido, " + userName + "!", Toast.LENGTH_SHORT).show();

            // Vamos al Main
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // No coincide
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
}