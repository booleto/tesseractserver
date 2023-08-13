import string
import math
import os
import json
import cv2
import skimage.data
import random as rd

import numpy as np
import matplotlib.pyplot as plt
import PIL.Image as PImage
import PIL.ImageFont as PFont
import PIL.ImageDraw as PDraw
import PIL.ImageColor as PColor

from skimage import transform

#Mở file từ điển và đưa vào dict_arr
dictfile = "ocr_tester/words.txt"
dict_arr = []
with open(dictfile, encoding="UTF-8") as df:
    for line in df:
        dict_arr.append(line[:-1])

def get_rand_word():
    """
    Lấy từ ngẫu nhiên trong từ điển
    """
    return dict_arr[rd.randint(0, len(dict_arr) - 1)]

def get_rand_str(max_size=50):
    """
    Lấy string ngẫu nhiên thỏa mãn độ dài
    """
    ret = ""
    while True:
        word = get_rand_word()
        if len(ret) + len(word) + 1 > max_size:
            return ret[:-1]
        else:
            ret = ret + get_rand_word() + " "


def generate_text_box(font, size=7, lines=8):
    """Tạo một textbox để test. Textbox ở vị trí ngẫu nhiên và xoay nhẹ trong không gian 3D
    alphabet: bảng chữ cái để tạo test, ở dạng string
    font: đường dẫn đến file font (.ttf)
    lines: số dòng của textbox
    """
    font_bound = (16, 24)
    rotationX_bound = (0, 0)
    rotationY_bound = (0, 0)
    rotationZ_bound = (-0.01, 0.01)

    background = PImage.open('ocr_tester/backgrounds/background.png')

    #Tạo chữ trên ảnh
    font_size = np.random.randint(font_bound[0], font_bound[1])
    texts = [get_rand_str() for i in range(lines)]
    text_boxes = [generate_text_mask(texts[j], font, font_size, font_align='left') for j in range(lines)]
    unpack = lambda boxes: [box.size for box in boxes]

    box_x, box_y = max(unpack(text_boxes))
    bg_x, bg_y = background.size
    box_bound_x = np.random.randint(low=0, high=max(bg_x - box_x, 1))
    box_bound_y = np.random.randint(low=0, high=max(bg_y - box_y * lines, 1))

    # print("max box: ", box_x, box_y)
    # print("background size: ", bg_x, bg_y)
    # print("box bound: ", box_bound_x, box_bound_y)
    # print(texts)

    for text_box in text_boxes:
            box_bound_y += box_y
            background.paste(im=text_box, box=(box_bound_x, box_bound_y))

    # Tạo textbox bị xoay, chuyển về dạng np array
    background = PImage.fromarray((255 * transform_image(
        background,
        thetaX = np.random.uniform(rotationX_bound[0], rotationX_bound[1]),
        thetaY = np.random.uniform(rotationY_bound[0], rotationY_bound[1]),
        thetaZ = np.random.uniform(rotationZ_bound[0], rotationZ_bound[1])
    )).astype(np.int8), mode='RGB')

    return np.asarray(background), texts


def generate_text_mask(
        text,
        font_filepath,
        font_size,
        color=(0,0,0),
        font_align="center"
    ):
    """Tạo từng dòng trong textbox
    """
    # Tạo textbox
    font = PFont.truetype(font_filepath, size=font_size)
    box = font.getbbox(text)
    img = PImage.new(mode='RGB', size=(box[2], box[3]), color=(255, 255, 225)) # box là tuple(left, top, right, bottom)
    draw = PDraw.Draw(img)

    draw_point = (0, 0)
    draw.text(draw_point, text=text, fill=color, anchor='lt', font=font, align=font_align)
    return img


def transform_image(image, thetaX=0, thetaY=0, thetaZ=0):
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

def test_autogen(count=5):
    font = 'ocr_tester/custom_font/arial.ttf'
    text_res = []
    img_res = []
    for i in range(0, count):
        img, text = generate_text_box(font, lines=20, size=1)
        img = PImage.fromarray(img)
        img.save("ocr_tester/generated_test/" + str(i) + ".png")
        img_res.append(img)
        text_res.append(text)
    return img_res, text_res

def main():
    font = 'ocr_tester/custom_font/arial.ttf'
    font_size = 16

    img, text = generate_text_box(font, lines=20, size=1)
    img = PImage.fromarray(img)
    img.save("ocr_tester/generated_test/odf.png")

if __name__ == "__main__":
    main()
