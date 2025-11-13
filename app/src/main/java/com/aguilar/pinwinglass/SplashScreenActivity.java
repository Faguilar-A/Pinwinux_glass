package com.aguilar.pinwinglass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int SPLASH_TIME_OUT = 3000;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Revisamos las SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
                int userId = sharedPreferences.getInt(LoginActivity.KEY_USER_ID, -1);

                Intent intent;
                if (userId != -1) {
                    // Usuario ya logueado, vamos a MainActivity
                    intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                } else {
                    // No hay sesión, vamos a LoginActivity
                    intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish(); // Cerrar esta actividad
            }
        }, SPLASH_TIME_OUT);
    }
}