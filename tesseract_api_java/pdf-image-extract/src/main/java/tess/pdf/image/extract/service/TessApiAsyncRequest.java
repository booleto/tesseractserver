/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.service;

//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tess.pdf.image.extract.config.PdfImageExtractConfig;
import tess.pdf.image.extract.runner.TessApiThreadRunner;

/**
 *
 * @author toanlh-10
 */
@Service
public class TessApiAsyncRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TessApiAsyncRequest.class);
    private final PdfImageExtractConfig pdfImageExtractConfig;

    public TessApiAsyncRequest(PdfImageExtractConfig pdfImageExtractConfig) {
        this.pdfImageExtractConfig = pdfImageExtractConfig;
    }
    
    
    public ObjectNode sendAsync(List<File> images) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        List<TessApiThreadRunner> runners = new ArrayList<>();
        ArrayList<String> responses = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        LOGGER.info("Node created");
        
        for (File image : images) {
            TessApiThreadRunner runner = new TessApiThreadRunner(new RequestSender(pdfImageExtractConfig));
            runner.setFile(image);
            runners.add(runner);
            Thread thread = new Thread(runner);
            threads.add(thread);
            thread.start();
        }

        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).join();
            LOGGER.info("Got to node " + i);
            node.put(Integer.toString(i), runners.get(i).getResult());
            LOGGER.info("Added node");
        }
        LOGGER.info(node.toPrettyString());
        return node;
    }
}
