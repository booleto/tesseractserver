import string
import math
import os
import json
import cv2
import skimage.data
import random as rd
import io

from fastapi import FastAPI, Form, UploadFile, File, Response
import fontTools.ttLib as ttl
import numpy as np
import tensorflow as tf
import logging as logger
import matplotlib.pyplot as plt
import PIL.Image as PImage
import PIL.ImageFont as PFont
import PIL.ImageDraw as PDraw
import PIL.ImageColor as PColor
from collections import Iterable

import keras_ocr
from image_utils import load_images


def is_same_line(box1, box2):
    box1_ymax = box1[2][1]
    box1_ymin = box1[1][1]
    box2_ymax = box2[3][1]
    box2_ymin = box2[0][1]
    box2_ymean = (box2_ymax + box2_ymin) / 2
    box1_height = box1_ymax - box1_ymin


    if not box1_ymax - box1_height/4 > box2_ymean > box1_ymin + box1_height/4:
        return False

    # box1_xmax = box1[1][0]
    # box2_xmin = box2[0][0]
    # boxes_xdist = box2_xmin - box1_xmax
    
    # if boxes_xdist > box1_height * 5:
    #     return False

    return True


def segment_by_lines(prediction_group):
    lines = []
    current_line = []
    last_box = prediction_group[0][1]

    for prediction in prediction_group:
        if len(current_line) == 0 or is_same_line(last_box, prediction[1]):
            current_line.append((prediction[1], prediction[0]))    
        else:
            lines.append(current_line)
            current_line = []
            current_line.append((prediction[1], prediction[0]))
        last_box = prediction[1]
    lines.append(current_line)

    return [fix_line(line) for line in lines]

def fix_line(line):
    line = [(keras_ocr.tools.get_rotated_box(box)[0], character) for box, character in line]
    centers = np.array([box.mean(axis=0) for box, _ in line])
    sortedx = centers[:, 0].argsort()
    sortedy = centers[:, 1].argsort()
    if np.diff(centers[sortedy][:, 1]).sum() > np.diff(centers[sortedx][:, 0]).sum():
        return [line[idx] for idx in sortedy]
    return [line[idx] for idx in sortedx]


def read_image(images, pipeline):
    # rgb_images = []
    # Thay channel alpha bằng kênh màu trắng
    # for image in images:
        # print(image.size)
        # img = PImage.new('RGB', image.size, (255, 255, 255))
        # img.paste(image, None)
        # image = img

        # trans_mask = image[:,:,3] == 0
        # image[trans_mask] = [255,255,255,255]
        # rgb_images.append(cv2.cvtColor(image, cv2.COLOR_BGRA2RGB))
        # img = PImage.fromarray()

    prediction_groups = pipeline.recognize(images)
    # print(prediction_groups)
    save_prediction_groups(prediction_groups, filename="result")

    for i in range(len(images)):
        fig, ax = plt.subplots(figsize=(30,30))
        keras_ocr.tools.drawAnnotations(image=images[i], predictions=prediction_groups[i], ax=ax)
        plt.savefig("result.png")


def save_prediction_groups(prediction_groups, filename="result"):
    groups = []
    for prediction in prediction_groups:
        textbox_list = []
        for text, box in prediction:
            box = box.tolist()
            textbox_list.append((text, box))
        groups.append(textbox_list)

    with open(filename + ".json", "w") as writefile:
        json.dump(groups, writefile)


def load_result(filename="result"):
    with open(filename + ".json", 'r') as loadfile:
        return json.load(loadfile)


def result_by_lines():
    """In kết quả đã đọc ra theo dòng. Mỗi dòng là 1 list các từ
    """
    data = load_result()
    for prediction_group in data:
        return [[text for box, text in line] for line in segment_by_lines(prediction_group=prediction_group)]

def result_by_string():
    """In kết quả đã đọc giống như văn bản gốc, dạng string
    """
    result = ""
    for line in result_by_lines():
        for text in line:
            result = result + " " + text
        result = result + "\n"
    return result

def init_pipeline():
    """Khởi tạo pipeline
    """
    # Build detector và recognizer
    # detector = keras_ocr.detection.Detector(weights='clovaai_general')
    # recognizer = keras_ocr.recognition.Recognizer()
    # recognizer.compile()

    # #Dựng pipeline
    # pipeline = keras_ocr.pipeline.Pipeline(detector=detector, recognizer=recognizer)
    pipeline = keras_ocr.pipeline.Pipeline()

    # Khóa các trọng số
    # for layer in recognizer.backbone.layers:
    #     layer.trainable = False

    return pipeline


def read_image_from_test(pipeline=init_pipeline()):
    """Đọc ảnh từ file test_set
    pipeline: pipeline keras_ocr, nếu không truyền vào thì tạo pipeline mới
    """
    read_image(load_images(), pipeline)
    return result_by_lines()


def read_image_from_path(file_path, pipeline=init_pipeline()):
    """Đọc ảnh từ đường dẫn file, lưu vào result.json
    file_path: Đường dẫn
    pipeline: pipeline keras_ocr, nếu không truyền vào thì tạo pipeline mới
    """
    read_image(load_images(file_path), pipeline)
    return result_by_lines()


def read_image_from_files(images, pipeline=init_pipeline()):
    """Đọc ảnh trực tiếp
    image: ảnh cần đọc (dạng PIL Image, bắt buộc dùng RGB)
    pipeline: pipeline keras_ocr, nếu không truyền vào thì tạo pipeline mới
    """
    np_images = []

    if not isinstance(images, Iterable):
        np_images = [np.asarray(images)]
    else:
        for image in images:
            np_images.append(np.asarray(image))
    
    read_image(np_images, pipeline)


def main():
    # read_autogenerated_image()
    img = load_images()
    img = img[5]
    img = PImage.fromarray(img, mode='RGB')
    read_image_from_files(img)
    print(result_by_lines())
    pass

if __name__=="__main__":
    main()