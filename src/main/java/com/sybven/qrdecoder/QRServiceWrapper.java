package com.sybven.qrdecoder;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;import com.konylabs.middleware.dataobject.Result;
import org.json.JSONObject;
import java.util.Map;

public class QRServiceWrapper implements JavaService2 {
    @Override
    public Object invoke(String opName, Object[] inputArray, DataControllerRequest request, DataControllerResponse response) throws Exception {
        String base64Input = null;
        String tecnica = "Ninguna";

        if (inputArray != null && inputArray.length > 0 && inputArray[0] instanceof Map) {
            base64Input = (String) ((Map)inputArray[0]).get("base64Input");
            if (base64Input != null) tecnica = "inputMap";
        }

        if (base64Input == null) {
            base64Input = request.getParameter("base64Input");
            if (base64Input != null) tecnica = "getParameter";
        }

        String jsonStr = Decoder.decode(base64Input);
        JSONObject json = new JSONObject(jsonStr);
        Result result = new Result();
        for (String key : json.keySet()) {
            result.addParam(key, json.get(key).toString());
        }
        result.addParam("tecnicaUtilizada", tecnica);
        return result;
    }
}