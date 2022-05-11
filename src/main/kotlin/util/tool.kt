package org.echoosx.mirai.plugin.util


import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import org.echoosx.mirai.plugin.MirageBuilder
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO


private val logger get() = MirageBuilder.logger

/**
 * 给String扩展 execute() 函数
 */
fun String.execute(): Process {
    val runtime = Runtime.getRuntime()
    return runtime.exec(this)
}

/**
 * 扩展Process扩展 text() 函数
 */
fun Process.text(): String {
    // 输出 Shell 执行结果
    val inputStream = this.inputStream
    val insReader = InputStreamReader(inputStream)
    val bufReader = BufferedReader(insReader)

    var output = ""
    var line: String? =""
    while (null!=line) {
        // 逐行读取shell输出，并保存到变量output
        line = bufReader.readLine()
        if(line!="" && line!=null && line!="\n")
            output += line +"\n"
    }
    return output.trimEnd()
}

fun Process.error():String{
    val errorStream = this.errorStream
    val errReader = InputStreamReader(errorStream)
    val bufReader = BufferedReader(errReader)

    var output = ""
    var line:String?= ""
    while (null!=line) {
        // 逐行读取shell输出，并保存到变量output
        line = bufReader.readLine()
        if(line!="" && line!=null && line!="\n")
            output += line +"\n"
    }
    return output.trimEnd()
}

internal suspend fun MessageEvent.getOrWaitImage(msg: String): Image? {
    return (message.takeIf { m -> m.contains(Image) } ?: runCatching {
        subject.sendMessage(msg)
        nextMessage(30_000) { event -> event.message.contains(Image) }
    }.getOrElse { e ->
        when (e) {
            is TimeoutCancellationException -> {
                subject.sendMessage(PlainText("超时未发送").plus(message.quote()))
                return null
            }
            else -> throw e
        }
    }).firstIsInstanceOrNull<Image>()
}

// InputStream -> File
@Throws(IOException::class)
fun copyInputStreamToFile(inputStream: InputStream, file: File) {
    FileOutputStream(file).use { outputStream ->
        var read: Int
        val bytes = ByteArray(1024)
        while (inputStream.read(bytes).also { read = it } != -1) {
            outputStream.write(bytes, 0, read)
        }
    }
}

// 如不存在则创建目录
fun touchDir(destDirName: String): Boolean {
    var destDirName = destDirName
    val dir = File(destDirName)
    if (dir.exists()) {
        return false
    }
    if (!destDirName.endsWith(File.separator)) {
        destDirName += File.separator
    }
    //创建目录
    return if (dir.mkdirs()) {
        true
    } else {
        logger.error("创建目录" + destDirName + "失败！")
        false
    }
}


internal fun convertToJPG(imagePath:String){
    val bufferedImage: BufferedImage
    try {
        //read image file
        bufferedImage = ImageIO.read(File(imagePath))

        // create a blank, RGB, same width and height, and a white background
        val newBufferedImage = BufferedImage(bufferedImage.width,
            bufferedImage.height, BufferedImage.TYPE_INT_RGB)

        //TYPE_INT_RGB:创建一个RBG图像，24位深度，成功将32位图转化成24位
        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null)

        // write to jpeg file
        ImageIO.write(newBufferedImage, "jpg", File(imagePath))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}