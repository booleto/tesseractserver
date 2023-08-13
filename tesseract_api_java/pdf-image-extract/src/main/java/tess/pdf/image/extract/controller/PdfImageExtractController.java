/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tess.pdf.image.extract.runner.ParaExtractThreadRunner;
import tess.pdf.image.extract.service.ImageExtractor;
import tess.pdf.image.extract.service.ParaExtractAsyncRequest;
import tess.pdf.image.extract.service.RequestSender;
import tess.pdf.image.extract.service.TessApiAsyncRequest;

/**
 *
 * @author toanlh-10
 */
@RestController
@RequestMapping("/pdfextract")
public class PdfImageExtractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfImageExtractController.class);

    private final ImageExtractor imageExtractor;
    private final RequestSender requestSender;
    private final ParaExtractAsyncRequest paraExtractAsyncRequest;
    private final TessApiAsyncRequest tessApiAsyncRequest;

    public PdfImageExtractController(
            ImageExtractor imageExtractor,
            RequestSender requestSender,
            ParaExtractAsyncRequest paraExtractAsyncRequest,
            TessApiAsyncRequest tessApiAsyncRequest
    ) {
        this.imageExtractor = imageExtractor;
        this.requestSender = requestSender;
        this.paraExtractAsyncRequest = paraExtractAsyncRequest;
        this.tessApiAsyncRequest = tessApiAsyncRequest;
    }

    /**
     * Đọc chữ từ file pdf. Tách các ảnh thành phần từ file pdf, gửi song song
     * tới API tách ảnh thành đoạn văn và API Tesseract
     *
     * @param header
     * @param body
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json;charset=UTF-8")
    public String ocrPdf(@RequestHeader Map<String, String> header,
            @RequestPart("body") String body,
            @RequestPart("file") MultipartFile file) throws IOException {
        byte[] filebytes = file.getBytes();

        ArrayList<File> images = imageExtractor.extractImageFiles(filebytes);
        Map<String, String> tessHeaders = new HashMap<>();

        ArrayList<String> responses = new ArrayList<>();
        for (File image : images) {
            responses.add(requestSender.sendImageToTessApi(image));
        }

        return responses.toString();
    }
//    @PostMapping(value = "/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String ocrPdf(@RequestHeader Map<String, String> header,
//            @RequestPart("body") String body,
//            @RequestPart("file") MultipartFile file) throws IOException {
//        byte[] filebytes = file.getBytes();
//
//        ArrayList<File> images = imageExtractor.extractImageFiles(filebytes);
////        Map<String, String> tessHeaders = new HashMap<>();
//
//        String response = tessApiTaskExecutor.sendFileToTessApiAsync(images);
//
//        return response;
//    }

//    @PostMapping(value = "/pdf/para", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String ocrPdf1(@RequestHeader Map<String, String> header,
//            @RequestPart("body") String body,
//            @RequestPart("file") MultipartFile file) throws IOException {
//        byte[] filebytes = file.getBytes();
//
//        ArrayList<File> images = imageExtractor.extractImageFiles(filebytes);
//        Map<String, String> tessHeaders = new HashMap<>();
//
//        ArrayList<String> responses = new ArrayList<>();
//        for (File image : images) {
//            responses.add(requestSender.sendImageToParagraphExtractor(image));
//        }
//
//        return responses.toString();
//    }

    @PostMapping(value = "/pdf/para", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity ocrPdfPara(@RequestHeader Map<String, String> header,
            @RequestPart("body") String body,
            @RequestPart("file") MultipartFile file) throws IOException, InterruptedException {
        byte[] filebytes = file.getBytes();

        ArrayList<File> images = imageExtractor.extractImageFiles(filebytes);
        Map<String, String> tessHeaders = new HashMap<>();
        
        ObjectNode response = paraExtractAsyncRequest.sendAsync(images);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping(value = "/pdf/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity ocrPdfAsync(@RequestHeader Map<String, String> header,
            @RequestPart("body") String body,
            @RequestPart("file") MultipartFile file) throws IOException, InterruptedException {
        byte[] filebytes = file.getBytes();

        ArrayList<File> images = imageExtractor.extractImageFiles(filebytes);
        Map<String, String> tessHeaders = new HashMap<>();

        ObjectNode response = tessApiAsyncRequest.sendAsync(images);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }
}
