# Mirai-Mirage

> 基于 [Mirai Console](https://github.com/mamoe/mirai-console) 的 “幻影坦克图” 生成插件

[![Release](https://img.shields.io/github/v/release/Echoosx/MiraiMirage)](https://github.com/Echoosx/MiraiMirage/releases)
[![Build](https://github.com/Echoosx/MiraiMirage/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)](https://github.com/Echoosx/MiraiMirage/actions/workflows/gradle.yml)
[![Downloads](https://img.shields.io/github/downloads/Echoosx/MiraiMirage/total)](https://github.com/Echoosx/MiraiMirage/releases)

## 功能
生成`幻影坦克图`<br/>
不了解`幻影坦克图`的可以先浏览 [此介绍](https://samarium150.github.io/mirage-tank-images/)
![指令交互](demo/demo_1.png)
![幻影坦克](demo/demo_2.gif)


## 指令
注意: 使用前请确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command) <br/>
`<...>`中的是指令名，由`｜`隔开表示其中任一名称都可执行  
`[...]`表示参数，当`[...]`后面带`?`时表示参数可选  
`{...}`表示连续的多个参数


| 指令                | 描述      | 指令权限名                                                    |
|:------------------|:--------|:---------------------------------------------------------|
| `/<mirage｜幻影坦克 >` | 生成幻影坦克图 | `org.echoosx.mirai.plugin.mirage-builder:command.mirage` |

## 配置
### setting.yml
```yaml
# 定期清理存储的时间(Cron表达式，默认每周一0点清理)
cleanCron: '0 0 0 ? * MON'
```
cron表达式的格式可以参考：https://www.bejson.com/othertools/cron/

## 安装
- 从 [Releases](https://github.com/Echoosx/MiraiMirage/releases) 下载`jar`包，放入根目录下的`plugins`文件夹
- 如果没有`plugins`文件夹，先运行 [Mirai Console](https://github.com/mamoe/mirai-console) ，会自动生成

## 参考项目
- [HYTank](https://github.com/wsgaowxh/HYTank)
- [mirage-tank-images](https://github.com/Samarium150/mirage-tank-images)