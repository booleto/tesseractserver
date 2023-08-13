/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tess.tesseract.paragraph.extract.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.bytedeco.javacpp.SizeTPointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.leptonica;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author toanlh-10
 */
@Service
public class RequestSender {

    private static final String SERVER = "http://localhost:12345/tesseract/upload";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);

//    public String sendImageToTesseract(PIX pix, Map<String, String> headers) throws IOException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//        for (Map.Entry<String, String> entry : headers.entrySet()) {
//            httpHeaders.add(entry.getKey(), entry.getValue());
//        }
//
////        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
////        body.add("params", "");
////        body.add("image", pix.asByteBuffer().array());
////        HttpEntity<MultiValueMap<String, Object>> requestEntity
////                = new HttpEntity<>(body, httpHeaders);
////        RestTemplate restTemplate = new RestTemplate();
////        ResponseEntity<String> response = restTemplate.postForObject(server, requestEntity, String.class);
////
//        HttpPost httpPost = new HttpPost(server);
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.addTextBody("params", "wtf");
////        builder.addBinaryBody("file", inputStream, ContentType.APPLICATION_OCTET_STREAM, fileName);
//        LOGGER.info("PIX address: " + Long.toString(pix.address()));
//        ByteBuffer pixBytesBuffer = pix.asByteBuffer();
//        LOGGER.info("67 " + pixBytesBuffer.toString() + " has array: " + pixBytesBuffer.hasArray());
////        byte[] pixBytesArr = pixBytesBuffer.array(); LOGGER.info("68 " + Arrays.toString(pixBytesArr));
////        byte[] pixBytesArr = new byte[pixBytesBuffer.];
//
////        builder.addBinaryBody("image", pixBytesArr); LOGGER.info("69");
////        builder.addBinaryBody("image", );
////        leptonica.pixwrite
//        System.out.println("create buffer has array: " + pix.createBuffer().hasArray());
//        BytesInputStream pixStream = new BytesInputStream(pixBytesBuffer);
//        builder.addBinaryBody("image", pixStream, ContentType.APPLICATION_OCTET_STREAM, "image.png");
//        HttpEntity multipart = builder.build();
////        
////        multipart.writeTo(new OutputStreamWriter());
////        InputStream mult = multipart.getContent();
////        String result = new BufferedReader(new InputStreamReader(mult))
////                .lines().collect(Collectors.joining("\n"));
////        LOGGER.info(result);
//
//        httpPost.setEntity(multipart);
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//        CloseableHttpResponse response = httpClient.execute(httpPost);
//
//        return EntityUtils.toString(response.getEntity());
//    }
    
//    public byte[] pixToBytesArr(PIX pic) {
//        if (pic == null) return null;
////        ByteBuffer buffer = ByteBuffer.allocate(0);
////        IntPointer picpoint = leptonica.pixExtractData(pic);
//        IntPointer point = leptonica.pixGetData(pic);
//        leptonica.pix
//        byte[] imgData = new byte[];
//        leptonica.pixWriteMemBmp(pfdata, pfsize, pic)
//
//        
//        
//    }
    
    /**
     * Gửi ảnh đoạn văn tới tesseract
     * @param pix ảnh đoạn văn
     * @return kết quả đọc được
     * @throws IOException 
     */
    public String sendPIXToTessApi(PIX pix) throws IOException {
        File file = pixToFile(pix);
        String result = sendImageToTessApi(file);
        file.delete();
        return result;

//        byte[] file = pixToBytes(pix);
//        String result = sendImageToTessApi(file);
////        file.delete();
//        return result;
    }
    
    /**
     * Gửi ảnh đoạn văn tới tesseract
     * @param file ảnh đoạn văn
     * @return kết quả đọc được
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
                .url(SERVER)
                .method("POST", body)
                .addHeader("PageSegMode", "6")
                .addHeader("Language", "vie")
                .build();
        Response response = client.newCall(request).execute();
        
        String result = response.body().string();
        LOGGER.info(result);
        return result;
    }
    
    /**
     * Chuyển ảnh từ dạng PIX về File
     * @param pix ảnh
     * @return File ảnh
     * @throws IOException 
     */
    public File pixToFile(PIX pix) throws IOException {   
//        leptonica.pixwritemempng
        
        UUID uuid = UUID.randomUUID();
        String tempName = "temp" + uuid.toString() + ".png";
        leptonica.pixWritePng(tempName, pix, 0);
        
        File file = new File(tempName);
        file.deleteOnExit();        
        return file;
//        ImageIO.write(, server, file)
    }
    
        /**
     * Gửi ảnh đoạn văn tới tesseract
     * @param file ảnh đoạn văn
     * @return kết quả đọc được
     * @throws FileNotFoundException
     * @throws IOException 
     */
//    public String sendImageToTessApi(byte[] bytes) throws FileNotFoundException, IOException {
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        LOGGER.info("Preparing to send");
//        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("params", "asdf")
//                .addFormDataPart("image", "pic_1.png",
//                        RequestBody.create(MediaType.parse("application/octet-stream"),
//                                bytes))
//                .build();
//        LOGGER.info("building request");
//        Request request = new Request.Builder()
//                .url(SERVER)
//                .method("POST", body)
//                .addHeader("PageSegMode", "6")
//                .addHeader("Language", "vie")
//                .build();
//        Response response = client.newCall(request).execute();
//        
//        String result = response.body().string();
//        LOGGER.info(result);
//        return result;
//    }
    
    /**
     * Chuyển ảnh từ dạng PIX về byte[]
     * @param pix ảnh
     * @return File ảnh
     * @throws IOException 
     */
//    public byte[] pixToBytes(PIX pix) throws IOException {  
////        byte[] bytes;
//        ByteBuffer bytes = pix.createBuffer();
//        SizeTPointer sizePointer = new SizeTPointer(pix.capacity());
////        ByteBuffer buffer = new ByteBuffer();
//        int res = leptonica.pixWriteMemPng(bytes, sizePointer, pix, 0);
//        if (res == 1) LOGGER.info("DED");
//        
//        int limit = bytes.limit();
//        byte[] bytearr = new byte[limit];
//        for (int i = 0; i < limit; i++) {
//            bytearr[i] = bytes.get(i);
//            LOGGER.info(Byte.toString(bytearr[i]));
//        }
//        
//        return bytearr;
////        ImageIO.write(, server, file)
//    }
}
