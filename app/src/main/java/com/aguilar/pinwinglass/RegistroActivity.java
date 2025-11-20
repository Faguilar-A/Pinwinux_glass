package com.aguilar.pinwinglass;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

    // Variables para las Bases de Datos
    AdminSQLiteOpenHelper adminDB; // SQLite (Local)
    FirebaseFirestore dbFirestore; // Firebase (Nube)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vinculación de la interfaz
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etEdad = findViewById(R.id.etEdad);
        etPassword = findViewById(R.id.etPassword);
        btnGuardar = findViewById(R.id.btnGuardar);
        tvIrALogin = findViewById(R.id.tvIrALogin);

        // Inicialización de Bases de Datos
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        dbFirestore = FirebaseFirestore.getInstance();

        // Configuración de botones
        btnGuardar.setOnClickListener(v -> registrarUsuario());
        tvIrALogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        // Obtener datos de los campos de texto
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();
        String passwordPlana = etPassword.getText().toString().trim();

        // Validar que no haya campos vacíos
        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty() || passwordPlana.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);

        // --- PASO CLAVE DE SEGURIDAD ---
        // Convertimos "123456" en "8d969eef6ecad3c..."
        String passwordProtegida = Seguridad.hashPassword(passwordPlana);

        // --- GUARDADO EN SQLITE (LOCAL) ---
        SQLiteDatabase db = adminDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("edad", edad);
        values.put("password", passwordProtegida); // Guardamos el Hash, no el texto plano

        try {
            // insertOrThrow lanza una excepción si hay error (ej: correo duplicado)
            long newRowId = db.insertOrThrow("usuarios", null, values);

            if (newRowId != -1) {
                // Si se guardó bien en el celular, procedemos a guardar en la nube
                // Pasamos también la passwordProtegida
                guardarEnFirebase(nombre, correo, edad, passwordProtegida);

                Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Cerramos y volvemos al Login
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: El correo ya podría estar registrado", Toast.LENGTH_SHORT).show();
        } finally {
            db.close(); // Siempre cerramos la conexión local
        }
    }

    // Método para subir a la nube
    private void guardarEnFirebase(String nombre, String correo, int edad, String passwordHash) {
        // Creamos el objeto Usuario usando el NUEVO constructor con password
        Usuario nuevoUsuario = new Usuario(nombre, correo, edad, passwordHash);

        // Subimos a la colección "usuarios"
        dbFirestore.collection("usuarios")
                .add(nuevoUsuario)
                .addOnSuccessListener(documentReference -> {
                    // Esto se ejecuta si sube bien (puedes ver el log en Logcat)
                    System.out.println("Firebase: Usuario agregado con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Esto se ejecuta si falla (ej: sin internet)
                    System.out.println("Firebase Error: " + e.getMessage());
                });
    }
}