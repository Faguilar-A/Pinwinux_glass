package com.aguilar.pinwinglass;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import db.AdminSQLiteOpenHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetectionActivity extends AppCompatActivity {

    // UI Vars
    TextView tvDistance;
    EditText etIpAddress;
    Button btnStartDetection, btnStopDetection, btnBackToMain;

    // Networking Vars
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Thread networkThread;
    private volatile boolean isRunning = false;
    private Handler uiHandler;

    // DB & Settings Vars
    private SharedPreferences settingsPrefs;
    private AdminSQLiteOpenHelper adminDB;
    private FirebaseFirestore dbFirestore;
    public static final String KEY_LAST_IP = "LastIP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deteccion);

        // Vincular vistas
        tvDistance = findViewById(R.id.tvDistance);
        etIpAddress = findViewById(R.id.etIpAddress);
        btnStartDetection = findViewById(R.id.btnStartDetection);
        btnStopDetection = findViewById(R.id.btnStopDetection);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        // Inicializar herramientas
        uiHandler = new Handler(Looper.getMainLooper());
        settingsPrefs = getSharedPreferences(SettingsActivity.SETTINGS_PREFS, MODE_PRIVATE);
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);
        dbFirestore = FirebaseFirestore.getInstance(); // Inicializar Firebase

        // Cargar última IP
        etIpAddress.setText(settingsPrefs.getString(KEY_LAST_IP, ""));

        // Listeners
        btnStartDetection.setOnClickListener(v -> startDetection());
        btnStopDetection.setOnClickListener(v -> stopDetection());
        btnBackToMain.setOnClickListener(v -> finish());
    }

    private void startDetection() {
        if (isRunning) {
            Toast.makeText(this, "La detección ya está iniciada", Toast.LENGTH_SHORT).show();
            return;
        }

        String ip = etIpAddress.getText().toString();
        if (ip.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa la IP del dispositivo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar IP
        settingsPrefs.edit().putString(KEY_LAST_IP, ip).apply();

        isRunning = true;
        networkThread = new Thread(new ClientTask(ip));
        networkThread.start();
        Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();
    }

    private void stopDetection() {
        if (!isRunning) return;

        isRunning = false;
        if (networkThread != null) networkThread.interrupt();

        new Thread(() -> {
            try {
                if (output != null) {
                    output.println("STOP");
                    output.flush();
                }
                if (socket != null) socket.close();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        uiHandler.post(() -> tvDistance.setText("---"));
        Toast.makeText(this, "Detección detenida", Toast.LENGTH_SHORT).show();
    }

    // Tarea en segundo plano para la conexión Wi-Fi
    class ClientTask implements Runnable {
        private String ip;
        private final int PORT = 8080;

        ClientTask(String ip) { this.ip = ip; }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                uiHandler.post(() -> Toast.makeText(DetectionActivity.this, "¡Conectado!", Toast.LENGTH_SHORT).show());

                sendSettings(); // Enviar configuración al ESP

                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    String distanceStr = input.readLine();
                    if (distanceStr == null) break;

                    // Actualizar UI
                    uiHandler.post(() -> tvDistance.setText(distanceStr + " cm"));

                    // Guardar en Local y Nube
                    saveToHistory(distanceStr);
                }
            } catch (Exception e) {
                if (isRunning) {
                    e.printStackTrace();
                    uiHandler.post(() -> Toast.makeText(DetectionActivity.this, "Error conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            } finally {
                isRunning = false;
            }
        }
    }

    private void sendSettings() {
        int alertModeId = settingsPrefs.getInt(SettingsActivity.KEY_ALERT_MODE, R.id.rbSound);
        String maxDist = settingsPrefs.getString(SettingsActivity.KEY_MAX_DIST, "125");
        String minDist = settingsPrefs.getString(SettingsActivity.KEY_MIN_DIST, "5");
        String panicDist = settingsPrefs.getString(SettingsActivity.KEY_PANIC_DIST, "30");

        int mode = 0;
        if (alertModeId == R.id.rbVibration) mode = 1;
        else if (alertModeId == R.id.rbBoth) mode = 2;

        if (output != null) {
            output.println("SET:MAX_DISTANCE:" + maxDist);
            output.println("SET:MIN_DISTANCE:" + minDist);
            output.println("SET:PANIC_THRESHOLD:" + panicDist);
            output.println("SET:MODE:" + mode);
            output.flush();
        }
    }

    private void saveToHistory(String distance) {
        try {
            double dist = Double.parseDouble(distance);

            // 1. Guardar en SQLite
            SQLiteDatabase db = adminDB.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("distancia", dist);
            // SQLite pone la fecha automática
            db.insert("detecciones", null, values);
            db.close();

            // 2. Guardar en Firebase
            uploadDetectionToFirebase(dist);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadDetectionToFirebase(double distancia) {
        // Creamos fecha manual para Firebase
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Usamos la clase modelo Deteccion
        Deteccion nuevaDeteccion = new Deteccion(distancia, fechaActual);

        dbFirestore.collection("detecciones")
                .add(nuevaDeteccion)
                .addOnSuccessListener(docRef -> System.out.println("Firebase Detección ID: " + docRef.getId()))
                .addOnFailureListener(e -> System.out.println("Firebase Error: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDetection();
    }
}