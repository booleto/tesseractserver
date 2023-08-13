import fitz
import PIL.Image as PImage
import io
import numpy as np
import matplotlib.pyplot as plt

def extract_pdf_images(bytesfile):
    pdf_file = fitz.open('pdf', bytesfile)

    images_in_pages = []
    for pages in pdf_file:
        image_list = []
        for image_ref in pages.get_images():
            xref = image_ref[0]
            image_data = pdf_file.extract_image(xref)
            image_bytes = image_data["image"]
            image = PImage.open(io.BytesIO(image_bytes)).convert('RGB')
            image_list.append(image)
        images_in_pages.append(image_list)
    return images_in_pages


def extract_pdf_images_as_bytes(bytesfile):
    pdf_file = fitz.open('pdf', bytesfile)

    images_in_pages = []
    for pages in pdf_file:
        image_list = []
        for image_ref in pages.get_images():
            xref = image_ref[0]
            image_data = pdf_file.extract_image(xref)
            image_bytes = image_data["image"]
            image = io.BytesIO(image_bytes)
            image_list.append(image)
        images_in_pages.append(image_list)
    return images_in_pages


def main():
    ls = extract_pdf_images("test_set/VanBanGoc_41.2017.NQ.QH14.pdf")
    for pages in ls:
        for image in pages:
            plt.imshow(image)
            plt.show()

if __name__=="__main__":
    main()