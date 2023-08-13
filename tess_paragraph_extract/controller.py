from fastapi import FastAPI, Form, UploadFile, File, Response
from request_handler import RequestHandler
import paragraph_extract as para_ex

app = FastAPI()

@app.post("/tesseract/paragraph")
async def read_image_by_lines(body="", uploaded_file : UploadFile = File(...)):
    image = para_ex.convert_uploaded_image(uploaded_file)   
    para_ex.extract_pic(image)
