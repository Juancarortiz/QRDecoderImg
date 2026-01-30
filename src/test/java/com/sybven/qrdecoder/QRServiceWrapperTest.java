package com.sybven.qrdecoder;

import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Result;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class QRServiceWrapperTest {

    private String loadResourceAsString(String resourceName) throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Archivo de recurso no encontrado: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    @Test
    public void testWrapperLogicIsCorrect() throws Exception {

        String base64QRImage = loadResourceAsString("qr_test_base64.txt");

        String jsonResponseString = Decoder.decode(base64QRImage);

        assertNotNull("La respuesta JSON no debería ser nula.", jsonResponseString);

        System.out.println("¡Prueba de lógica de negocio exitosa!");
        System.out.println("Respuesta JSON generada: " + jsonResponseString);

        String expectedData = "6234021358412-27791570613V000026825605800801040105";

        assertTrue("La respuesta debe contener el código 200.", jsonResponseString.contains("\"code\":200"));
        assertTrue("La respuesta debe contener el mensaje de éxito.", jsonResponseString.contains("Código QR decodificado con éxito"));
        assertTrue("La respuesta debe contener los datos decodificados.", jsonResponseString.contains(expectedData));
    }
}
