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

    // Usaremos SharedPreferences para guardar la sesión del usuario
    SharedPreferences sharedPreferences;
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
        SQLiteDatabase db = adminDB.getReadableDatabase(); // [cite: 188]
        String correo = etCorreoLogin.getText().toString();
        String password = etPasswordLogin.getText().toString();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hacemos la consulta [cite: 189]
        Cursor cursor = db.rawQuery("SELECT id, nombre FROM usuarios WHERE correo = ? AND password = ?",
                new String[]{correo, password});

        // Verificamos si el cursor encontró un resultado [cite: 191]
        if (cursor.moveToFirst()) {
            // Usuario y contraseña correctos
            int userId = cursor.getInt(0); // [cite: 194]
            String userName = cursor.getString(1); // [cite: 197]

            // Guardamos la sesión
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_USER_ID, userId);
            editor.putString(KEY_USER_NAME, userName);
            editor.apply();

            Toast.makeText(this, "¡Bienvenido, " + userName + "!", Toast.LENGTH_SHORT).show();

            // Vamos a MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cerramos LoginActivity

        } else {
            // Credenciales incorrectas
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }

        cursor.close(); // [cite: 201]
        db.close(); // [cite: 202]
    }
}