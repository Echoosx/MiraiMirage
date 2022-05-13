package org.echoosx.mirai.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MirageConfig:AutoSavePluginConfig("setting") {
    @ValueDescription("存储清理时间(Cron表达式)")
    val cleanCron:String by value("0 0 0 ? * MON")
}