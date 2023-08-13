/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package tess.tesseract.paragraph.extract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author toanlh-10
 */
@SpringBootApplication
@ComponentScan({
    "tess.tesseract.paragraph.extract",
    "tess.tesseract.paragraph.extract.controller"
})
public class TesseractParagraphExtract {

    public static void main(String[] args) {
        SpringApplication.run(TesseractParagraphExtract.class, args);
    }
}
