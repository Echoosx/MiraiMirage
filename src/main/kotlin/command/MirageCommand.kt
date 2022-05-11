package org.echoosx.mirai.plugin.command

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.echoosx.mirai.plugin.MirageBuilder
import org.echoosx.mirai.plugin.util.copyInputStreamToFile
import org.echoosx.mirai.plugin.util.error
import org.echoosx.mirai.plugin.util.execute
import org.echoosx.mirai.plugin.util.getOrWaitImage
import java.io.File
import java.io.InputStream

object MirageCommand:SimpleCommand(
    MirageBuilder,
    "mirage","幻影坦克", description = "制造幻影坦克"
) {
    val ORIGIN_PATH = "Mirage/Origin"
    val OUTPUT_PATH = "Mirage/Output"

    @Suppress("unused")
    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.handle(scale_custom:String = "", light:String = "1-0.25", color:String = "0.5-0.7"){
        try {
            val outsideImage = this.fromEvent.getOrWaitImage("开始制作幻影坦克图！\n首先请发送『表图』:") ?: return
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(outsideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${ORIGIN_PATH}/outside.jpg"))
                }
            }

            val insideImage = this.fromEvent.getOrWaitImage("接下来请发送『里图』:") ?: return
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(insideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${ORIGIN_PATH}/inside.jpg"))
                }
            }

            val ratio = Math.min((outsideImage.width * 1f / insideImage.width * 1f),(outsideImage.height *1f / insideImage.height * 1f))
//        val scale = scale_custom.ifBlank {
//            "1-${ratio}"
//        }

            val scale = if(scale_custom.isBlank() || scale_custom=="default"){
                "1-${ratio}"
            }else{
                scale_custom
            }

            // val timestamp = DateTimeFormatter.ofPattern("YYMMddHHmmss").format(LocalDateTime.now())
            val timestamp = "output"

            val process = ("python3 Mirage/MirageTankGo.py -o ${OUTPUT_PATH}/${timestamp}.png ${ORIGIN_PATH}/inside.jpg ${ORIGIN_PATH}/outside.jpg --scale=${scale} --light=${light} --color=${color}").execute()
            MirageBuilder.logger.info("python3 Mirage/MirageTankGo.py -o ${OUTPUT_PATH}/${timestamp}.png ${ORIGIN_PATH}/inside.jpg ${ORIGIN_PATH}/outside.jpg --scale=${scale} --light=${light} --color=${color}")
            val exitCode = withContext(Dispatchers.IO) {
                process.waitFor()
            }
            val error = process.error()
            if(exitCode == 0) {
                val resource = File("${OUTPUT_PATH}/${timestamp}.png").toExternalResource()
                val image = resource.use { subject!!.uploadImage(it) }
                sendMessage(image)
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