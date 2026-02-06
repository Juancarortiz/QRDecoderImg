package com.sybven.qrdecoder;

import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;
import org.json.JSONObject;

public class QRServiceWrapperTest {

    /**
     * Test que valida la lógica de decodificación.
     * Es dinámico: lee la imagen del archivo, la procesa y muestra el resultado en consola.
     */
    @Test
    public void testWrapperLogicIsCorrect() throws Exception {
        // 1. Cargar la imagen desde el archivo de recursos
        // Asegúrate de que el archivo src/test/resources/qr_test_base64.txt exista
        String base64QRImage = loadResourceAsString("qr_test_base64.txt");

        System.out.println("=== Iniciando Prueba de Lógica del Wrapper ===");

        // 2. Ejecutar la decodificación llamando a la clase Decoder
        String jsonResponseString = Decoder.decode(base64QRImage);

        // Validar que la respuesta no sea nula
        assertNotNull("La respuesta del Decoder no debería ser nula", jsonResponseString);

        // 3. Parsear la respuesta JSON para validar y mostrar resultados
        JSONObject json = new JSONObject(jsonResponseString);

        // Obtenemos el código (el Decoder lo devuelve como String según el último cambio)
        String code = json.getString("code");
        String message = json.getString("message");

        System.out.println("STATUS CODE: " + code);
        System.out.println("MENSAJE: " + message);

        // 4. Si el código es 200, la prueba es exitosa y mostramos la data
        if ("200".equals(code)) {
            String dataEncontrada = json.getString("data");
            System.out.println("CONTENIDO ENCONTRADO: " + dataEncontrada);

            // Verificación final del test
            assertEquals("El código de respuesta debe ser 200", "200", code);
            assertFalse("La data no debería estar vacía", dataEncontrada.isEmpty());

            System.out.println("=== Prueba Exitosa ===");
        } else {
            // Si falla, imprimimos el error para depurar
            System.err.println("FALLO EN DECODIFICACIÓN: " + message);
            fail("El Decoder no pudo extraer la información del QR. Respuesta: " + jsonResponseString);
        }
    }

    /**
     * Método de utilidad para leer el archivo .txt de la carpeta src/test/resources
     */
    private String loadResourceAsString(String resourceName) throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Archivo de recurso no encontrado: " + resourceName +
                        ". Asegúrate de que esté en src/test/resources/");
            }
            // Usamos readAllBytes para leer todo el contenido del archivo (Java 9+)
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }
}