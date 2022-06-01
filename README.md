# Mirai-Mirage

> 基于 [Mirai Console](https://github.com/mamoe/mirai-console) 的幻影坦克图生成插件

[![Release](https://img.shields.io/github/v/release/Echoosx/MiraiMirage)](https://github.com/Echoosx/MiraiMirage/releases)
[![Build](https://github.com/Echoosx/MiraiMirage/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)](https://github.com/Echoosx/MiraiMirage/actions/workflows/gradle.yml)


## 功能
交互式生成`幻影坦克图`

不了解`幻影坦克图`的可以先浏览此[介绍](https://samarium150.github.io/mirage-tank-images/) <br/>
在此鸣谢 [Samarium](https://github.com/Samarium150) 大佬提供的 [生成原理](https://github.com/Samarium150/mirage-tank-images)


## 指令
注意: 使用前请确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)  
带括号的`/`前缀是缺省的  
`<...>`中的是指令名，由`｜`隔开表示其中任一名称都可执行  
`[...]`表示参数，当`[...]`后面带`?`时表示参数可选  
`{...}`表示连续的多个参数


| 指令          | 描述          | 权限名称                                                |
|:------------|:------------|:----------------------------------------------------|
| `/<mirage>` | 生成幻影坦克图（交互命令）  | `org.echoosx.mirai.plugin.mirage-builder:command.mirage` |

## 安装
- 从 [Releases](https://github.com/Echoosx/MiraiMirage/releases) 下载`jar`包，放入根目录下的`plugins`文件夹
- 从 [Releases](https://github.com/Echoosx/MiraiMirage/releases) 下载`Mirage.zip`，`解压后`放在工作目录下
- 本插件使用了`Python`脚本，因此要准备好`Python`和`pip`环境
- 在`工作目录/Mirage`目录下，执行`pip install -r requirment.txt`安装好所需的第三方库

## 配置
### setting.yml
```yaml
# 定期清理存储的时间(Cron表达式，默认每周一0点清理)
cleanCron: '0 0 0 ? * MON'
```

