from fastapi import FastAPI, Form, UploadFile, File, Response
from request_handler import RequestHandler
import numpy as np
import httpx
import cv2
import asyncio
import io
import PIL.Image as PImage
import logging as logger
import matplotlib.pyplot as plt

import lines_separator.lines_separator as line_s

app = FastAPI()
logger.basicConfig(level=logger.INFO)
tika_server_url = "http://10.14.222.194:19998"


@app.post("/read/image/tika")
async def read_image_by_lines(body="", uploaded_file : UploadFile = File(...)):
    tasks = []
    results = {}
    req = RequestHandler()
    
    image = uploaded_file.file.read()
    image = io.BytesIO(image)
    image = PImage.open(image).convert('RGB')
    image = np.array(image)

    # plt.imshow(image)
    # plt.savefig("source.png")

    line_boxes = line_s.extract_text_lines(image)
    line_crops = line_s.segment_image_by_lines(image, line_boxes)
    for line_crop in line_crops:
        line_crop = line_s.preprocess_ocr(line_crop)
        tasks.append(asyncio.ensure_future(req.send_to_tika(line_crop, url=tika_server_url)))
        # tasks.append(asyncio.ensure_future(req.request(1)))
        print("Thread started")

    await asyncio.wait(tasks)
    print(req.returns)
    
    for i in range(req.returns.qsize()):
        res = req.returns.get(i)
        print(res)
        results[i] = res
        # results.append(req.returns.get(i))
    return results