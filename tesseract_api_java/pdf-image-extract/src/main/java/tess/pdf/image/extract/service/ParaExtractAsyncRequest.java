/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import tess.pdf.image.extract.config.PdfImageExtractConfig;
import tess.pdf.image.extract.runner.ParaExtractThreadRunner;

/**
 *
 * @author toanlh-10
 */
@Service
public class ParaExtractAsyncRequest {
    private final PdfImageExtractConfig pdfImageExtractConfig;

    public ParaExtractAsyncRequest(PdfImageExtractConfig pdfImageExtractConfig) {
        this.pdfImageExtractConfig = pdfImageExtractConfig;
    }
    
    

    /**
     * Gửi song song một list file ảnh tới Tesseract Paragraph Extract
     *
     * @param images
     * @return
     * @throws InterruptedException
     */
    public ObjectNode sendAsync(List<File> images) throws InterruptedException, JsonProcessingException {
        List<Thread> threads = new ArrayList<>();
        List<ParaExtractThreadRunner> runners = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        
        for (File image : images) {
            ParaExtractThreadRunner runner = new ParaExtractThreadRunner(new RequestSender(pdfImageExtractConfig));
            runner.setFile(image);
            runners.add(runner);
            Thread thread = new Thread(runner);
            threads.add(thread);
            thread.start();
        }

        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).join();
            JsonNode tree = mapper.readTree(runners.get(i).getResult());
            result.put("page " + i, tree);
        }
        return result;
    }
}
