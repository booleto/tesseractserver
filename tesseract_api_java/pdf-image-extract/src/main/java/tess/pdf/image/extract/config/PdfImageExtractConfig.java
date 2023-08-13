/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import tess.pdf.image.extract.service.RequestSender;

/**
 *
 * @author toanlh-10
 */

@Configuration
@EnableAsync
public class PdfImageExtractConfig {
    @Value("${tess.pdf.image.extract.tessServer}")
    String tessServer;
    @Value("${tess.pdf.image.extract.paraExtractServer}")
    String paraExtractServer;

    public String getTessServer() {
        return tessServer;
    }

    public String getParaExtractServer() {
        return paraExtractServer;
    }
    
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
//    @Bean
//    RequestSender requestSender() {
//        return new RequestSender();
//    }
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
