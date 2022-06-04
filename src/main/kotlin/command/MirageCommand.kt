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
import org.echoosx.mirai.plugin.MirageBuilder.dataFolder
import org.echoosx.mirai.plugin.util.*
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MirageCommand:SimpleCommand(
    MirageBuilder,
    "mirage","幻影坦克", description = "制作幻影坦克图"
) {
    private val INPUT_PATH = "${dataFolder.absolutePath}/Mirage/Input"
    private val OUTPUT_PATH = "${dataFolder.absolutePath}/Mirage/Output"

    @Suppress("unused")
    @Handler
    suspend fun CommandSenderOnMessage<MessageEvent>.handle(){
        try {
            val timestamp = DateTimeFormatter.ofPattern("YYMMddHHmmss").format(LocalDateTime.now())

            val outsideImage = this.fromEvent.getOrWaitImage("开始制作幻影坦克图！\n首先请发送『表图』:") ?: return
            touchDir("${INPUT_PATH}/${user!!.id}")
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(outsideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${INPUT_PATH}/${user!!.id}/${timestamp}_out.jpg"))
                }
            }
            if(outsideImage.imageType.name != "JPG"){
                convertToJPG("${INPUT_PATH}/${user!!.id}/${timestamp}_out.jpg")
            }

            val insideImage = this.fromEvent.getOrWaitImage("接下来请发送『里图』:") ?: return
            HttpClient(OkHttp).use { client->
                client.get<InputStream>(insideImage.queryUrl()).use{
                    copyInputStreamToFile(it,File("${INPUT_PATH}/${user!!.id}/${timestamp}_in.jpg"))
                }
            }
            if(insideImage.imageType.name != "JPG"){
                convertToJPG("${INPUT_PATH}/${user!!.id}/${timestamp}_in.jpg")
            }

            touchDir("${OUTPUT_PATH}/${user!!.id}")
            MirageUtils.buildMirageTank(
                "${INPUT_PATH}/${user!!.id}/${timestamp}_out.jpg",
                "${INPUT_PATH}/${user!!.id}/${timestamp}_in.jpg",
                "${OUTPUT_PATH}/${user!!.id}/${timestamp}.png"
            )
            val resource = File("${OUTPUT_PATH}/${user!!.id}/${timestamp}.png").toExternalResource()
            subject?.sendImage(resource)
            withContext(Dispatchers.IO) {
                resource.close()
            }

        }catch (e:Throwable){
            sendMessage("生成失败！")
            logger.error(e)
        }
    }
}