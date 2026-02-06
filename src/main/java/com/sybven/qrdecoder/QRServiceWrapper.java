package com.sybven.qrdecoder;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;import com.konylabs.middleware.dataobject.Result;
import org.json.JSONObject;

import java.util.Map;

/**
 * Wrapper de integración para Kony Fabric.
 * Implementa múltiples técnicas de captura para asegurar que el base64Input
 * sea detectado sin importar el formato de la petición.
 */
public class QRServiceWrapper implements JavaService2 {

    @Override
    public Object invoke(String operationName, Object[] inputArray,
                         DataControllerRequest request, DataControllerResponse response) throws Exception {

        String base64Input = null;
        String tecnicaUsada = "Ninguna (Input vacío)";

        // Técnica 1: Intentar leer del inputMap (Configuración estándar de Integration Service)
        try {
            if (inputArray != null && inputArray.length > 0 && inputArray[0] instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) inputArray[0];
                if (inputMap.containsKey("base64Input")) {
                    base64Input = (String) inputMap.get("base64Input");
                    if (base64Input != null && !base64Input.trim().isEmpty()) {
                        tecnicaUsada = "Tecnica 1: inputMap";
                    }
                }
            }
        } catch (Exception ignored) {}

        // Técnica 2: Intentar leer de los parámetros del request (Query Params o Form-UrlEncoded)
        if (base64Input == null || base64Input.trim().isEmpty()) {
            try {
                base64Input = request.getParameter("base64Input");
                if (base64Input != null && !base64Input.trim().isEmpty()) {
                    tecnicaUsada = "Tecnica 2: request.getParameter";
                }
            } catch (Exception ignored) {}
        }

        // Técnica 3: Intentar leer del cuerpo JSON (Peticiones REST con JSON Body)
        if (base64Input == null || base64Input.trim().isEmpty()) {
            try {
                Object requestBodyObj = request.getAttribute("REQUEST_BODY");
                if (requestBodyObj instanceof Map) {
                    Map<String, Object> requestBodyMap = (Map<String, Object>) requestBodyObj;
                    if (requestBodyMap.containsKey("base64Input")) {
                        base64Input = (String) requestBodyMap.get("base64Input");
                        if (base64Input != null && !base64Input.trim().isEmpty()) {
                            tecnicaUsada = "Tecnica 3: request.getAttribute('REQUEST_BODY')";
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        String jsonResponseString = Decoder.decode(base64Input);

        JSONObject jsonResponse = new JSONObject(jsonResponseString);
        Result result = new Result();

        for (String key : jsonResponse.keySet()) {
            result.addParam(key, String.valueOf(jsonResponse.get(key)));
        }

        result.addParam("tecnicaUtilizada", tecnicaUsada);

        return result;
    }
}