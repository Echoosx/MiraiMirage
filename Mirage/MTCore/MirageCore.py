#!/usr/bin/env python
# -*- coding: utf-8 -*-
from ast import Return, arg
import time
from typing import Callable, Tuple

import cv2
import numpy as np
from numba import njit
from PIL import Image


def profile(func: Callable) -> Callable:
    """
    A decorator function for elapsed-time profiling

    usage: @profile

    :param func: function to profile
    """

    def with_profiling(*args, **kwargs):
        start_time = time.time()
        ret = func(*args, **kwargs)
        elapsed_time = time.time() - start_time
        print('{} finished, takes {:.4f}s'.format(func.__name__, elapsed_time))
        return ret

    return with_profiling


@profile
def desaturate(image: np.ndarray) -> np.ndarray:
    """
    Photoshop-like desaturation, formula:

    lambda r, g, b: (max(r, g, b) + min(r, g, b)) / 2

    source: https://stackoverflow.com/a/28873770

    :param image: input RGB image read by cv2
    :return: desaturated grayscale image
    """
    cp: np.ndarray = image.copy().astype(np.float_)
    desaturated: np.ndarray = (np.amax(cp, 2) + np.amin(cp, 2)) / 2
    return desaturated.astype(np.uint8)


@profile
def adjust_lightness(image: np.ndarray, ratio: float) -> np.ndarray:
    """
    Adjust the lightness of the image

    :param image: input grayscale image
    :param ratio: float number from -100% to +100%
    :return: adjusted image
    """
    cp: np.ndarray = image.copy().astype(np.float_)
    if ratio > 0:
        return (cp * (1 - ratio) + 255 * ratio).astype(np.uint8)
    return np.ceil(cp * (1 + ratio)).astype(np.uint8)


@profile
def invert(image: np.ndarray) -> np.ndarray:
    """
    Invert the color of the image

    :param image: input grayscale image
    :return: inverted image
    """
    return 255 - image


@profile
def linear_dodge_blend(img_x: np.ndarray, img_y: np.ndarray) -> np.ndarray:
    """
    Blend image x and y in 'linear dodge' mode

    :param img_x: input grayscale image on top
    :param img_y: input grayscale image at bottom
    :return:
    """
    return img_x + img_y


@profile
@njit
def divide_blend(img_x: np.ndarray, img_y: np.ndarray) -> np.ndarray:
    """
    Blend image x and y in 'divide' mode

    :param img_x: input grayscale image on top
    :param img_y: input grayscale image at bottom
    :return:
    """
    result = np.zeros_like(img_x, np.float_)
    height, width = img_x.shape
    for i in range(height):
        for j in range(width):
            if img_x[i, j] == 0:
                color = img_y[i, j] and 255 or 0
            elif img_x[i, j] == 255:
                color = img_y[i, j]
            elif img_x[i, j] == img_y[i, j]:
                color = 255
            else:
                color = (img_y[i, j] / img_x[i, j]) * 255
            result[i, j] = color
    return result.astype(np.uint8)


@profile
def add_mask(img_x: np.ndarray, img_y: np.ndarray) -> np.ndarray:
    """
    Add image y to x as the alpha channel

    :param img_x: input grayscale image
    :param img_y: input grayscale image
    :return: the result image
    """
    result: np.ndarray = cv2.cvtColor(img_x, cv2.COLOR_GRAY2BGRA)
    result[:, :, 3] = img_y
    return result

@profile
def resize_image(im1: Image.Image, im2: Image.Image, mode: str) -> Tuple[Image.Image, Image.Image]:
    """
    统一图像大小
    """
    _wimg = im1.convert(mode)
    _bimg = im2.convert(mode)

    wwidth, wheight = _wimg.size
    bwidth, bheight = _bimg.size

    width = max(wwidth, bwidth)
    height = max(wheight, bheight)

    wimg = Image.new(mode, (width, height), 255)
    bimg = Image.new(mode, (width, height), 0)

    wimg.paste(_wimg, ((width - wwidth) // 2, (height - wheight) // 2))
    bimg.paste(_bimg, ((width - bwidth) // 2, (height - bheight) // 2))

    return wimg, bimg

# def build(source_x: str, source_y: str, target_name: str, shrink: float = 1):
#     """
#     Build a 'mirage tank' image from [source_x] and [source_y]

#     (Assuming [source_x] and [source_y] have the same size)

#     :param source_x: name of the image shown on white background
#     :param source_y: name of the image shown on black background
#     :param target_name: name of the image to be saved
#     :param shrink: size change comparing to the source image
#     """
#     print("start process")
#     img_a = cv2.cvtColor(
#         cv2.imread(source_x, cv2.IMREAD_UNCHANGED),
#         cv2.COLOR_BGR2RGB
#     )
#     img_b = cv2.cvtColor(
#         cv2.imread(source_y, cv2.IMREAD_UNCHANGED),
#         cv2.COLOR_BGR2RGB
#     )
#     height, width, _ = img_a.shape
#     height = int(height * shrink)
#     width = int(width * shrink)
#     img_a = invert(
#         adjust_lightness(desaturate(cv2.resize(img_a, (width, height))), 0.5)
#     )
#     img_b = adjust_lightness(
#         desaturate(cv2.resize(img_b, (width, height))), -0.5
#     )
#     linear_dodged = linear_dodge_blend(img_a, img_b)
#     divided = divide_blend(linear_dodged, img_b)
#     cv2.imwrite(target_name, add_mask(divided, linear_dodged))
#     print("finished")


def build(whiteImg: Image, blackImg: Image, target_name: str, shrink: float = 1):
    """
    Build a 'mirage tank' image from [source_x] and [source_y]

    (Assuming [source_x] and [source_y] have the same size)

    :param source_x: name of the image shown on white background
    :param source_y: name of the image shown on black background
    :param target_name: name of the image to be saved
    :param shrink: size change comparing to the source image
    """
    print("start process")
    wimg, bimg = resize_image(whiteImg, blackImg, 'L')
    img_a = cv2.cvtColor(
        np.array(wimg),
        cv2.COLOR_BGR2RGB
    )
    img_b = cv2.cvtColor(
        np.array(bimg),
        cv2.COLOR_BGR2RGB
    )
    height, width, _ = img_a.shape
    height = int(height * shrink)
    width = int(width * shrink)
    img_a = invert(
        adjust_lightness(desaturate(cv2.resize(img_a, (width, height))), 0.5)
    )
    img_b = adjust_lightness(
        desaturate(cv2.resize(img_b, (width, height))), -0.5
    )
    linear_dodged = linear_dodge_blend(img_a, img_b)
    divided = divide_blend(linear_dodged, img_b)
    cv2.imwrite(target_name, add_mask(divided, linear_dodged))
    print("finished")