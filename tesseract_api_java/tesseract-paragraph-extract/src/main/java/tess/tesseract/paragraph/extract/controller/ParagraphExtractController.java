/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseract.paragraph.extract.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.leptonica;
import org.bytedeco.tesseract.global.tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tess.tesseract.paragraph.extract.service.ParagraphExtractor;
import tess.tesseract.paragraph.extract.service.RequestSender;
import tess.tesseract.paragraph.extract.service.TessApiAsyncRequest;

/**
 *
 * @author toanlh-10
 */
@RestController
@RequestMapping("/tess/paragraph")
public class ParagraphExtractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParagraphExtractController.class);
    private final ParagraphExtractor paragraphExtractor;
    private final RequestSender requestSender;
    private final TessApiAsyncRequest tessApiAsyncRequest;

    public ParagraphExtractController(
            ParagraphExtractor paragraphExtractor,
            RequestSender requestSender,
            TessApiAsyncRequest tessApiAsyncRequest
    ) {
        this.paragraphExtractor = paragraphExtractor;
        this.requestSender = requestSender;
        this.tessApiAsyncRequest = tessApiAsyncRequest;
    }

    /**
     * Chia ảnh thành từng đoạn văn, gửi song song tới tesseract
     *
     * @param headers
     * @param params
     * @param image
     * @return kết quả tesseract đọc được
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String readCustom(
//            @RequestHeader Map<String, String> headers,
//            @RequestPart String params,
//            @RequestPart MultipartFile image) throws IOException {
//        byte[] bytes = image.getBytes();
//        long img_size = image.getSize();
//        
//        PIX pic = null;
//        List<PIX> segmentedImages = new ArrayList<>();
//        try {
//            ArrayList<String> results = new ArrayList<>();
//            pic = leptonica.pixReadMem(bytes, img_size);
//            segmentedImages = paragraphExtractor.extract(pic);
//            
//            // Header pagesegmode là PSM_SINGLE_BLOCK vì mỗi ảnh là một đoạn văn (block of text)
//            Map<String, String> tessHeader = new HashMap<>();
//            tessHeader.put("pagesegmode", Integer.toString(tesseract.PSM_SINGLE_BLOCK));
//            
//            for (int i = 0; i < segmentedImages.size(); i++) {
//                PIX img = segmentedImages.get(i);
//                if (img.isNull()) {
//                    LOGGER.info("Null image detected. Address before parse: " + Long.toString(img.address()));
//                    break;
//                } 
//                String res = requestSender.sendPIXToTessApi(img);
//                results.add(res);
//            }
//            
//            String ret = new String();
//            for (String res : results) {
//                ret = ret + "\n" + res;
//            }
//            return ret;
//            
//        } finally {
//            leptonica.pixDestroy(pic);
//            if (!segmentedImages.isEmpty()) {
//                for (PIX pix : segmentedImages) leptonica.pixDestroy(pix);
//            }
//        }
//    }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> readCustom(
            @RequestHeader Map<String, String> headers,
            @RequestPart String params,
            @RequestPart MultipartFile image) throws IOException, InterruptedException {
        byte[] bytes = image.getBytes();
        long img_size = image.getSize();
        
        PIX pic = null;
        List<PIX> segmentedImages = new ArrayList<>();
        try {
            pic = leptonica.pixReadMem(bytes, img_size);
            segmentedImages = paragraphExtractor.extract(pic);
            
            // Header pagesegmode là PSM_SINGLE_BLOCK vì mỗi ảnh là một đoạn văn (block of text)
            Map<String, String> tessHeader = new HashMap<>();
            tessHeader.put("pagesegmode", Integer.toString(tesseract.PSM_SINGLE_BLOCK));
            
            List<File> imageFiles = new ArrayList<>();
            for (PIX pix : segmentedImages) {
                imageFiles.add(requestSender.pixToFile(pix));
            }
            ObjectNode ret = tessApiAsyncRequest.sendAsync(imageFiles);
             
            return new ResponseEntity<>(ret.toString(), HttpStatus.OK);
            
        } finally {
            leptonica.pixDestroy(pic);
            if (!segmentedImages.isEmpty()) {
                for (PIX pix : segmentedImages) leptonica.pixDestroy(pix);
            }
        }
    }

}
