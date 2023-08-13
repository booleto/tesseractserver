/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseract.paragraph.extract.runner;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tess.tesseract.paragraph.extract.service.RequestSender;

/**
 *
 * @author toanlh-10
 */
public class TessApiThreadRunner implements Runnable {

    private RequestSender requestSender;

    private volatile String result;
    private File file;

    public TessApiThreadRunner(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public void run() {
        try {
            result = requestSender.sendImageToTessApi(file);
        } catch (IOException ex) {
            Logger.getLogger(TessApiThreadRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getResult() {
        return this.result;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
}
