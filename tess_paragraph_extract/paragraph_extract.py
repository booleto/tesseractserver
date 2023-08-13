import PIL.Image as PImage
from fastapi import UploadFile
from tesserocr import PyTessBaseAPI, RIL

def extract_pic(image : PImage):
    with PyTessBaseAPI() as api:
        api.SetImage(image)
        boxes = api.GetComponentImages(RIL.TEXTLINE, True)
        print('Found {} textline image components.'.format(len(boxes)))
        for i, (im, box, _, _) in enumerate(boxes):
            # im is a PIL image object
            # box is a dict with x, y, w and h keys
            api.SetRectangle(box['x'], box['y'], box['w'], box['h'])
            ocrResult = api.GetUTF8Text()
            conf = api.MeanTextConf()
            print(u"Box[{0}]: x={x}, y={y}, w={w}, h={h}, "
                "confidence: {1}, text: {2}".format(i, conf, ocrResult, **box))
            

def convert_uploaded_image(image : UploadFile):
    image = image.file.read()
    image = PImage.open(image)
    return image