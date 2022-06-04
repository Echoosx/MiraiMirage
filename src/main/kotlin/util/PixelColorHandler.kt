package org.echoosx.mirai.plugin.util

interface PixelColorHandler {
    fun onHandle(x: Int, y: Int, a: Int, r: Int, g: Int, b: Int)
}