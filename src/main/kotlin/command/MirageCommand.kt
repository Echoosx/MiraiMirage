package org.echoosx.mirai.plugin.command

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.echoosx.mirai.plugin.MirageBuilder
import org.echoosx.mirai.plugin.MirageConfig.blackColor
import org.echoosx.mirai.plugin.MirageConfig.blackLight
import org.echoosx.mirai.plugin.MirageConfig.whiteColor
import org.echoosx.mirai.plugin.MirageConfig.whiteLight
import org.echoosx.mirai.plugin.util.*
import org.echoosx.mirai.plugin.util.getOrWaitImage
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MirageCommand:SimpleCommand(
    MirageBuilder,
    "mirage","幻影坦克", description = "制造幻影坦克"
) {
    private val ORIGIN_PATH = "Mirage/Origin"
    private val OUTPUT_PATH = "Mirage/Output"
    private val logger get() = MirageBuilder.logger

    private val SupportImageTypes = listOf<String>("JPG","PNG")

    @Suppress("unused")
    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.handle(light:Float = blackLight, chess:Boolean = false){
        try {
            val timestamp = DateTimeFormatter.ofPattern("YYMMddHHmmss").format(LocalDateTime.now())
            val outsideImage = this.fromEvent.getOrWaitImage("开始制作幻影坦克图！\n首先请发送『表图』:") ?: return
            if(!SupportImageTypes.contains(outsideImage.imageType.name)) {
                sendMessage("此图片格式不支持！")
                return
            }
            touchDir("${ORIGIN_PATH}/${user!!.id}")
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(outsideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${ORIGIN_PATH}/${user!!.id}/${timestamp}_white.jpg"))
                }
            }

            val insideImage = this.fromEvent.getOrWaitImage("接下来请发送『里图』:") ?: return
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(insideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${ORIGIN_PATH}/${user!!.id}/${timestamp}_black.jpg"))
                }
            }
            if(!SupportImageTypes.contains(insideImage.imageType.name)) {
                sendMessage("此图片格式不支持！")
                return
            }
            touchDir("${OUTPUT_PATH}/${user!!.id}")

            // 获取里外图片宽度和高度比的较小值，当里外图片大小不一致时，以此比率放缩能保证外图不会产生灰边
            val ratio = Math.min((outsideImage.width * 1f / insideImage.width * 1f),(outsideImage.height *1f / insideImage.height * 1f))

            val command = "python3 Mirage/MirageTankGo.py -o ${OUTPUT_PATH}/${user!!.id}/${timestamp}.png ${ORIGIN_PATH}/${user!!.id}/${timestamp}_black.jpg ${ORIGIN_PATH}/${user!!.id}/${timestamp}_white.jpg ${if(chess) "-e " else ""}--scale=1-${ratio} --light=${whiteLight}-${light} --color=${whiteColor}-${blackColor}"
            val process = command.execute()

            val exitCode = withContext(Dispatchers.IO) {
                process.waitFor()
            }
            val error = process.error()
            if(exitCode == 0) {
                val resource = File("${OUTPUT_PATH}/${user!!.id}/${timestamp}.png").toExternalResource()
                subject!!.sendImage(resource)
                withContext(Dispatchers.IO) {
                    resource.close()
                }
            }else{
                throw Exception(error)
            }
        }catch (e:Throwable){
            sendMessage("生成失败 Error: $e")
        }
    }
}