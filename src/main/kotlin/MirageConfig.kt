package org.echoosx.mirai.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MirageConfig:AutoSavePluginConfig("setting") {
    @ValueDescription("定期清理缓存时间(Cron表达式格式，默认每周一0点)")
    val cleanCron:String by value("0 0 0 ? * MON")
}