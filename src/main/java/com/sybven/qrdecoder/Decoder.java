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

public final class Decoder {
    private static final QRCodeMultiReader multiReader = new QRCodeMultiReader();

    private Decoder() {}

    public static String decode(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return errorResponse(400, "No se recibió imagen.");
        }

        try {
            String cleanBase64 = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
            cleanBase64 = cleanBase64.replaceAll("\\s", "");
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (originalImage == null) return errorResponse(400, "Imagen inválida.");

            Result result = tryDecode(originalImage);

            if (result == null) {
                result = tryDecode(upscaleImage(originalImage, 2.0));
            }

            if (result == null) {
                BufferedImage gray = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = gray.createGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.dispose();
                result = tryDecode(upscaleImage(gray, 1.5));
            }

            if (result != null) {
                return successResponse(result.getText());
            } else {
                return errorResponse(404, "QR no detectado.");
            }

        } catch (Exception e) {
            return errorResponse(500, "Error: " + e.getMessage());
        }
    }

    private static Result tryDecode(BufferedImage img) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(img);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));

            Result[] results = multiReader.decodeMultiple(bitmap, hints);
            if (results != null && results.length > 0) {
                Result best = results[0];
                for (Result r : results) {
                    if (r.getText().length() > best.getText().length()) best = r;
                }
                return best;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static BufferedImage upscaleImage(BufferedImage src, double factor) {
        int w = (int) (src.getWidth() * factor);
        int h = (int) (src.getHeight() * factor);
        BufferedImage zoomed = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = zoomed.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
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

    private static String errorResponse(int code, String msg) {
        JSONObject json = new JSONObject();
        json.put("code", String.valueOf(code));
        json.put("message", msg);
        return json.toString();
    }
}