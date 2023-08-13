from fastapi import FastAPI, Form, UploadFile, File, Response
import asyncio
import io
import cv2
import os
import csv
import json
import numpy as np
import PIL.Image as PImage
import diff_match_patch as diffmp
import matplotlib.pyplot as plt

from ocr_tester.request_handler import RequestHandler
from ocr_tester.testgen import test_autogen


app = FastAPI()
TESS_URL = "http://10.14.222.194:12321/tesseract/upload"
PARA_URL = "http://10.14.222.194:8080/tess/paragraph/upload"
TEST_COUNT = 50

@app.get("/testtesspara")
async def send_imgs_to_tesseract_para():
    results = {}
    req = RequestHandler()
    
    images, texts = test_autogen(TEST_COUNT)

    for img in images:
        buffered = io.BytesIO()
        img.save(buffered, format="PNG")

        task = asyncio.ensure_future(req.send_read_request(buffered, url=PARA_URL))
        await asyncio.wait([task])
    
    for i in range(req.returns.qsize()):
        res = req.returns.get(i)
        # print(res)
        results[i] = res

    calc_error_para(results, texts)
    return results


@app.get("/testtess")
async def send_imgs_to_tesseract_para():
    results = {}
    req = RequestHandler()
    
    images, texts = test_autogen(TEST_COUNT)

    for img in images:
        buffered = io.BytesIO()
        img.save(buffered, format="PNG")

        task = asyncio.ensure_future(req.send_read_request(buffered, url=TESS_URL))
        await asyncio.wait([task])
    
    for i in range(req.returns.qsize()):
        res = req.returns.get(i)
        # print(res)
        results[i] = res

    calc_error(results, texts)
    return results


def calc_error_para(results, texts):
    # Khởi tạo Diff Match Patch
    dmp = diffmp.diff_match_patch()

    for i in range(len(results)):
        result_dict = json.loads(results[i])
        result_str = ""
        text_str = ""

        for key in result_dict.keys():
            result_str = result_str + str(result_dict[key])
        result_str = result_str[:-1]
        
        for line in texts[i]:
            text_str = text_str + line + "\n"
        text_str = text_str[:-1]

        diff = dmp.diff_main(text1=text_str, text2=result_str)
        levdiff = dmp.diff_levenshtein(diff)
        print("Levenshtein score: ", str(levdiff))
        print("Text length: ", str(len(text_str)))
        print("Levenshtein accuracy: " + str(100 - levdiff*100 / len(text_str)))
        dmp.diff_cleanupSemanticLossless(diff)
        print(diff)


def calc_error(results, texts):
    # Khởi tạo Diff Match Patch
    dmp = diffmp.diff_match_patch()
    difflist = []
    levenshtein_list = []
    file = open("ocr_tester/generated_test/result.csv", "w+")
    file.truncate()
    writer = csv.writer(file)

    # So sánh, tính độ chính xác
    for i in range(len(results)):
        result_str = results[i]
        text_str = ""
        
        for line in texts[i]:
            text_str = text_str + line + "\n"
        text_str = text_str[:-1]

        diff = dmp.diff_main(text1=text_str, text2=result_str)
        levdiff = dmp.diff_levenshtein(diff)
        lev_accuracy = 100 - levdiff*100 / len(text_str)
        writer.writerow([i, levdiff, len(text_str), lev_accuracy])
        levenshtein_list.append(lev_accuracy)

        dmp.diff_cleanupSemanticLossless(diff)
        difflist.append(diff)

    file.close()

    #Độ chính xác trung bình
    print("Average accuracy: ", sum(levenshtein_list) / len(levenshtein_list))
    plt.hist(levenshtein_list)
    plt.title("Độ chính xác của Tesseract")
    plt.show()

    #Phân tích các sai lệch
    mismatch_chars = 0
    extra_chars = 0
    missing_chars = 0
    error_dict = {}

    for diff in difflist:
        for i in range(len(diff)):
            diffcode, diffstr = diff[i]

            if diffcode == 0:
                continue

            if diffcode == -1 and i < len(diff) - 1 and diff[i + 1][0] == 1:
                mismatch_chars += 1
                updatedict(error_dict, diff[i + 1][1])
                continue
                
            if diffcode == 1:
                extra_chars += 1
                updatedict(error_dict, diffstr)
                continue

            if diffcode == -1:
                missing_chars += 1
                updatedict(error_dict, diffstr)
                continue

    print("mismatchs: ", str(mismatch_chars))
    print("extras: ", str(extra_chars))
    print("missings: ", str(missing_chars))

    keys = list(error_dict.keys())
    values = list(error_dict.values())
    sorted_value_index = np.argsort(values)
    sorted_value_index = np.flipud(sorted_value_index)[:30]
    error_dict = {keys[i]: values[i] for i in sorted_value_index}

    print(error_dict)
    plt.bar(error_dict.keys(), error_dict.values())
    plt.title("Tần suất lỗi")
    plt.xticks(rotation=90)
    plt.show()


def updatedict(error_dict : dict, error_key : str):
    if "\n" in error_key:
        error_key = "newline"

    if error_key == " ":
        error_key = "space"

    if error_key in error_dict:
        error_dict[error_key] += 1
    else:
        error_dict[error_key] = 1
    