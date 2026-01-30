package com.sybven.qrdecoder;

import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class DecoderTest {

    @Test
    public void testDecode_Successful() throws IOException {

        String base64Image = loadResourceAsString();
        String jsonResponse = Decoder.decode(base64Image);

        if (jsonResponse.contains("Código QR decodificado con éxito")) {
            System.out.println("¡Decodificación Exitosa! Respuesta JSON:");
            System.out.println(jsonResponse);
            assertTrue(true);
        } else {
            System.err.println("La decodificación falló. Respuesta recibida:");
            System.err.println(jsonResponse);
            fail("La decodificación no fue exitosa.");
        }
    }

    /**
     * Este es un métod0 de ayuda para leer un archivo de la carpeta 'resources'
     * y devolver su contenido como una única cadena de texto.
     *
     * @return El contenido del archivo como un String.
     * @throws IOException Si el archivo no se encuentra.
     */
    private String loadResourceAsString() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("qr_test_base64.txt")) {
            if (is == null) {
                throw new IOException("Archivo de recurso no encontrado: " + "qr_test_base64.txt");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
