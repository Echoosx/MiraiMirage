package org.echoosx.mirai.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object MirageConfig:AutoSavePluginConfig("setting") {
    @ValueDescription("存储清理时间(Cron表达式)")
    val cleanCron:String by value("0 0 0 ? * MON")

    @ValueDescription("表图默认亮度")
    val whiteLight:Float by value(1f)

    @ValueDescription("里图默认亮度")
    val blackLight:Float by value(0.2f)

    @ValueDescription("表图默认色彩度")
    val whiteColor:Float by value(0.5f)

    @ValueDescription("里图默认色彩度")
    val blackColor:Float by value(0.7f)
}