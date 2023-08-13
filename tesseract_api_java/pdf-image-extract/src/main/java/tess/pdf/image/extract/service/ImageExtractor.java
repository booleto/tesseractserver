/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.service;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author toanlh-10
 */
@Service
public class ImageExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageExtractor.class);

    /**
     * Tách ảnh từ file pdf
     * @param file
     * @return
     * @throws IOException 
     */
    public ArrayList<File> extractImageFiles(byte[] file) throws IOException {
        ArrayList<File> imageList;
        try (PDDocument doc = PDDocument.load(file)) {
            LOGGER.info("Loaded PDF with pages count: " + doc.getNumberOfPages());
            imageList = new ArrayList<>();

            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                PDPage page = doc.getPage(i);
                PDResources resource = page.getResources();
                LOGGER.info("Loaded page " + i);
                imageList.addAll(getImagesFromResources(resource));
            }

        }
        LOGGER.info("Number of detected images: " + imageList.size());
        return imageList;
    }

    /**
     * Tìm ảnh từ PDResources trong file pdf
     * Tìm kiếm đệ quy qua các resources con thuộc PDResources để lấy các file ảnh.
     * @param resources
     * @return
     * @throws IOException 
     */
    private List<File> getImagesFromResources(PDResources resources) throws IOException {
        List<File> images = new ArrayList<>();
        
        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);

            if (xObject instanceof PDFormXObject) {
                images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
            } else if (xObject instanceof PDImageXObject) {
                BufferedImage image = ((PDImageXObject) xObject).getImage();
                File file = bufferedImageToTempPng(image);
                images.add(file);
            }
        }

        return images;
    }
    
    /**
     * Chuyển ảnh dạng BufferedImage về file png tạm
     * @param image ảnh
     * @return file tạm
     * @throws IOException 
     */
    public File bufferedImageToTempPng(BufferedImage image) throws IOException {
        UUID uuid = UUID.randomUUID();
        File file = File.createTempFile(uuid.toString(), ".png");
        file.deleteOnExit();
        ImageIO.write(image, "png", file);
        return file;
    }
}
