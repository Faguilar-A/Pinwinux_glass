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
    AdminSQLiteOpenHelper adminDB; // [cite: 365]

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

        // Inicializamos el Helper de la BD [cite: 365-366]
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);

        btnGuardar.setOnClickListener(new View.OnClickListener() { // [cite: 339]
            @Override
            public void onClick(View v) {
                registrarUsuario(); // [cite: 361]
            }
        });

        tvIrALogin.setOnClickListener(v -> finish()); // Cierra esta actividad y vuelve a Login
    }

    private void registrarUsuario() { // [cite: 364]
        // Abrimos BD en modo escritura [cite: 367]
        SQLiteDatabase db = adminDB.getWritableDatabase();

        String nombre = etNombre.getText().toString(); // [cite: 369]
        String correo = etCorreo.getText().toString();
        String edadStr = etEdad.getText().toString();
        String password = etPassword.getText().toString();

        // Validamos campos vacíos [cite: 373]
        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show(); // [cite: 374]
            return; // [cite: 376]
        }

        int edad = Integer.parseInt(edadStr);

        // Usamos ContentValues para insertar los datos [cite: 343]
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("edad", edad);
        values.put("password", password);

        // Insertamos el nuevo registro [cite: 344]
        try {
            long newRowId = db.insertOrThrow("usuarios", null, values); // [cite: 345]

            // Verificamos si se insertó correctamente
            if (newRowId != -1) { // [cite: 347]
                Toast.makeText(this, "Usuario registrado con éxito (ID: " + newRowId + ")", Toast.LENGTH_SHORT).show(); // [cite: 348]
                finish(); // Cerramos la actividad y volvemos a Login [cite: 354]
            }
        } catch (Exception e) {
            // Esto probablemente ocurra si el correo ya existe (UNIQUE constraint)
            Toast.makeText(this, "Error al registrar el usuario. El correo ya podría estar en uso.", Toast.LENGTH_SHORT).show();
        } finally {
            db.close(); // Cerramos la base de datos [cite: 346]
        }
    }
}