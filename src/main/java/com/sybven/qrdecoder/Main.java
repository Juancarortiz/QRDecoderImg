package com.sybven.qrdecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        System.out.println("Iniciando decodificador de QR...");

        if (args.length == 0) {
            System.err.println("Error: Debes proporcionar la ruta a un archivo que contenga la imagen en formato Base64.");
            System.err.println("Uso: java -jar QRDecoderImg-1.0-SNAPSHOT.jar <ruta_del_archivo>");
            return;
        }

        String filePath = args[0];
        System.out.println("Leyendo archivo: " + filePath);

        try {

            String base64Image = new String(Files.readAllBytes(Paths.get(filePath)));
            String jsonResponse = Decoder.decode(base64Image.trim());
            System.out.println("Resultado de la decodificación:");
            System.out.println(jsonResponse);

        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}
