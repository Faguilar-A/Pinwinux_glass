package com.aguilar.pinwinglass;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import db.AdminSQLiteOpenHelper;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etCorreo, etEdad, etPassword;
    Button btnGuardar;
    TextView tvIrALogin;
    AdminSQLiteOpenHelper adminDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etEdad = findViewById(R.id.etEdad);
        etPassword = findViewById(R.id.etPassword);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvIrALogin = findViewById(R.id.tvIrALogin);

        // Inicializamos el Helper de la BD
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        tvIrALogin.setOnClickListener(v -> finish()); // Cierra esta actividad y vuelve a Login
    }

    private void registrarUsuario() {
        // Abrimos BD en modo escritura
        SQLiteDatabase db = adminDB.getWritableDatabase();

        String nombre = etNombre.getText().toString();
        String correo = etCorreo.getText().toString();
        String edadStr = etEdad.getText().toString();
        String password = etPassword.getText().toString();

        // Validamos campos vacíos
        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);

        // Usamos ContentValues para insertar los datos
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("edad", edad);
        values.put("password", password);

        // Insertamos el nuevo registro
        try {
            long newRowId = db.insertOrThrow("usuarios", null, values);

            // Verificamos si se insertó correctamente
            if (newRowId != -1) {
                Toast.makeText(this, "Usuario registrado con éxito (ID: " + newRowId + ")", Toast.LENGTH_SHORT).show();
                finish(); // Cerramos la actividad y volvemos a Login
            }
        } catch (Exception e) {
            // Esto probablemente ocurra si el correo ya existe (UNIQUE constraint)
            Toast.makeText(this, "Error al registrar el usuario. El correo ya podría estar en uso.", Toast.LENGTH_SHORT).show();
        } finally {
            db.close(); // Cerramos la base de datos
        }
    }
}