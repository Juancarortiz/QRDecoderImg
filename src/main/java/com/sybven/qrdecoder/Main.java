package com.sybven.qrdecoder;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java -jar QRDecoder.jar <ruta_archivo_txt>");
            return;
        }
        try {
            String content = new String(Files.readAllBytes(Paths.get(args[0])));
            System.out.println(Decoder.decode(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}