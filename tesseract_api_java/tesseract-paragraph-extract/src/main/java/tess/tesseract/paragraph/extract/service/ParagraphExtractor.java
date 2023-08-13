/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseract.paragraph.extract.service;

import java.util.ArrayList;
import java.util.Random;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.leptonica.BOX;
import org.bytedeco.leptonica.BOXA;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.PIXA;
import org.bytedeco.leptonica.global.leptonica;
import org.bytedeco.tesseract.TessBaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.bytedeco.tesseract.global.tesseract;

/**
 *
 * @author toanlh-10
 */
@Service
public class ParagraphExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParagraphExtractor.class);

    public ParagraphExtractor() {
        LOGGER.info("ParagraphExtractor initialized");
    }

//    public ArrayList<PIX> dei(PIX image) {
//        TessBaseAPI tessApi = new TessBaseAPI();
//        tessApi.Init(null, "vie");
//        tessApi.SetImage(image);
//
//        BOXA boxes = null;
//        PIXA pics = null;
//
//        try {
//            boxes = tessApi.GetComponentImages(1, true, null, (IntBuffer) null);
//            pics = leptonica.pixClipRectangles(image, boxes);
//            Random rand = new Random(123456);
//            
////        PointerPointer picsArrayPointer = leptonica.pixaGetPixArray(pics);
//
//            ArrayList<PIX> extractedImages = new ArrayList<>();
//            for (int i = 0; i < pics.sizeof(); i++) {
//                
//                PIX pic = pics.pix(i);
//                leptonica.pixWritePng("test" + Long.toString(rand.nextLong()) + ".png", pic, 0);
//                
//                UByteBufferIndexer bufferIndexer = new UByteBufferIndexer(pic.asByteBuffer());
//                extractedImages.add(pic);
//                
//            }
//            
//            LOGGER.info("Size of image list: " + pics.sizeof());
//            LOGGER.info("Addresses: "); String str = "";
//            for(PIX pix : extractedImages) str = str + " " + Long.toString(pix.address());
//            LOGGER.info(str);
//            
//            return extractedImages;
//        } finally {
//            // deallocate
//            leptonica.boxaDestroy(boxes);
//            leptonica.pixaDestroy(pics);
//            tessApi.deallocate();
//        }
//    }
    public ArrayList<PIX> extract123(PIX image) {
        TessBaseAPI tessApi = new TessBaseAPI();
        tessApi.Init(null, "vie");
        tessApi.SetImage(image);

        BOXA boxes = null;
        PIXA pics = null;
        try {
//            boxes = tessApi.GetComponentImages(1, true, null, (IntBuffer) null);
            boxes = tessApi.GetRegions(new PointerPointer());

            for (int i = 0; i < boxes.sizeof(); i++) {
//                if (boxes.box(i) != null) LOGGER.info(Long.toString(boxes.box(i).address()));
//                else LOGGER.info(" null ");
                BOX box = boxes.box(i);
                if (box == null) {
                    continue;
                }
                LOGGER.info(box.x() + " " + box.y() + " " + box.w() + " " + box.h());
            }

            pics = leptonica.pixClipRectangles(image, boxes);
            Random rand = new Random(123456);

            String str = new String();
            for (int i = 0; i < pics.sizeof(); i++) {
                if (pics.pix(i) != null) {
                    str = str + " " + pics.pix(i).address();
                } else {
                    str = str + " null ";
                }
            }
            LOGGER.info(str);

//        PointerPointer picsArrayPointer = leptonica.pixaGetPixArray(pics);
            ArrayList<PIX> extractedImages = new ArrayList<>();
            for (int i = 0; i < pics.sizeof(); i++) {
                PIX pic = pics.pix(i);

                if (pic == null) {
                    continue;
                }

                LOGGER.info(Long.toString(pic.address()));
                if (pic.address() < 1000) {
                    continue;
                }
                leptonica.pixWritePng("test" + Long.toString(rand.nextLong()) + ".png", pic, 0);

//                UByteBufferIndexer bufferIndexer = new UByteBufferIndexer(pic.asByteBuffer());
//                extractedImages.add(pic);
            }
            return extractedImages;

        } finally {
            leptonica.boxaDestroy(boxes);
            leptonica.pixaDestroy(pics);
            tessApi.deallocate();
        }
    }
    
    /**
     * Tách ảnh đoạn văn ra khỏi ảnh gốc
     * 
     * @param image: ảnh gốc
     * @return ArrayList<PIX>: các ảnh đoạn văn đã tách
     */
    public ArrayList<PIX> extract(PIX image) {
        Random rand = new Random(12345);
        TessBaseAPI tessApi = new TessBaseAPI();
        tessApi.Init(null, "vie");
        tessApi.SetPageSegMode(tesseract.PSM_AUTO);
        tessApi.SetImage(image);

//        BOXA boxes = tessApi.
//        BOXA boxes = tessApi.GetComponentImages(tesseract.RIL_SYMBOL, true, null, (int[]) null);
//        BOXA boxes = tessApi.GetTextlines(null, (int[]) null);
//        BOXA boxes = tessApi.GetStrips(null, (int[]) null);
        BOXA boxes = tessApi.GetRegions((PIXA) null);

        ArrayList<PIX> result = new ArrayList<>();

        for (int i = 0; i < leptonica.boxaGetValidCount(boxes); i++) {
//            BOX box = boxes.box(i);
            BOX box = leptonica.boxaGetValidBox(boxes, i, leptonica.L_COPY); //L_COPY=1, truyền con trỏ tới bản sao thay vì đến object gốc
            if (box != null) {
                LOGGER.info("Found valid box: " + box.x() + " " + box.y() + " " + box.w() + " " + box.h());
            } else {
                LOGGER.info(null);
            }

            PIX pic = leptonica.pixClipRectangle(image, box, (BOX) null);
            leptonica.pixWritePng("imagedump/test" + Long.toString(rand.nextLong()) + ".png", pic, 0);
            result.add(pic);
        }

        return result;
    }

//    public ArrayList<PIX> extract(PIX image) {
//        Random rand = new Random(12345);
//        TessBaseAPI tessApi = new TessBaseAPI();
//        tessApi.Init(null, "vie");
//        tessApi.SetPageSegMode(3);
//        tessApi.SetImage(image);
////        BOXA boxes = tessApi.
//        BOXA boxes = tessApi.GetComponentImages(tesseract.RIL_SYMBOL, true, null, (int[]) null);
////        BOXA boxes = tessApi.GetTextlines(null, (int[]) null);
////        BOXA boxes = tessApi.GetStrips(null, (int[]) null);
////        BOXA boxes = tessApi.GetRegions((PIXA) null);
////        tessApi.get
//        ArrayList<PIX> result = new ArrayList<>();
//        BOXA validBoxes = new BOXA();
//        for (int i = 0; i < leptonica.boxaGetValidCount(boxes); i++) {
////            BOX box = boxes.box(i);
//            BOX box = leptonica.boxaGetValidBox(boxes, i, leptonica.L_COPY); //L_COPY=1, truyền con trỏ tới bản sao thay vì đến object gốc
//            if (box != null) {
//                LOGGER.info("Found valid box: " + box.x() + " " + box.y() + " " + box.w() + " " + box.h());
//                leptonica.boxaAddBox(validBoxes, box, leptonica.L_COPY_CLONE);
//            } else {
//                LOGGER.info(null);
//            }
//
//            PIX pic = leptonica.pixClipRectangle(image, box, (BOX) null);
//            leptonica.pixWritePng("imagedump/test" + Long.toString(rand.nextLong()) + ".png", pic, 0);
//            result.add(pic);
//        }
//        
//        PIX boxPic = leptonica.pixDrawBoxa(image, validBoxes, 0, 0);
//        leptonica.pixWritePng("imagedump/result.png", boxPic, 0);
//
//        return result;
//    }
}
