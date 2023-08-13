import cv2
import PIL.Image as PImage
import matplotlib.pyplot as plt
from matplotlib import transforms
import numpy as np
import math
import os
import httpx
import keras_ocr

from image_utils import load_images

# def paragraph_extract_preprocess(image : np.ndarray, min_size=(1080, 1080)):
#     """
#     """
#     image = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
#     kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5,5))
#     image = cv2.GaussianBlur(image, kernel.shape, 0)
#     image = cv2.medianBlur(image, 5)
#     thres, image = cv2.threshold(image, 0, 204, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

#     plt.imshow(image, cmap='gray')
#     plt.show()
#     return image

# def extract_paragraphs(image : np.ndarray):
#     # Erode để phân tách dòng
#     kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (11,1))
#     image = cv2.erode(image, kernel, iterations=4)
#     contours, hierarchy = cv2.findContours(image, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
#     plt.imshow(image, cmap='gray')
#     plt.show()
    
    # avg_height = 0
    # for contour in contours:
    #     x, y, width, height = cv2.boundingRect(contour)
    #     cv2.rectangle(image, (x, y, width, height), color=(0, 0, 255), thickness=3)
    #     avg_height += height
    # avg_height = int(avg_height /(12 * len(contours))) + 1
    # print(avg_height)

    # kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (1,avg_height))
    # image = cv2.erode(image, kernel, iterations=4)
    # plt.imshow(image, cmap='gray')
    # plt.show()

    # contours, hierarchy = cv2.findContours(image, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    # return contours     


def segment_image_by_lines(image : np.ndarray, line_boxes) -> list:
    """tách ảnh theo dòng, trả về ảnh crop theo dòng
    
    image: ảnh gốc
    line_boxes: danh sách vị trí các dòng, trả về ở extract_text_lines()
    """
    # plt.imshow(img, cmap='gray')
    # plt.show()
    # preprocessed_img = paragraph_extract_preprocess(img)
    # # print(pytesseract.image_to_osd(preprocessed_img))
    # # offset = detect_orientation(preprocessed_img)
    # # reoriented_img = orient_image(preprocessed_img, offset)
    # # reoriented_original_img = orient_image(img, offset)

    # line_crops = []
    # y_list = []
    # contours = extract_paragraphs(preprocessed_img)
    # for contour in contours:
    #     x, y, width, height = cv2.boundingRect(contour)
    #     if height < img.shape[1]/100:
    #         continue

    #     # cv2.rectangle(img, (x, y, width, height), color=(0, 0, 255), thickness=3)
    #     line_crops.append(img[y : y+height, x : x+width])
    #     y_list.append(y)
    
    # line_crops.pop(0) # Contour đầu tiên là toàn bộ ảnh, bỏ ra vì ko cần
    # y_list.pop(0)
    # # Sắp xếp từ trên xuống dưới
    # args_sorted = np.argsort(y_list)
    # line_crops = [line_crops[i] for i in args_sorted]
    # return line_crops

    image = cv2.cvtColor(image, code=cv2.COLOR_RGB2GRAY)
    line_crops = []
    for line_box in line_boxes:
        rect = cv2.boundingRect(line_box)
        x, y, w, h = rect
        crop = image[y : y+h, x : x+w].copy()

        line_box = line_box - line_box.min(axis=0)
        mask = np.zeros((h, w), dtype=np.uint8)
        cv2.drawContours(mask, [line_box], -1, (255, 255, 255), -1, cv2.LINE_AA)
        # print(crop)
        line_image = cv2.bitwise_and(crop, crop, mask=mask)
        
        background = np.ones_like(crop, dtype=np.uint8)*255
        cv2.bitwise_not(background, background, mask=mask)
        line_image = line_image + background
        line_crops.append(line_image)

    # image = cv2.cvtColor(image, code=cv2.COLOR_RGB2GRAY)
    # line_crops = []
    # for boxes in line_boxes:
    #     points = []
    #     for point in boxes:
    #         points.append(point)

    #     points = np.asarray(points, dtype=np.uint8)
    #     print(points)

    #     rect = cv2.boundingRect(points)
    #     x, y, w, h = rect
    #     crop = image[y : y+h, x : x+w]

    #     mask = np.zeros((h,w), dtype=np.uint8)
    #     for box in line_boxes:
    #         cv2.drawContours(mask, [box], -1, (255,0,0), -1, cv2.LINE_AA)
            
    #     line_image = cv2.bitwise_and(crop, crop, mask=mask)

    #     background = np.ones_like(crop, dtype=np.uint8)*255
    #     cv2.bitwise_not(background, background, mask=mask)
    #     line_image = line_image + background
    #     line_crops.append(line_image)
    
    return line_crops


def separate_lines(image : np.ndarray, threshold = 200):
    '''tìm ranh giới các dòng, sắp xếp từ trên xuống dưới. Trả về list các (y1, y2), y1 là cận trên dòng, y2 là cận dưới dòng
    
    image: ảnh rgb
    '''
    image = cv2.cvtColor(image, code=cv2.COLOR_RGB2GRAY)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5,5))
    image = cv2.GaussianBlur(image, kernel.shape, 0)
    image = cv2.medianBlur(image, 5)
    thres, image = cv2.threshold(image, 0, 204, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (11,1))
    image = cv2.dilate(image, kernel, iterations=4)
    histogram = cv2.reduce(image, dim=1, rtype=cv2.REDUCE_AVG)
    histogram = [math.pow(x[0], 2) for x in histogram]
    # plt.imshow(image,cmap='gray')
    plt.plot(histogram)
    plt.savefig("debug_images/histogram.png")

    H,W = image.shape[:2]
    uppers = [y for y in range(H-1) if histogram[y]<=threshold and histogram[y+1]>threshold]
    lowers = [y for y in range(H-1) if histogram[y]>threshold and histogram[y+1]<=threshold]

    debug_image = cv2.cvtColor(image.copy(), cv2.COLOR_GRAY2BGR)
    for y in uppers:
        cv2.line(debug_image, (0,y), (W, y), (255,0,0), 1)

    for y in lowers:
        cv2.line(debug_image, (0,y), (W, y), (0,255,0), 1)

    cv2.imwrite("debug_images/line_sep_res.png", debug_image)

    # plt.imshow(image)
    # plt.show()

    lines = [(y1, y2) for y1, y2 in zip(uppers, lowers)]
    return lines


def keras_recognize(image : np.ndarray):
    """nhận diện vị trí chữ bằng Keras_ocr
    
    image: ảnh, dạng numpy array
    """
    debug_image = image.copy()
    image, scale_factor = preprocess_osd(image) 
    image = cv2.cvtColor(image, code=cv2.COLOR_GRAY2RGB)
    recognizer = keras_ocr.detection.Detector()
    prediction_groups = recognizer.detect([image])

    groups = []
    for prediction in prediction_groups:
        boxes = []
        for box in prediction:
            unscaled_box = [(x / scale_factor, y / scale_factor) for x, y in box]
            unscaled_box = np.array(unscaled_box, dtype=np.int32)
            boxes.append(unscaled_box)
        groups.append(boxes)

    debug_image = keras_ocr.tools.drawBoxes(debug_image, boxes, color=(255,0,0), thickness=1, boxes_format='boxes')
    cv2.imwrite("debug_images/osd_result.png", debug_image)

    return groups


def categorize_by_lines(lines : list, prediction_groups):
    """Phân nhóm các hộp chứa chữ theo dòng

    lines: danh sách dòng, trả về từ separate_lines()
    prediction_groups: danh sách nơi có chữ, trả về từ keras_recognize()
    """
    box_by_lines = [[] for line in lines]
    for prediction in prediction_groups:
        for box in prediction:
            v1, v2, v3, v4 = box
            centroid = (v1[1] + v2[1] + v3[1] + v4[1]) / 4
            line_idx = 0
            for line_y1, line_y2 in lines:
                if line_y1 <= centroid <= line_y2:
                    # print("box: ", box, "IS in line", line_idx, "because", line_y1, line_y2)
                    box_by_lines[line_idx].append(box)
                    break
                else:
                    # print("box: ", box, "NOT in line", line_idx, "because", line_y1, line_y2)
                    line_idx += 1
    return box_by_lines


def preprocess_osd(image : np.ndarray, min_size=(2160,2160)):
    if len(image.shape) == 3:
        image = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
    total_scale_factor = 1

    min_width, min_height = min_size
    height, width = image.shape[:2]

    if width < min_width:
        scale_factor = min_width / width
        height = int(height * scale_factor)
        width = min_width
        total_scale_factor *= scale_factor
    if height < min_height:
        scale_factor = min_height / height
        width = int(width * scale_factor)
        height = min_height
        total_scale_factor *= scale_factor

    # while width < min_width or height < min_height:
    #     width *= 2
    #     height *= 2
    #     total_scale_factor *= 2
    
    image = cv2.resize(src=image, dsize=(width, height), interpolation=cv2.INTER_CUBIC)
    return image, total_scale_factor


def preprocess_ocr(image : np.ndarray, min_size=(320,320)):
    """Tiền xử lý ảnh trước khi OCR, trả về ảnh đã xử lý

    image: ảnh gốc, dạng numpy array
    min_size: kích cỡ tối thiểu (chiều cao, chiều dài). Đối với OCR nên để ở khoảng 
    """
    if len(image.shape) == 3:
        image = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
    
    image, scale_factor = preprocess_osd(image, min_size=min_size)
    thres, image = cv2.threshold(image, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    # kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3,3))
    # image = cv2.GaussianBlur(image, kernel.shape, 0)
    # thres, image = cv2.threshold(image, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    # image = cv2.morphologyEx(image, cv2.MORPH_CLOSE, kernel)
    return image


def send_to_tika(image : np.ndarray):
    is_success, image_buffer = cv2.imencode(".jpeg", image)
    image_bytes = image_buffer.tobytes()
    with httpx.Client() as client:
        headers = {
            "Accept": "text/plain",
            "X-Tika-OCRPageSegMode": "7", #single text line
            "X-Tika-OCRLanguage": "vie"
        }
        return client.put(url="http://10.15.10.123:9998/tika", data=image_bytes, headers=headers, timeout=None).text


def extract_text_lines(image : np.ndarray):
    """tìm dòng, trả về polygon bao quanh mỗi chữ dưới dạng list

    image: ảnh dạng numpy array
    """
    lines = separate_lines(image)
    prediction_groups = keras_recognize(image)
    box_by_lines = categorize_by_lines(lines, prediction_groups)
    line_boxes = []
    for line in box_by_lines:
        line_box = []
        for box in line:
            for point in box:
                line_box.append(point)
        if len(line_box) == 0:
            continue
        box = np.asarray(line_box, dtype=np.int32)
        rectangle = cv2.minAreaRect(box.copy())
        box = cv2.boxPoints(rectangle)
        box = np.array(box, dtype=np.int32)
        line_boxes.append(box)
    
    debug_image = image.copy()
    debug_image = cv2.polylines(debug_image, line_boxes, isClosed=True, color=(0, 0, 255), thickness=2)
    cv2.imwrite("debug_images/polygon.png", debug_image)

    return line_boxes


def read_image_by_lines(image : np.ndarray):
    """OCR ảnh, sắp xếp theo từng dòng

    image: ảnh, dạng numpy array
    """
    plt.imshow(image)
    plt.savefig("debug_images/source.png")
    results = []
    line_boxes = extract_text_lines(image)
    line_crops = segment_image_by_lines(image, line_boxes)
    for line_crop in line_crops:
        line_crop = preprocess_ocr(line_crop)
        result = send_to_tika(line_crop)
        results.append(result)
        
    print(results)
    return results


def main():
    img = load_images()
    img = img[3]
    results = []
    line_boxes = read_image_by_lines(img)

    # box = [[910,1969],
    #         [934,1946],
    #         [1000,2016],
    #         [976,2039],
    #         [1086,1958],
    #         [1124,1958],
    #         [1124,1987],
    #         [1086,1987],
    #         [1067,2028],
    #         [1153,1952],
    #         [1177,1978],
    #         [1092,2055],
    #         [1004,2018],
    #         [1080,2018],
    #         [1080,2047],
    #         [1004,2047],
    #         [1149,2038],
    #         [1211,2038],
    #         [1211,2066],
    #         [1149,2066],
    #         [1092,2043],
    #         [1137,2043],
    #         [1137,2066],
    #         [1092,2066]]

    # box = np.asarray(box, dtype=np.int32)
    # rectangle = cv2.minAreaRect(box.copy())
    # print(rectangle)
    # read_image_by_lines(img)


if __name__ == '__main__':
    main()