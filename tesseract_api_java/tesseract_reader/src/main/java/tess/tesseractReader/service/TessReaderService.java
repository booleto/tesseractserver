/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseractReader.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author toanlh-10
 */
@Service
public class TessReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TessReaderService.class);

    public String readPIXImage(PIX image, Map<String, String> params) {
        TessBaseAPI tessApi = new TessBaseAPI();
        String str_result = new String();

        setTesseractParameters(tessApi, params);

//        tessApi.GetComponentImages(0, true, pixa, ib)
        tessApi.SetImage(image);

        try (BytePointer result = tessApi.GetUTF8Text()) {
            if (result != null) {
                byte[] result_bytes = result.getStringBytes();
                str_result = new String(result_bytes, StandardCharsets.UTF_8);
            }

            tessApi.End();
//        if (result != null) {
//            result.deallocate();
//        }
        }
        return str_result;
    }

    private TessBaseAPI setTesseractParameters(TessBaseAPI tessApi, Map<String, String> params) {
        // Các giá trị mặc định
        String lang = "vie";
        int pagesegmode = 3;

        if (params.containsKey("language")) {
            lang = params.get("language");
        }
        if (tessApi.Init(null, lang) != 0) {
            LOGGER.error("khong load duoc ngon ngu :(");
        }
        LOGGER.info("language: " + lang);

        if (params.containsKey("pagesegmode")) {
            pagesegmode = Integer.parseInt(params.get("pagesegmode"));
        }
        LOGGER.info("pageSegMode: " + params.get("pagesegmode"));
        tessApi.SetPageSegMode(pagesegmode);

        return tessApi;
    }
//    
//    private String setOrDefault(Map<String, String> params, String key, String defaultValue) {
//        if (params.containsKey(key)) {
//            return params.get(key);
//        }
//        return defaultValue;
//    }
}
