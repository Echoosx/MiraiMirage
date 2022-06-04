package org.echoosx.mirai.plugin.util

import org.echoosx.mirai.plugin.MirageBuilder
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

@Suppress("unused","duplicates")
object MirageUtils {
    private val logger get() = MirageBuilder.logger

    // 亮、暗色阶映射表
    private var mLightColorTable: IntArray? = null
    private var mDarkColorTable: IntArray? = null

    // 生成幻影坦克
    fun buildMirageTank(pathOut:String,pathIn:String,savePath:String){
        var picA = setImageOne(pathOut)!!
        var picB = setImageTwo(pathIn)!!
        val targetList: List<BufferedImage?> = picResize(picA, picB)
        if (targetList.isNotEmpty()) {
            if (targetList[0] != null) {
                picA = targetList[0]!!
            }
            if (targetList[1] != null) {
                picB = targetList[1]!!
            }
        }
        setGray(picA)
        changeColorLevel(picA, true)
        opposition(picA)
        setGray(picB)
        changeColorLevel(picB, false)
        picA = linearDodge(picA, picB)
        val temp: BufferedImage = redChannels(picA)
        picA = divide(picA, picB)
        val result: BufferedImage = masking(picA, temp)
        saveFile(savePath, result)
    }

    private fun getImage(path: String?): BufferedImage? {
        val file = File(path!!)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
        } catch (e1: FileNotFoundException) {
            e1.printStackTrace()
        }
        try {
            if (fis != null) {
                return ImageIO.read(fis)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setImageOne(pathOne: String?): BufferedImage? {
        return getImage(pathOne)
    }

    private fun setImageTwo(pathTwo: String?): BufferedImage? {
        return getImage(pathTwo)
    }

    private fun saveFile(savePath: String?, result: BufferedImage?) {
        val file = File(savePath!!)
        try {
            if (!file.exists()) {
                file.createNewFile()
                ImageIO.write(result, "png", file)
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    // 重设图片大小
    private fun picResize(targetTop: BufferedImage, targetBottom: BufferedImage): List<BufferedImage?>{
        val topWidth = targetTop.width
        val topHeight = targetTop.height
        val bottomWidth = targetBottom.width
        val bottomHeight = targetBottom.height
        val targetList: MutableList<BufferedImage?> = ArrayList()

        if((topWidth == bottomWidth) && (topHeight == bottomHeight)){
            targetList.add(targetTop)
            targetList.add(targetBottom)
        }else {
            val scaleRatio = min(
                ((topWidth * 1f)/(bottomWidth * 1f)),
                ((topHeight * 1f)/(bottomHeight * 1f))
            )
            val scaledWidth = (targetBottom.width * scaleRatio).toInt()
            val scaledHeight = (targetBottom.height * scaleRatio).toInt()
            val scaleBottom = targetBottom.getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_DEFAULT
            )
            val resultBottom = BufferedImage(topWidth, topHeight, BufferedImage.TYPE_INT_ARGB)
            val graphics = resultBottom.createGraphics()
            graphics.drawImage(
                scaleBottom,
                (topWidth-scaledWidth) / 2,
                (topHeight-scaledHeight) / 2,
                null
            )
            graphics.dispose()

            targetList.add(targetTop)
            targetList.add(resultBottom)
        }
        return targetList
    }

    //去色
    private fun setGray(target: BufferedImage) {
        val width = target.width
        val height = target.height
        val targetPixels = IntArray(width * height)
        getBitmapPixelColor(target, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                val gray = (r + g + b) / 3
                targetPixels[x + y * width] = getIntFromColor(a, gray, gray, gray)
            }
        })
        target.setRGB(0, 0, width, height, targetPixels, 0, width)
    }

    // 红色通道
    private fun redChannels(target: BufferedImage): BufferedImage {
        val width = target.width
        val height = target.height
        val targetPixels = IntArray(width * height)
        val result = BufferedImage(target.width, target.height,
            BufferedImage.TYPE_INT_ARGB)
        getBitmapPixelColor(target, object : PixelColorHandler{
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                targetPixels[x + y * width] = getIntFromColor(r, r, g, b)
            }
        })
        result.setRGB(0, 0, width, height, targetPixels, 0, width)

        return result
    }

    // 蒙版
    private fun masking(src: BufferedImage, target: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val srcPixels = IntArray(width * height)
        val result = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB)
        getBitmapPixelColor(src, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                val dstA: Int
                val dstPixelColor: Int = target.getRGB(x, y)
                dstA = dstPixelColor and -0x1000000 shr 24
                srcPixels[x + y * width] = getIntFromColor(dstA, r, g, b)
            }
        })
        result.setRGB(0, 0, width, height, srcPixels, 0, width)
        return result
    }

    // 反相
    private fun opposition(target: BufferedImage) {
        val width = target.width
        val height = target.height
        val targetPixels = IntArray(width * height)
        getBitmapPixelColor(target, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                val max = 255
                targetPixels[x + y * width] = getIntFromColor(a, max - r, max - g, max - b)
            }
        })
        target.setRGB(0, 0, width, height, targetPixels, 0, width)
    }

    //划分
    private fun divide(src: BufferedImage, target: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val srcPixels = IntArray(width * height)
        val result = BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB)
        getBitmapPixelColor(src, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                val dstR: Int
                val dstG: Int
                val dstB: Int
                val dstPixelColor: Int = target.getRGB(x, y)
                dstR = dstPixelColor and 0xFF0000 shr 16
                dstG = dstPixelColor and 0xFF00 shr 8
                dstB = dstPixelColor and 0xFF
                val resultA = 255
                val resultR: Int = (255 / (r.toFloat() / dstR.toFloat())).toInt()
                val resultG: Int = (255 / (g.toFloat() / dstG.toFloat())).toInt()
                val resultB: Int = (255 / (b.toFloat() / dstB.toFloat())).toInt()
                srcPixels[x + y * width] = getIntFromColor(resultA, resultR, resultG, resultB)
            }
        })
        result.setRGB(0, 0, width, height, srcPixels, 0, width)
        return result
    }

    //线性减淡
    private fun linearDodge(src: BufferedImage, target: BufferedImage): BufferedImage {
        val width = src.width
        val height = src.height
        val srcPixels = IntArray(width * height)
        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        getBitmapPixelColor(src, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                val dstR: Int
                val dstG: Int
                val dstB: Int
                var resultR: Int
                var resultG: Int
                var resultB: Int
                val dstPixelColor: Int = target.getRGB(x, y)
                dstR = dstPixelColor and 0xFF0000 shr 16
                dstG = dstPixelColor and 0xFF00 shr 8
                dstB = dstPixelColor and 0xFF
                val resultA = 255
                resultR = r + dstR
                resultG = g + dstG
                resultB = b + dstB
                // 防止色值溢出
                if (resultR > 255) resultR = 255
                if (resultG > 255) resultG = 255
                if (resultB > 255) resultB = 255
                srcPixels[x + y * width] = getIntFromColor(resultA, resultR, resultG, resultB)
            }
        })
        result.setRGB(0, 0, width, height, srcPixels, 0, width)
        return result
    }

    // 调整色阶, true->up, false->down
    private fun changeColorLevel(target: BufferedImage, isToLight: Boolean) {
        val width = target.width
        val height = target.height
        val targetPixels = IntArray(width * height)
        val table = if (isToLight) lightColorTable else darkColorTable
        getBitmapPixelColor(target, object : PixelColorHandler {
            override fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int) {
                targetPixels[x + y * width] = getIntFromColor(a, table!![r], table[g], table[b])
            }
        })
        target.setRGB(0, 0, width, height, targetPixels, 0, width)
    }

    private val lightColorTable: IntArray?
        get() {
            if (mLightColorTable == null) initLightColorTable()
            return mLightColorTable
        }
    private val darkColorTable: IntArray?
        get() {
            if (mDarkColorTable == null) initDarkColorTable()
            return mDarkColorTable
        }

    private fun getBitmapPixelColor(target: BufferedImage, handler: PixelColorHandler) {
        var a: Int
        var r: Int
        var g: Int
        var b: Int
        var pixelColor: Int
        for (y in 0 until target.height) {
            for (x in 0 until target.width) {
                pixelColor = target.getRGB(x, y)
                a = pixelColor and -0x1000000 shr 24
                r = pixelColor and 0xFF0000 shr 16
                g = pixelColor and 0xFF00 shr 8
                b = pixelColor and 0xFF
                handler.onHandle(x, y, a, r, g, b)
            }
        }
    }

    // Color.argb
    private fun getIntFromColor(alpha: Int, red: Int, green: Int, blue: Int): Int {
        var alpha = alpha
        var red = red
        var green = green
        var blue = blue
        alpha = alpha shl 24 and -0x1000000
        red = red shl 16 and 0x00FF0000 // Shift red 16-bits and mask out other stuff
        green = green shl 8 and 0x0000FF00 // Shift Green 8-bits and mask out other stuff
        blue = blue and 0x000000FF // Mask out anything not blue.
        return 0x00000000 or alpha or red or green or blue
    }

    private fun initLightColorTable() {
        // 输出色阶 120 ～ 255 的映射表
        // 由 getColorLevelTable(120, 255); 得来
        mLightColorTable = intArrayOf(120, 120, 121, 121, 122, 122, 123, 123, 124, 124, 125, 125, 126, 126, 127, 127,
            128, 128, 129, 129, 130, 130, 131, 132, 132, 133, 133, 134, 134, 135, 135, 136, 136, 137, 137, 138, 138,
            139, 139, 140, 140, 141, 142, 142, 143, 143, 144, 144, 145, 145, 146, 146, 147, 147, 148, 148, 149, 149,
            150, 150, 151, 152, 152, 153, 153, 154, 154, 155, 155, 156, 156, 157, 157, 158, 158, 159, 159, 160, 161,
            161, 162, 162, 163, 163, 164, 164, 165, 165, 166, 166, 167, 167, 168, 168, 169, 170, 170, 171, 171, 172,
            172, 173, 173, 174, 174, 175, 175, 176, 176, 177, 177, 178, 179, 179, 180, 180, 181, 181, 182, 182, 183,
            183, 184, 184, 185, 185, 186, 186, 187, 188, 188, 189, 189, 190, 190, 191, 191, 192, 192, 193, 193, 194,
            194, 195, 195, 196, 197, 197, 198, 198, 199, 199, 200, 200, 201, 201, 202, 202, 203, 203, 204, 205, 205,
            206, 206, 207, 207, 208, 208, 209, 209, 210, 210, 211, 211, 212, 212, 213, 214, 214, 215, 215, 216, 216,
            217, 217, 218, 218, 219, 219, 220, 220, 221, 222, 222, 223, 223, 224, 224, 225, 225, 226, 226, 227, 227,
            228, 228, 229, 229, 230, 231, 231, 232, 232, 233, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 239,
            239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 247, 247, 248, 248, 249, 249, 250,
            250, 251, 251, 252, 252, 253, 253, 254, 255)
    }

    private fun initDarkColorTable() {
        // 输出色阶 0 ～ 135 的映射表
        // 由 getColorLevelTable(0, 135); 得来
        mDarkColorTable = intArrayOf(0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 12, 12,
            13, 13, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21, 22, 22, 23, 23, 24, 24, 25, 25, 26,
            26, 27, 27, 28, 28, 29, 29, 30, 30, 31, 32, 32, 33, 33, 34, 34, 35, 35, 36, 36, 37, 37, 38, 38, 39, 39,
            40, 41, 41, 42, 42, 43, 43, 44, 44, 45, 45, 46, 46, 47, 47, 48, 48, 49, 50, 50, 51, 51, 52, 52, 53, 53,
            54, 54, 55, 55, 56, 56, 57, 57, 58, 59, 59, 60, 60, 61, 61, 62, 62, 63, 63, 64, 64, 65, 65, 66, 66, 67,
            68, 68, 69, 69, 70, 70, 71, 71, 72, 72, 73, 73, 74, 74, 75, 75, 76, 77, 77, 78, 78, 79, 79, 80, 80, 81,
            81, 82, 82, 83, 83, 84, 85, 85, 86, 86, 87, 87, 88, 88, 89, 89, 90, 90, 91, 91, 92, 92, 93, 94, 94, 95,
            95, 96, 96, 97, 97, 98, 98, 99, 99, 100, 100, 101, 102, 102, 103, 103, 104, 104, 105, 105, 106, 106,
            107, 107, 108, 108, 109, 109, 110, 111, 111, 112, 112, 113, 113, 114, 114, 115, 115, 116, 116, 117, 117,
            118, 119, 119, 120, 120, 121, 121, 122, 122, 123, 123, 124, 124, 125, 125, 126, 127, 127, 128, 128, 129,
            129, 130, 130, 131, 131, 132, 132, 133, 133, 134, 135)
    }

    private fun getColorLevelTable(outputMin: Int, outputMax: Int): IntArray {
        var outputMin = outputMin
        var outputMax = outputMax
        val data = IntArray(256)
        val inputMin = 0
        val inputMiddle = 128
        val inputMax = 255
        if (outputMin < 0) outputMin = 0
        if (outputMin > 255) outputMin = 255
        if (outputMax < 0) outputMax = 0
        if (outputMax > 255) outputMax = 255
        for (index in 0..255) {
            var temp = (index - inputMin).toDouble()
            temp = if (temp < 0) {
                outputMin.toDouble()
            } else if (temp + inputMin > inputMax) {
                outputMax.toDouble()
            } else {
                val gamma = ln(0.5) / ln((inputMiddle - inputMin).toDouble() / (inputMax - inputMin))
                outputMin + (outputMax - outputMin) * (temp / (inputMax - inputMin)).pow(gamma)
            }
            if (temp > 255) temp = 255.0 else if (temp < 0) temp = 0.0
            data[index] = temp.toInt()
        }
        return data
    }
}