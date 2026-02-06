package com.sybven.qrdecoder;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Clase motor de decodificación de códigos QR.
 * Optimizada para tokens de alta densidad, imágenes con ruido y reparación de Base64 corrupto.
 */
public final class Decoder {
    private static final QRCodeMultiReader multiReader = new QRCodeMultiReader();

    private Decoder() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Decodifica una imagen en formato Base64.
     * @param base64Image Cadena Base64 de la imagen.
     * @return JSON string con el resultado.
     */
    public static String decode(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return errorResponse(400, "No se recibió ninguna imagen.");
        }

        try {
            String cleanBase64 = cleanBase64String(base64Image);

            if (cleanBase64.isEmpty()) {
                return errorResponse(400, "La cadena Base64 no es válida tras la limpieza.");
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (originalImage == null) {
                return errorResponse(400, "No se pudo procesar la imagen (formato no soportado o corrupto).");
            }

            Result result = tryDecode(originalImage);

            if (result == null) {
                result = tryDecode(upscaleImage(originalImage, 2.0));
            }

            if (result == null) {
                BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = grayImage.createGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.dispose();
                result = tryDecode(upscaleImage(grayImage, 1.5));
            }

            if (result != null) {
                return successResponse(result.getText());
            } else {
                return errorResponse(404, "No se detectó ningún código QR. Verifique la nitidez de la imagen.");
            }

        } catch (IllegalArgumentException e) {
            return errorResponse(400, "Error de formato Base64: Longitud incorrecta o caracteres inválidos.");
        } catch (IOException e) {
            return errorResponse(400, "Error al leer los datos de la imagen: " + e.getMessage());
        } catch (Exception e) {
            return errorResponse(500, "Error interno: " + e.getMessage());
        }
    }

    /**
     * Intenta extraer el texto de un QR usando el motor MultiReader.
     */
    private static Result tryDecode(BufferedImage img) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(img);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));

            Result[] results = multiReader.decodeMultiple(bitmap, hints);
            if (results != null && results.length > 0) {
                Result bestResult = results[0];
                for (Result r : results) {
                    if (r.getText().length() > bestResult.getText().length()) {
                        bestResult = r;
                    }
                }
                return bestResult;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Limpia la cadena Base64 y repara daños causados por URL-Encoding (espacios por +).
     */
    private static String cleanBase64String(String base64) {
        if (base64 == null) return "";

        if (base64.contains(",")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }

        base64 = base64.replace(" ", "+");

        return base64.replaceAll("[^A-Za-z0-9+/=]", "");
    }

    /**
     * Agranda la imagen usando interpolación bicúbica para mejorar la detección.
     */
    private static BufferedImage upscaleImage(BufferedImage src, double factor) {
        int w = (int) (src.getWidth() * factor);
        int h = (int) (src.getHeight() * factor);

        BufferedImage zoomed = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = zoomed.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return zoomed;
    }

    private static String successResponse(String data) {
        JSONObject json = new JSONObject();
        json.put("code", "200");
        json.put("message", "Código QR decodificado con éxito");
        json.put("data", data);
        return json.toString();
    }

    private static String errorResponse(int code, String message) {
        JSONObject json = new JSONObject();
        json.put("code", String.valueOf(code));
        json.put("message", message);
        return json.toString();
    }
}