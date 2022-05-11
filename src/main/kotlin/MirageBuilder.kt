package org.echoosx.mirai.plugin

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import org.echoosx.mirai.plugin.command.MirageCommand

object MirageBuilder : KotlinPlugin(
    JvmPluginDescription(
        id = "org.echoosx.mirai.plugin.mirage-builder",
        name = "mirage-builder",
        version = "1.0.0"
    ) {
        author("Echoosx")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        MirageCommand.register()
    }
}
