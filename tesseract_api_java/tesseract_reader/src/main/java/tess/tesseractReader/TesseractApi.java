package tess.tesseractReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author toanlh-10
 */
@SpringBootApplication
@ComponentScan({
    "tess.tesseractReader.service",
    "tess.tesseractReader.controller"
//    "tess.tesseractReader.config"
})
public class TesseractApi {
    public static void main(String[] args) {
        SpringApplication.run(TesseractApi.class, args);
    }
}
