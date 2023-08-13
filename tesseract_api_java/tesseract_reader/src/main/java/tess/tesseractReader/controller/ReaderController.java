/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseractReader.controller;

import java.io.IOException;
import java.util.Map;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.leptonica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tess.tesseractReader.service.TessReaderService;

/**
 *
 * @author toanlh-10
 */
@RestController
@RequestMapping("/tesseract")
public class ReaderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderController.class);
    
    private final TessReaderService tessReaderService;
    
    public ReaderController(
            TessReaderService tessReaderService
    ) {
        this.tessReaderService = tessReaderService;
    }

    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/plain;charset=UTF-8")
    public String readCustom(
            @RequestHeader Map<String, String> headers, 
            @RequestPart String params, 
            @RequestPart MultipartFile image) throws IOException {
        byte[] bytes = image.getBytes();
        long img_size = image.getSize();
        PIX pic = leptonica.pixReadMem(bytes, img_size);
        LOGGER.info("PIX address: ", pic.address());
        String result = tessReaderService.readPIXImage(pic, headers);
        
        //deallocate
        leptonica.pixDestroy(pic);
        
        return result;
//        return tessReaderService.readPIXImage(pic, headers);
    }
}
