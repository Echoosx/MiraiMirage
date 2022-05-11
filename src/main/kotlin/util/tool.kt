package org.echoosx.mirai.plugin.util


import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import java.io.*


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