package com.sybven.qrdecoder;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import org.json.JSONObject;

import java.util.Map;

public class QRServiceWrapper implements JavaService2 {

    @Override
    public Object invoke(String operationName, Object[] inputArray,
                         DataControllerRequest request, DataControllerResponse response) throws Exception {

        String base64Input = null;
        String tecnicaUsada = "Ninguna (Input fue nulo)";

        // Técnica 1: Intentar leer del inputMap
        try {
            if (inputArray != null && inputArray.length > 0 && inputArray[0] instanceof Map) {
                Map<String, Object> inputMap = (Map<String, Object>) inputArray[0];
                if (inputMap.containsKey("base64Input")) {
                    base64Input = (String) inputMap.get("base64Input");
                    if (base64Input != null && !base64Input.isEmpty()) {
                        tecnicaUsada = "Tecnica 1: inputMap";
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // Técnica 2: Intentar leer de los parámetros del request
        if (base64Input == null || base64Input.isEmpty()) {
            try {
                base64Input = request.getParameter("base64Input");
                if (base64Input != null && !base64Input.isEmpty()) {
                    tecnicaUsada = "Tecnica 2: request.getParameter";
                }
            } catch (Exception ignored) {
            }
        }

        // Técnica 3: Intentar leer del cuerpo JSON
        if (base64Input == null || base64Input.isEmpty()) {
            try {
                Map<String, Object> requestBodyMap = (Map<String, Object>) request.getAttribute("REQUEST_BODY");
                if (requestBodyMap != null && requestBodyMap.containsKey("base64Input")) {
                    base64Input = (String) requestBodyMap.get("base64Input");
                    if (base64Input != null && !base64Input.isEmpty()) {
                        tecnicaUsada = "Tecnica 3: request.getAttribute('REQUEST_BODY')";
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String jsonResponseString = Decoder.decode(base64Input);

        JSONObject jsonResponse = new JSONObject(jsonResponseString);
        Result result = new Result();
        for (String key : jsonResponse.keySet()) {
            result.addParam(key, jsonResponse.get(key).toString());
        }

        result.addParam("tecnicaUtilizada", tecnicaUsada);

        return result;
    }
}
