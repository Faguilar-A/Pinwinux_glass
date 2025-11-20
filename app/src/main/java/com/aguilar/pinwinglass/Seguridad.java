package com.aguilar.pinwinglass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad para funciones de seguridad.
 * Se encarga de encriptar (hashear) las contraseñas.
 */
public class Seguridad {

    // Método estático para convertir una contraseña legible en un Hash SHA-256
    public static String hashPassword(String password) {
        try {
            // 1. Instanciamos el algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 2. Convertimos el texto a bytes y aplicamos el hash
            byte[] hash = digest.digest(password.getBytes());

            // 3. Convertimos el resultado (bytes) a formato Hexadecimal (String)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            // Retornamos el código encriptado (ej: "a591a6...")
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Retorna null si el algoritmo no existe en el teléfono
        }
    }
}