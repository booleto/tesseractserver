import string
import math
import os
import json
import cv2
import skimage.data
import random as rd

import fontTools.ttLib as ttl
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt
import PIL.Image as PImage
import PIL.ImageFont as PFont
import PIL.ImageDraw as PDraw
import PIL.ImageColor as PColor
from collections import Iterable

import keras_ocr
from skimage import transform

def generate_text_box(alphabet, font, size=7, lines=8, rotX=0, rotY=0, rotZ=0):
    """Tạo một textbox để test. Textbox ở vị trí ngẫu nhiên và xoay nhẹ trong không gian 3D
    alphabet: bảng chữ cái để tạo test, ở dạng string
    font: đường dẫn đến file font (.ttf)
    lines: số dòng của textbox
    """
    font_bound = (12, 24)
    rotationX_bound = (-rotX, rotX)
    rotationY_bound = (-rotY, rotY)
    rotationZ_bound = (-rotZ, rotZ)

    text_generator = keras_ocr.data_generation.get_text_generator(alphabet=alphabet, max_string_length=50)
    background = PImage.open('backgrounds/background.png')

    font_size = np.random.randint(font_bound[0], font_bound[1])
    texts = [next(text_generator) for i in range(lines)]
    text_boxes = [generate_text_mask(texts[j], font, font_size, font_align='left') for j in range(lines)]
    unpack = lambda boxes: [box.size for box in boxes]

    box_x, box_y = max(unpack(text_boxes))
    bg_x, bg_y = background.size
    box_bound_x = np.random.randint(low=0, high=max(bg_x - box_x, 1))
    box_bound_y = np.random.randint(low=0, high=max(bg_y - box_y * lines, 1))

    print("max box: ", box_x, box_y)
    print("background size: ", bg_x, bg_y)
    print("box bound: ", box_bound_x, box_bound_y)
    print(texts)

    for text_box in text_boxes:
            box_bound_y += box_y
            background.paste(im=text_box, box=(box_bound_x, box_bound_y))

    if (rotX, rotY, rotZ) == (0,0,0):
        return np.asarray(background)

    # Tạo textbox bị xoay, chuyển về dạng np array
    background = PImage.fromarray((255 * transform_image(
        background,
        thetaX = np.random.uniform(rotationX_bound[0], rotationX_bound[1]),
        thetaY = np.random.uniform(rotationY_bound[0], rotationY_bound[1]),
        thetaZ = np.random.uniform(rotationZ_bound[0], rotationZ_bound[1])
    )).astype(np.int8), mode='RGB')

    return np.asarray(background)


def generate_text_mask(
        text,
        font_filepath,
        font_size,
        color=(0,0,0),
        font_align="center"
    ):
    """Tạo 1 dòng textbox
    text: Chữ của textbox
    font_filepath: path đến font (phải là .ttf)
    font_size: cỡ font
    color: màu (dạng RGB)
    font_align: căn chữ so với dòng
    """
    # Tạo textbox
    font = PFont.truetype(font_filepath, size=font_size)
    box = font.getsize_multiline(text)
    print("Box: ", box)
    img = PImage.new(mode='RGB', size=(box[0], int(box[1] * 1.1)), color=(255, 255, 225))
    draw = PDraw.Draw(img)

    draw_point = (0, 0)
    draw.text(draw_point, text=text, fill=color, anchor='lt', font=font, align=font_align)
    # draw.multiline_text(xy=draw_point, text=text, fill=color, anchor='la', font=font, align=font_align)
    return img


def transform_image(image, thetaX=0, thetaY=0, thetaZ=0):
    """Xoay ảnh theo 3 góc euler. Gốc tọa độ là góc trên bên trái ảnh
    image: ảnh
    thetaX: độ xoay theo trục X
    thetaY: độ xoay theo trục Y
    thetaZ: độ xoay theo trục Z

    Đơn vị là radian, thetaX và thetaY nên thuộc (-0.0002, 0.0002), thetaZ nên thuộc (-0.002, 0.002) để ko xoay quá nhiều
    """
    if (thetaX, thetaY, thetaZ) == (0,0,0):
        return np.asarray(image)

    #Ma trận xoay
    R_x = np.array([[1, 0, 0],
                    [0, math.cos(thetaX), -math.sin(thetaX)],
                    [0, math.sin(thetaX), math.cos(thetaX)]])

    R_y = np.array([[math.cos(thetaY), 0, math.sin(thetaY)],
                    [0, 1, 0],
                    [-math.sin(thetaY), 0, math.cos(thetaY)]])

    R_z = np.array([[math.cos(thetaZ), -math.sin(thetaZ), 0],
                    [math.sin(thetaZ), math.cos(thetaZ), 0],
                    [0, 0, 1]])

    rotation_matrix = np.matmul(R_x, np.matmul(R_y, R_z))

    # Xoay ảnh
    image = np.asarray(image, dtype=np.uint8) # phải chuyển về np array để tránh lỗi
    tform = transform.ProjectiveTransform(matrix=rotation_matrix)
    tf_img = transform.warp(image, tform.inverse)

    # Vẽ ảnh đã được xoay
    # fig, ax = plt.subplots()
    # ax.imshow(tf_img)
    # ax.set_title('Ảnh đã biến đổi')
    # plt.show()
    return tf_img

def main():
    alphabet=string.digits + string.ascii_letters + '!?. '
    font = "custom_font/arial.ttf"
    ft_image = generate_text_box(alphabet, font, size=1)
    plt.imshow(ft_image)
    plt.show()

if __name__ == '__main__':
    main()