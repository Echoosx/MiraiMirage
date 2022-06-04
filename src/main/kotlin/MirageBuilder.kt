package org.echoosx.mirai.plugin

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import org.echoosx.mirai.plugin.MirageConfig.cleanCron
import org.echoosx.mirai.plugin.command.MirageCommand
import org.echoosx.mirai.plugin.util.StorageClean
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory

object MirageBuilder : KotlinPlugin(
    JvmPluginDescription(
        id = "org.echoosx.mirai.plugin.mirage-builder",
        name = "mirage-builder",
        version = "2.0.0"
    ) {
        author("Echoosx")
    }
) {
    override fun onEnable() {
        logger.info { "MirageBuilder loaded" }
        MirageConfig.reload()
        MirageCommand.register()

        val scheduler = StdSchedulerFactory.getDefaultScheduler()

        val jobDetail = JobBuilder.newJob(StorageClean::class.java)
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cleanCron)
            )
            .startNow()
            .build()

        scheduler.scheduleJob(jobDetail,trigger)
        scheduler.start()
    }
}
