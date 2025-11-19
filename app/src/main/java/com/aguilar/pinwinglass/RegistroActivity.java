package com.aguilar.pinwinglass;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import db.AdminSQLiteOpenHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etCorreo, etEdad, etPassword;
    Button btnGuardar;
    TextView tvIrALogin;

    // Variables para Base de Datos
    AdminSQLiteOpenHelper adminDB;
    FirebaseFirestore dbFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etEdad = findViewById(R.id.etEdad);
        etPassword = findViewById(R.id.etPassword);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvIrALogin = findViewById(R.id.tvIrALogin);

        // Inicializar Bases de Datos
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        dbFirestore = FirebaseFirestore.getInstance();

        // Listener del botón Guardar
        btnGuardar.setOnClickListener(v -> registrarUsuario());

        // Volver al login
        tvIrALogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        // 1. Obtener datos
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 2. Validación
        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);

        // 3. Guardar en SQLite (Local)
        SQLiteDatabase db = adminDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("edad", edad);
        values.put("password", password);

        try {
            long newRowId = db.insertOrThrow("usuarios", null, values);

            if (newRowId != -1) {
                // 4. Si se guardó en local, guardar en Firebase (Nube)
                guardarEnFirebase(nombre, correo, edad);

                Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad y vuelve al Login
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: El correo ya podría estar registrado", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void guardarEnFirebase(String nombre, String correo, int edad) {
        // Usamos la clase modelo Usuario
        Usuario nuevoUsuario = new Usuario(nombre, correo, edad);

        dbFirestore.collection("usuarios")
                .add(nuevoUsuario)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Firebase: Usuario agregado ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    System.out.println("Firebase Error: " + e.getMessage());
                });
    }
}