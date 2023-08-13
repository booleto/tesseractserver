/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package tess.pdf.image.extract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author toanlh-10
 */

@SpringBootApplication
@ComponentScan({
    "tess.pdf.image.extract.controller",
    "tess.pdf.image.extract.service",
    "tess.pdf.image.extract.config"
})
public class PdfImageExtract {

    public static void main(String[] args) {
        SpringApplication.run(PdfImageExtract.class, args);
    }
}
