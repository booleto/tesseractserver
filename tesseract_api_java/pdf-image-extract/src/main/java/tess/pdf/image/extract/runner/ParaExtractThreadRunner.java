/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.runner;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tess.pdf.image.extract.service.RequestSender;

/**
 *
 * @author toanlh-10
 */
public class ParaExtractThreadRunner implements Runnable {

    private RequestSender requestSender;

    private volatile String result;
    private File file;

    public ParaExtractThreadRunner(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public void run() {
        try {
            result = requestSender.sendImageToParagraphExtractor(file);
        } catch (IOException ex) {
            Logger.getLogger(ParaExtractThreadRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getResult() {
        return this.result;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
}
