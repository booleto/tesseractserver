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

    async def send_read_request(self, image, url):
        # image_bytes = image
        uploaded_file = {
            'params' : 'none',
            'image' : image
        }
        async with httpx.AsyncClient() as client:
            result = await client.post(url=url, files=uploaded_file, timeout=None)
            self.returns.put(result.text)

    async def send_post_request(self, body, url):
        async with httpx.AsyncClient() as client:
            result = await client.post(url=url, content=body, timeout=None)
            self.returns.put(result.text)
