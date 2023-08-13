import pytesseract
import cv2
import time

from pytesseract import Output

# from image_utils import load_images

pytesseract.pytesseract.tesseract_cmd = r'/usr/local/bin/tesseract'

def main():
    image = cv2.imread("../test_set/vpcp.png")
    config = r'--psm 1'
    result = {}
    start = time.time()
    # print(pytesseract.image_to_boxes(image, lang='vie'))
    # print(pytesseract.image_to_string(image, lang='vie'))
    # print(pytesseract.image_to_data(image, lang='vie', config=config))
    result = pytesseract.image_to_data(image, lang='vie', config=config, output_type=Output.DICT)
    print("Time take for doc:", time.time() - start)

    for i in range(len(result["top"])):
        if result["conf"][i] != -1 or result["level"][i] != 2:
            continue

        left, top, width, height = (result['left'][i], result['top'][i], result['width'][i], result['height'][i])
        print(left, top, width, height, result['text'][i])
        image = cv2.rectangle(image, (left, top), (left + width, top + height), color=(0, 255, 0))
    
    cv2.imwrite(img=image, filename="boxes.png")

    # image = cv2.imread("../test_set/french.PNG")
    # config = r'--psm 7'
    # start = time.time()
    # print(pytesseract.image_to_data(image))
    # print("Time take for line:", time.time() - start)


if __name__ == '__main__':
    main()