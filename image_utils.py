import numpy as np
import os
import cv2
import math
import matplotlib.pyplot as plt

def load_images(image_dir="test_set"):
    # Load ảnh
    images = []
    fileList = os.listdir(image_dir)
    for file in fileList:
        # Kiểm tra xem tên file có "pic" không, tránh load phải .ipynb_checkpoints
        if not "pic" in file:
            continue

        print(image_dir + "/" + file)
        image = cv2.imread(image_dir + "/" + file)
        images.append(image)
    return images   


def detect_orientation(image : np.ndarray, max_size = (400, 400)):
    # Thu nhỏ kích cỡ ảnh để thuật toán phần sau chạy nhanh hơn
    max_width, max_height = max_size
    height, width = image.shape
    if width > max_width:
        height = int(height * (max_width / width))
        width = max_width
    if height > max_height:
        width = int(width * (max_height / height))
        height = max_height
    
    image = cv2.resize(src=image, dsize=(width, height))

    lines = cv2.HoughLinesP(image=image, rho=2, theta=math.pi/180, threshold=80, minLineLength=100, maxLineGap=5)
    # lines = cv2.HoughLines(image=image, rho=1, theta=math.pi/180, threshold=80, min_theta= -math.pi/2, max_theta=math.pi/2)
    plt.imshow(image)
    avg_angle = 0
    for line in lines:
        for x_start, y_start, x_end, y_end in line:
            plt.plot((x_start, x_end), (y_start, y_end))
            dy = np.abs(y_end - y_start)
            dx = np.abs(x_end - x_start)
            avg_angle += np.arctan(dy / dx)
    
    avg_angle /= len(lines)
    plt.title("Orientation prediction: " + str(avg_angle))
    plt.show()
    return avg_angle


def orient_image(image : np.ndarray, offset_angle):
    # Xác định kích cỡ ảnh mới
    width, height = image.shape[:2]
    new_width = int(height * np.sin(offset_angle) + width * np.cos(offset_angle))
    new_height = int(width * np.sin(offset_angle) + height * np.cos(offset_angle))
    max_width = width + height
    max_height = width + height
    
    # Dịch chuyển ảnh ra giữa để chuẩn bị xoay
    shift_matrix = np.asarray([[1, 0, (max_height - height)//2], 
                               [0, 1, (max_width - width)//2]],dtype=np.float32)
    image = cv2.warpAffine(src=image, M=shift_matrix, dsize=(max_height, max_width), borderMode=cv2.BORDER_REPLICATE)
    
    # Xoay
    angle = np.rad2deg(offset_angle)
    rotation_matrix = cv2.getRotationMatrix2D(center=(max_height//2, max_width//2), angle=angle, scale=1)
    image = cv2.warpAffine(src=image, M=rotation_matrix, dsize=(max_height, max_width), borderMode=cv2.BORDER_REPLICATE)

    # Crop về cho vừa cỡ ảnh
    crop_matrix = np.asarray([[1, 0, (new_height - max_height)//2],
                              [0, 1, (new_width - max_width)//2]], dtype=np.float32)
    image = cv2.warpAffine(src=image, M=crop_matrix, dsize=(new_height, new_width), borderMode=cv2.BORDER_REPLICATE)
    plt.imshow(image, cmap='gray')
    plt.show()
    return image