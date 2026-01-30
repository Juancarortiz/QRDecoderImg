package com.sybven.qrdecoder;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public final class Decoder {
    private static final MultiFormatReader reader = initReader();

    private Decoder() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Decodifica una imagen en formato Base64 para encontrar un código QR.
     *
     * @param base64Image La cadena de la imagen en formato Base64.
     * @return Una cadena JSON con el resultado de la operación.
     */
    public static String decode(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return errorResponse(400, "No se recibió ninguna imagen.");
        }

        try {
            BufferedImage image = decodeBase64ToImage(base64Image);

            if (image == null) {
                return errorResponse(400, "No se pudo decodificar la cadena Base64 a una imagen.");
            }

            Result result = decodeQRCode(image);
            return successResponse(result.getText());

        } catch (IllegalArgumentException e) {
            return errorResponse(400, "El formato de la imagen no es válido.");
        } catch (NotFoundException e) {
            return errorResponse(404, "Código QR no encontrado en la imagen.");
        } catch (IOException e) {
            return errorResponse(400, "Error al leer los datos de la imagen.");
        } catch (ReaderException e) {
            return errorResponse(400, "Código QR dañado o ilegible.");
        } catch (Exception e) {
            return errorResponse(500, "Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Convierte una cadena Base64 en un objeto BufferedImage.
     * Usa java.util.Base64 y javax.imageio.ImageIO.
     */
    private static BufferedImage decodeBase64ToImage(String base64Image) throws IOException, IllegalArgumentException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Decodifica el QR desde un objeto BufferedImage.
     * Usa la clase de ayuda BufferedImageLuminanceSource de la librería ZXing.
     */
    private static Result decodeQRCode(BufferedImage image) throws ReaderException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        return reader.decode(binaryBitmap);
    }

    private static MultiFormatReader initReader() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        MultiFormatReader r = new MultiFormatReader();
        r.setHints(hints);
        return r;
    }

    private static String successResponse(String data) {
        return createJsonResponse(200, "Código QR decodificado con éxito", data).toString();
    }

    private static String errorResponse(int code, String message) {
        return createJsonResponse(code, message, null).toString();
    }

    private static JSONObject createJsonResponse(int code, String message, String data) {
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse.put("code", code);
            jsonResponse.put("message", message);
            if (data != null) {
                jsonResponse.put("data", data);
            }
        } catch (JSONException e) {
            try {
                JSONObject errorJson = new JSONObject();
                errorJson.put("code", 500);
                errorJson.put("message", "Error interno al generar la respuesta JSON.");
                return errorJson;
            } catch (JSONException je) {
                return new JSONObject();
            }
        }
        return jsonResponse;
    }
}
