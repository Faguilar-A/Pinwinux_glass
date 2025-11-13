package com.aguilar.pinwinglass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_PREFS = "PinwinuxSettings";
    public static final String KEY_ALERT_MODE = "AlertMode";
    public static final String KEY_MAX_DIST = "MaxDist";
    public static final String KEY_MIN_DIST = "MinDist";
    public static final String KEY_PANIC_DIST = "PanicDist";

    RadioGroup rgAlertType;
    EditText etMaxDistance, etMinDistance, etPanicThreshold;
    Button btnSaveSettings, btnBack;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        rgAlertType = findViewById(R.id.rgAlertType);
        etMaxDistance = findViewById(R.id.etMaxDistance);
        etMinDistance = findViewById(R.id.etMinDistance);
        etPanicThreshold = findViewById(R.id.etPanicThreshold);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnBack = findViewById(R.id.btnBackToMainFromSettings);

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE);

        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        // Cargar Modo Alerta
        int alertModeId = sharedPreferences.getInt(KEY_ALERT_MODE, R.id.rbSound);
        rgAlertType.check(alertModeId);

        // Cargar Distancias (con valores por defecto)
        String maxDist = sharedPreferences.getString(KEY_MAX_DIST, "125");
        String minDist = sharedPreferences.getString(KEY_MIN_DIST, "5");
        String panicDist = sharedPreferences.getString(KEY_PANIC_DIST, "30");

        etMaxDistance.setText(maxDist);
        etMinDistance.setText(minDist);
        etPanicThreshold.setText(panicDist);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Guardar Modo Alerta
        editor.putInt(KEY_ALERT_MODE, rgAlertType.getCheckedRadioButtonId());

        // Guardar Distancias
        editor.putString(KEY_MAX_DIST, etMaxDistance.getText().toString());
        editor.putString(KEY_MIN_DIST, etMinDistance.getText().toString());
        editor.putString(KEY_PANIC_DIST, etPanicThreshold.getText().toString());

        editor.apply();

        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        finish();
    }
}