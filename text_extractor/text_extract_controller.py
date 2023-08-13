from fastapi import FastAPI, Form, UploadFile, File, Response
from fastapi.middleware.cors import CORSMiddleware
import logging as logger
import io
import json
import time
import asyncio

from request_handler import RequestHandler
# from test_generator import generate_text_box
# from text_detector import read_image_from_files, read_image_from_test, read_image, load_result, result_by_string
from text_extractor.pdf_images_extract import extract_pdf_images, extract_pdf_images_as_bytes

app = FastAPI()
logger.basicConfig(level=logger.INFO)
keras_server_url = "http://10.14.222.194:12345"
line_sep_server_url = "http://10.14.222.194:12345"

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

@app.get("/test/threads")
async def test_multithread():
    req = RequestHandler()
    start = time.time()
    # threads = queue.Queue()
    tasks = []
    result = ""
    for i in range(6):
        # thread = threading.Thread(target=send_read_request, args=[i])
        tasks.append(asyncio.ensure_future(req.request(i)))
        print("Thread", i, "started")

    await asyncio.wait(tasks) # Đợi các task chạy xong
    for i in range(req.returns.qsize()):
        result = result + req.returns.get(i)
        print("status:", result)
    
    print(result)
    end = time.time()
    return end - start


@app.post("/read/pdf/keras")
async def process_pdf_request(body="", uploaded_file : UploadFile = File(...)):
    tasks = []
    request = RequestHandler()
    req_url = "/read/custom"
    result = {}

    contents = uploaded_file.file.read()
    pdf_images = extract_pdf_images_as_bytes(io.BytesIO(contents))
    for pages in pdf_images:
        for image in pages:
            tasks.append(asyncio.ensure_future(request.send_read_request(image, keras_server_url + req_url)))
            print("Thread started")
    
    await asyncio.wait(tasks)
    print(request.returns)
    for i in range(request.returns.qsize()):
        res = json.loads(request.returns.get(i))
        print(res)
        result["page " + str(i)] = res
    return result


@app.post("/read/pdf/tika")
async def process_pdf_image_tika(body="", uploaded_file : UploadFile = File(...)):
    tasks = []
    request = RequestHandler()
    result = {}
    endpoint = "/read/image/tika"

    contents = uploaded_file.file.read()
    pdf_images = extract_pdf_images_as_bytes(io.BytesIO(contents))
    for pages in pdf_images:
        for image in pages:
            print(image)
            # temp_file = io.BytesIO(image)
            # image = PImage.open(image).convert('RGB')
            # plt.imshow(image)
            # plt.show()
            tasks.append(asyncio.ensure_future(request.send_read_request(image, line_sep_server_url + endpoint)))
            print("Thread started")

    await asyncio.wait(tasks)
    print(request.returns)
    for i in range(request.returns.qsize()):
        res = json.loads(request.returns.get(i))
        print(res)
        result["page " + str(i)] = res
    return result