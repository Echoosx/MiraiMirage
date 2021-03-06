package org.echoosx.mirai.plugin.util

import org.echoosx.mirai.plugin.MirageBuilder
import org.echoosx.mirai.plugin.MirageBuilder.dataFolder
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


internal class StorageClean:Job{
    private val logger get() = MirageBuilder.logger

    @Throws(JobExecutionException::class)
    override fun execute(jobExecutionContext: JobExecutionContext?) {
        // 当前时间
        val date = Date()
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateStr = dateFormat.format(date)

        // 工作内容
        File("${dataFolder.absolutePath}/Mirage/Input").deleteRecursively()
        File("${dataFolder.absolutePath}/Mirage/Output").deleteRecursively()

        logger.info("MirageBuilder清理缓存 执行时间：$dateStr")
    }
}