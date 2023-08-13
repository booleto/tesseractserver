import queue
import asyncio
import httpx
import cv2
import numpy as np
import time
import matplotlib.pyplot as plt

class RequestHandler:   
    def __init__(self) -> None:
        self.returns = queue.Queue()

    async def request(self, i):
        await asyncio.sleep(i)
        self.returns.put("oof" + str(i) + " ")

    async def send_read_request(self, image, url):
        # image_bytes = image
        uploaded_file = {'uploaded_file' : image}
        async with httpx.AsyncClient() as client:
            result = await client.post(url=url, files=uploaded_file, timeout=None)
            self.returns.put(result.text)

    
    async def send_post_request(self, body, url):
        async with httpx.AsyncClient() as client:
            result = await client.post(url=url, content=body, timeout=None)
            self.returns.put(result.text)


    async def send_to_tika(self, image : np.ndarray, url):
        image = cv2.cvtColor(image, cv2.COLOR_GRAY2RGB)

        is_success, image_buffer = cv2.imencode(".png", image)
        image_bytes = image_buffer.tobytes()
        start = str(time.time())
        cv2.imwrite("image_dump/" + start + ".png", image)
        # try:
        with httpx.Client() as client:
            headers = {
                "Content-Type": "image/png", 
                "Accept": "text/plain",
                "Connection": "keep-alive",
                "X-Tika-OCRPreserveInterwordSpacing" : "true",
                # "X-Tika-OCRTimeout": "300",
                "X-Tika-OCRPageSegMode": "7", #single text line
                "X-Tika-OCRLanguage": "vie"
            }
            result = client.put(url=url + "/tika", data=image_bytes, headers=headers, timeout=None)
            print(result.text)
            # print("Time required:", str(time.time() - start))
            self.returns.put(result.text.replace("\n", ""))
        # except Exception:
        #     print("exception: ", Exception.with_traceback)
