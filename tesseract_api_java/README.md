# Tesseract API Java

### Cài dặt và chạy
Trên máy data node 3:

##### Tesseract Paragraph Extract:
IP: http://10.14.222.194:11235

Chạy:
```
cd /opt/tesseract_api/tess_paragraph_extract
./run.sh
```

Ngừng chạy:
```
cd /opt/tesseract_api/tess_paragraph_extract
./stop.sh
```

##### Tesseract Reader:
IP: http://10.14.222.194:12321

Chạy:
```
cd /opt/tesseract_api/tess_api
./run.sh
```

Ngừng chạy:
```
cd /opt/tesseract_api/tess_api
./stop.sh
```

### Tại sao dùng JavaCPP thay vì Pytesseract, Tess4j

```mermaid
graph LR;
	pdf(PDF) --> img1(Trang 1)
	pdf(PDF) --> img2(Trang 2)
	
	img1 --> ocr1[(Máy chủ 1)]
	
	img2 --> ocr2[(Máy chủ 2)]
	
	img2 ~~~ ocr3[(Máy chủ 3)]
	img2 ~~~ ocr1
	
	style pdf fill:#649be1,stroke:#000,stroke-width:1px
	style ocr1 fill:#2dd,stroke:#000,stroke-width:1px
	style ocr2 fill:#2dd,stroke:#000,stroke-width:1px
	style ocr3 fill:#2dd,stroke:#000,stroke-width:1px
```

Dùng JavaCPP thì:
```mermaid
graph LR;
	pdf(PDF) --> img1(Trang 1)
	pdf(PDF) --> img2(Trang 2)
	
	img1 --> para1(Đoạn 1)
	img1 --> para2(Đoạn 2)
	img2 --> para3(Đoạn 3)
	img2 --> para4(Đoạn 4)
	
	para1 --> ocr1[(Máy chủ 1)]
	
	para2 --> ocr2[(Máy chủ 2)]
	
	para3 --> ocr3[(Máy chủ 3)]
	para4 --> ocr3
	
	style pdf fill:#649be1,stroke:#000,stroke-width:1px
	style ocr1 fill:#2dd,stroke:#000,stroke-width:1px
	style ocr2 fill:#2dd,stroke:#000,stroke-width:1px
	style ocr3 fill:#2dd,stroke:#000,stroke-width:1px
```


### Kiến trúc phần mềm

```mermaid
graph LR;
	pdf(PDF) --> text_extract(Tách ảnh khỏi PDF)
	
    subgraph pdf-image-extract [PDF Image Extract]
	text_extract --> img(Ảnh)
	end
	
	subgraph tesseract-paragraph-extract [Tesseract Paragraph Extract]
	img -->|Cách 1: Song song theo đoạn| paragraph-extract(Tách ảnh đoạn văn)
	paragraph-extract --> para_img(Ảnh đoạn văn)
    end
	
	subgraph tesseract-api [Tesseract Reader]
	para_img --> tess-reader(OCR ảnh)
	img -->|Cách 2: Song song theo trang| tess-reader
	img -->|Cách 3: Tuần tự| tess-reader
	end
	
    style pdf fill:#649be1,stroke:#000,stroke-width:1px
	style text_extract fill:#fe2,stroke:#000,stroke-width:1px
	style paragraph-extract fill:#fe2,stroke:#000,stroke-width:1px
	style tess-reader fill:#2dd,stroke:#000,stroke-width:1px
```

Tesseract Reader và Tesseract Paragraph Extract cũng có thể được chạy trên nhiều máy khác nhau để xử lý song song:

```mermaid
graph LR;
	pdf(PDF) --> text_extract(Tách ảnh khỏi PDF)
	
    subgraph pdf-image-extract [PDF Image Extract]
	text_extract --> img1(Trang 1)
	text_extract --> img2(Trang 2)
	text_extract --> img3(Trang 3)
	text_extract --> img4(Trang 4)
	end
	
	subgraph tesseract-paragraph-extract1 [Tesseract Paragraph Extract 1]
	img1 --> paragraph-extract1(Tách đoạn văn)
	img2 --> paragraph-extract1
	paragraph-extract1 --> para_img11(Đoạn 1 trang 1)
	paragraph-extract1 --> para_img12(Đoạn 2 trang 1)
	paragraph-extract1 --> para_img21(Đoạn 1 trang 2)
    end
	
	subgraph tesseract-paragraph-extract2 [Tesseract Paragraph Extract 2]
	img3 --> paragraph-extract2(Tách đoạn văn)
	img4 --> paragraph-extract2
	paragraph-extract2 --> para_img31(Đoạn 1 trang 3)
	paragraph-extract2 --> para_img41(Đoạn 1 trang 4)
	paragraph-extract2 --> para_img42(Đoạn 2 trang 4)
    end
	
	subgraph tesseract-api1 [Tesseract Reader 1]
	para_img11 --> tess-reader1(OCR ảnh)
	para_img12 --> tess-reader1
	end
	
	subgraph tesseract-api2 [Tesseract Reader 2]
	para_img21 --> tess-reader2(OCR ảnh)
	para_img31 --> tess-reader2
	end
	
	subgraph tesseract-api3 [Tesseract Reader 3]
	para_img41 --> tess-reader3(OCR ảnh)
	para_img42 --> tess-reader3
	end
	
    style pdf fill:#649be1,stroke:#000,stroke-width:1px
	style text_extract fill:#fe2,stroke:#000,stroke-width:1px
	style paragraph-extract1 fill:#fe2,stroke:#000,stroke-width:1px
	style paragraph-extract2 fill:#fe2,stroke:#000,stroke-width:1px
	
	style tess-reader1 fill:#2dd,stroke:#000,stroke-width:1px
	style tess-reader2 fill:#2dd,stroke:#000,stroke-width:1px
	style tess-reader3 fill:#2dd,stroke:#000,stroke-width:1px
```



### PDF Image Extract

Bước 1
```mermaid
graph LR;
	pdf(PDF) -->|POST HTTP| controller(PdfImageExtractController)
	
	subgraph step1[Bước 1: Tách ảnh khỏi PDF]
		controller --> image-extractor(ImageExtractor)
		image-extractor --> img1(Trang 1)
		image-extractor --> img2(Trang 2)
		image-extractor --> img3(Trang 3)
	end
	
    style pdf fill:#649be1,stroke:#000,stroke-width:1px
	style image-extractor fill:#fe2,stroke:#000,stroke-width:1px
	style controller fill:#fe2,stroke:#000,stroke-width:1px
```

Bước 2

#### Song song theo đoạn

```mermaid
graph LR;
	img1(Trang 1) --> paraasync(ParaExtractAsyncRequest)
	img2(Trang 2) --> paraasync
	img3(Trang 3) --> paraasync
	
	subgraph step2[Bước 2: Gửi song song tới API tách đoạn]
		paraasync -->|Tạo luồng xử lý trang 1| thread1(ParaExtractThreadRunner)
		paraasync -->|Tạo luồng xử lý trang 2| thread2(ParaExtractThreadRunner)
		paraasync -->|Tạo luồng xử lý trang 3| thread3(ParaExtractThreadRunner)
		thread1 --> req1(RequestSender)
		thread2 --> req2(RequestSender)
		thread3 --> req3(RequestSender)
	end
	
	subgraph paraapi[Tesseract Paragraph Extract]
		req1 --> api("Module tiếp theo")
		req2 --> api
		req3 --> api
	end
	
	style paraasync fill:#fe2,stroke:#000,stroke-width:1px
	style thread1 fill:#fe2,stroke:#000,stroke-width:1px
	style thread2 fill:#fe2,stroke:#000,stroke-width:1px
	style thread3 fill:#fe2,stroke:#000,stroke-width:1px
	style req1 fill:#2dd,stroke:#000,stroke-width:1px
	style req2 fill:#2dd,stroke:#000,stroke-width:1px
	style req3 fill:#2dd,stroke:#000,stroke-width:1px
```

#### Song song theo trang

```mermaid
graph LR;
	img1(Trang 1) --> paraasync(TessApiAsyncRequest)
	img2(Trang 2) --> paraasync
	img3(Trang 3) --> paraasync
	
	subgraph step2[Bước 2: Gửi song song tới Tesseract Reader]
		paraasync -->|Tạo luồng xử lý trang 1| thread1(TessApiThreadRunner)
		paraasync -->|Tạo luồng xử lý trang 2| thread2(TessApiThreadRunner)
		paraasync -->|Tạo luồng xử lý trang 3| thread3(TessApiThreadRunner)
		thread1 --> req1(RequestSender)
		thread2 --> req2(RequestSender)
		thread3 --> req3(RequestSender)
	end
	
	subgraph paraapi[Tesseract Reader]
		req1 --> api("Module tiếp theo")
		req2 --> api
		req3 --> api
	end
	
	style paraasync fill:#fe2,stroke:#000,stroke-width:1px
	style thread1 fill:#fe2,stroke:#000,stroke-width:1px
	style thread2 fill:#fe2,stroke:#000,stroke-width:1px
	style thread3 fill:#fe2,stroke:#000,stroke-width:1px
	style req1 fill:#2dd,stroke:#000,stroke-width:1px
	style req2 fill:#2dd,stroke:#000,stroke-width:1px
	style req3 fill:#2dd,stroke:#000,stroke-width:1px
```

#### Tuần tự

```mermaid
graph LR;
	img1(Trang 1) --> req(RequestSender)
	img2(Trang 2) -.->|sau trang 1| req
	img3(Trang 3) -.->|sau trang 2| req
	
	subgraph step2[Bước 2: Gửi tới Tesseract Reader]
		req
	end
	
	subgraph paraapi[Tesseract Reader]
		req --> api("Module tiếp theo")
	end
	
	style req fill:#2dd,stroke:#000,stroke-width:1px
	style req fill:#2dd,stroke:#000,stroke-width:1px
	style req fill:#2dd,stroke:#000,stroke-width:1px
```


### Tesseract Paragraph Extract
Bước 1:
```mermaid
graph LR;
	img(Trang) -->|POST HTTP| controller(ParagraphExtractController)
	
	subgraph step1[Bước 1: Tách đoạn văn]
		controller --> para-extractor(ParagraphExtractor)
		para-extractor --> img1(Đoạn 1)
		para-extractor --> img2(Đoạn 2)
		para-extractor --> img3(Đoạn 3)
	end
	
	style para-extractor fill:#fe2,stroke:#000,stroke-width:1px
	style controller fill:#fe2,stroke:#000,stroke-width:1px
```
Bước 2:
```mermaid
graph LR;
	img1(Đoạn 1) --> tessasync(TessApiAsyncRequest)
	img2(Đoạn 2) --> tessasync
	img3(Đoạn 3) --> tessasync
	
	subgraph step2[Bước 2: Gửi song song tới API tách đoạn]
		tessasync -->|Tạo luồng xử lý đoạn 1| thread1(TessApiThreadRunner)
		tessasync -->|Tạo luồng xử lý đoạn 2| thread2(TessApiThreadRunner)
		tessasync -->|Tạo luồng xử lý đoạn 3| thread3(TessApiThreadRunner)
		thread1 --> req1(RequestSender)
		thread2 --> req2(RequestSender)
		thread3 --> req3(RequestSender)
	end
	
	subgraph paraapi[Tesseract Reader]
		req1 --> api("Module tiếp theo")
		req2 --> api
		req3 --> api
	end
	
	style tessasync fill:#fe2,stroke:#000,stroke-width:1px
	style thread1 fill:#fe2,stroke:#000,stroke-width:1px
	style thread2 fill:#fe2,stroke:#000,stroke-width:1px
	style thread3 fill:#fe2,stroke:#000,stroke-width:1px
	style req1 fill:#2dd,stroke:#000,stroke-width:1px
	style req2 fill:#2dd,stroke:#000,stroke-width:1px
	style req3 fill:#2dd,stroke:#000,stroke-width:1px
```

### Tesseract Reader
```mermaid
graph LR;
	img(Ảnh) -->|POST HTTP| controller(ReaderController)
	
	subgraph step1[Đọc ảnh]
		controller --> tess-reader(TessReaderService)
	end
	
	tess-reader --> res(Kết quả dạng String)
	
	style tess-reader fill:#fe2,stroke:#000,stroke-width:1px
	style controller fill:#fe2,stroke:#000,stroke-width:1px
```