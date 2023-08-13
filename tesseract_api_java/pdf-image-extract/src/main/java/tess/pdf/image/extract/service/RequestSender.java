/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.pdf.image.extract.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import tess.pdf.image.extract.config.PdfImageExtractConfig;

/**
 *
 * @author toanlh-10
 */
@Service
//@Configuration
public class RequestSender {

//    private static final String tessServer = "http://10.14.222.194:12321/tesseract/upload";
//    private static final String paraExtractServer = "http://10.14.222.194:11235/tess/paragraph/upload";
//    private static final String tessServer = "http://localhost:12321/test/post";
    
//    @Value("${tess.pdf.image.extract.tessServer}")
//    public String tessServer;
//    @Value("${tess.pdf.image.extract.paraExtractServer}")
//    public String paraExtractServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);
    
    private final PdfImageExtractConfig pdfImageExtractConfig;

    public RequestSender(PdfImageExtractConfig pdfImageExtractConfig) {
        this.pdfImageExtractConfig = pdfImageExtractConfig;
    }
    
    

//    public String sendImageToTessApi(InputStream stream, Map<String, String> headers) throws IOException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//        for (Map.Entry<String, String> entry : headers.entrySet()) {
//            httpHeaders.add(entry.getKey(), entry.getValue());
//        }
//
//        HttpPost httpPost = new HttpPost(server);
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.addTextBody("params", "wtf");
//        builder.addBinaryBody("image", stream);
//        HttpEntity multipart = builder.build();
//
//        httpPost.setEntity(multipart);
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//        CloseableHttpResponse response = httpClient.execute(httpPost);
//
//        LOGGER.info("response sent");
//        return EntityUtils.toString(response.getEntity());
//    }
//    public String sendImageToTessApi(byte[] bytes, Map<String, String> headers) throws IOException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//        for (Map.Entry<String, String> entry : headers.entrySet()) {
//            httpHeaders.add(entry.getKey(), entry.getValue());
//        }
//
//        HttpPost httpPost = new HttpPost(server);
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.addTextBody("params", "wtf");
//        builder.addBinaryBody("image", bytes);
//        HttpEntity multipart = builder.build();
//
//        httpPost.setEntity(multipart);
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//        CloseableHttpResponse response = httpClient.execute(httpPost);
//
//        LOGGER.info("response sent");
//        return EntityUtils.toString(response.getEntity());
//    }
//    public String sendImageToTessApi(byte[] bytes) throws IOException {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parseMediaType("text/plain");
//        okhttp3.RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("body", "none")
//                .addFormDataPart("file", "/C:/Users/toanlh-10/Documents/Git/keras_demo/test_set/4691-cn.signed.pdf",
//                        okhttp3.RequestBody.create(bytes))
//                .build();
//        Request request = new Request.Builder()
//                .url("server")
//                .method("POST", body)
//                .build();
//        Response response = client.newCall(request).execute();
//        return response.message();
//    }
//    public String sendImageToTessApi(byte[] bytes) {
//        HttpHeaders headers = new HttpHeaders();
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
//        body.add("params", "non");
//        body.add("image", bytes);
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity
//                = new HttpEntity<>(body, headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.postForEntity(tessServer, requestEntity, String.class);
//        return response.getBody();
//    }
//    public String sendImageToTessApi1(File file) throws FileNotFoundException, IOException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Connection", "keep-alive");
//        headers.add("Accept-Encoding", "gzip, deflate, br");
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("params", "non");
////        body.add("file", );
////        body.add("file", new FileInputStream(file).readAllBytes());
//
////        body.add("image", file);
////        LOGGER.info(Arrays.toString(new FileInputStream(file).readAllBytes()));
//        HttpEntity<MultiValueMap<String, Object>> requestEntity
//                = new HttpEntity<>(body, headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.postForEntity(tessServer, requestEntity, String.class);
//        return response.getBody();
//    }
    /**
     * Gửi file ảnh đến Tesseract API
     *
     * @param file file ảnh
     * @return chữ đọc được từ ảnh
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String sendImageToTessApi(File file) throws FileNotFoundException, IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("params", "asdf")
                .addFormDataPart("image", "pic_1.png",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                file))
                .build();
        Request request = new Request.Builder()
                .url(pdfImageExtractConfig.getTessServer())
                .method("POST", body)
                .addHeader("PageSegMode", "3")
                .addHeader("Language", "vie")
                .build();
        Response response = client.newCall(request).execute();

        String result = response.body().string();
        LOGGER.info(result);
        return result;
    }

    /**
     * Gửi ảnh tới Tesseract Paragraph Extract
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String sendImageToParagraphExtractor(File file) throws FileNotFoundException, IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("params", "asdf")
                .addFormDataPart("image", "pic_1.png",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                file))
                .build();
        Request request = new Request.Builder()
                .url(pdfImageExtractConfig.getParaExtractServer())
                .method("POST", body)
                .addHeader("PageSegMode", "3")
                .addHeader("Language", "vie")
                .build();
        Response response = client.newCall(request).execute();

        String result = response.body().string();
        LOGGER.info(result);
        return result;
    }
}
