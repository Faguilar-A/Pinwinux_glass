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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DetectionActivity extends AppCompatActivity {

    TextView tvDistance;
    EditText etIpAddress;
    Button btnStartDetection, btnStopDetection, btnBackToMain;

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Thread networkThread;
    private volatile boolean isRunning = false;
    private Handler uiHandler;

    private SharedPreferences settingsPrefs;
    private AdminSQLiteOpenHelper adminDB;
    public static final String KEY_LAST_IP = "LastIP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deteccion);

        tvDistance = findViewById(R.id.tvDistance);
        etIpAddress = findViewById(R.id.etIpAddress);
        btnStartDetection = findViewById(R.id.btnStartDetection);
        btnStopDetection = findViewById(R.id.btnStopDetection);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        uiHandler = new Handler(Looper.getMainLooper());
        settingsPrefs = getSharedPreferences(SettingsActivity.SETTINGS_PREFS, MODE_PRIVATE);
        adminDB = new AdminSQLiteOpenHelper(this, "Pinwinux.db", null, 1);

        // Cargar la última IP usada
        etIpAddress.setText(settingsPrefs.getString(KEY_LAST_IP, ""));

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

        // Guardar la IP para la próxima vez
        settingsPrefs.edit().putString(KEY_LAST_IP, ip).apply();

        isRunning = true;
        networkThread = new Thread(new ClientTask(ip));
        networkThread.start();
        Toast.makeText(this, "Iniciando conexión...", Toast.LENGTH_SHORT).show();
    }

    private void stopDetection() {
        if (!isRunning) {
            Toast.makeText(this, "La detección no está iniciada", Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = false;
        if (networkThread != null) {
            networkThread.interrupt(); // Interrumpe el hilo
        }

        // Cierra el socket en un hilo separado para evitar NetworkOnMainThreadException
        new Thread(() -> {
            try {
                if (output != null) {
                    output.println("STOP"); // Envía comando STOP
                    output.flush();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        uiHandler.post(() -> tvDistance.setText("---"));
        Toast.makeText(this, "Detección detenida", Toast.LENGTH_SHORT).show();
    }

    // Tarea de red que corre en un hilo separado
    class ClientTask implements Runnable {
        private String ip;
        private final int PORT = 8080;

        ClientTask(String ip) {
            this.ip = ip;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, PORT);
                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                uiHandler.post(() -> Toast.makeText(DetectionActivity.this, "¡Conectado!", Toast.LENGTH_SHORT).show());

                // 1. Enviar configuración al ESP
                sendSettings();

                // 2. Escuchar distancias
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    String distanceStr = input.readLine();
                    if (distanceStr == null) {
                        // El servidor cerró la conexión
                        throw new Exception("Servidor desconectado.");
                    }

                    // Actualizar UI y guardar en BD
                    uiHandler.post(() -> tvDistance.setText(distanceStr + " cm"));
                    saveToHistory(distanceStr);
                }

            } catch (Exception e) {
                if (isRunning) { // Solo muestra error si no fue detenido manualmente
                    e.printStackTrace();
                    uiHandler.post(() -> Toast.makeText(DetectionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            } finally {
                // Limpieza al final
                try {
                    if (socket != null) socket.close();
                    output = null;
                    input = null;
                    isRunning = false; // Asegura que el estado esté detenido
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendSettings() {
        // Carga la configuración guardada
        int alertModeId = settingsPrefs.getInt(SettingsActivity.KEY_ALERT_MODE, R.id.rbSound);
        String maxDist = settingsPrefs.getString(SettingsActivity.KEY_MAX_DIST, "125");
        String minDist = settingsPrefs.getString(SettingsActivity.KEY_MIN_DIST, "5");
        String panicDist = settingsPrefs.getString(SettingsActivity.KEY_PANIC_DIST, "30");

        // Traduce el ID del RadioButton al modo del Arduino (0, 1, 2)
        int mode = 0; // 0=Sonido por defecto
        if (alertModeId == R.id.rbVibration) {
            mode = 1; // 1=Vibración
        } else if (alertModeId == R.id.rbBoth) {
            mode = 2; // 2=Ambos
        }

        // Envía los comandos al ESP
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
            SQLiteDatabase db = adminDB.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("distancia", dist);
            // La fecha se inserta automáticamente por el DEFAULT CURRENT_TIMESTAMP
            db.insert("detecciones", null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace(); // Error al parsear o guardar
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDetection(); // Asegura que todo se detenga al salir
    }
}