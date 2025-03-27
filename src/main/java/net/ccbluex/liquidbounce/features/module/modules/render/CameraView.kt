/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.CameraPositionEvent
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.render.Camera

object CameraView : Module("CameraView", Category.RENDER, gameDetecting = false) {

    private val customY by float("CustomY", 0f, -10f..10f)
    private val saveLastGroundY by boolean("SaveLastGroundY", true)
    private val onScaffold by boolean("OnScaffold", true)
    private val onF5 by boolean("OnF5", true)
    private val smoothness by float("Smoothness", 0.05f, 0.01f..0.5f)

    private var launchY: Double? = null
    private var prevRenderY = 0.0
    private var currentInterpolation = 0.5

    override fun onEnable() {
        mc.thePlayer?.run {
            launchY = posY
            prevRenderY = posY
        }
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST) return@handler

        mc.thePlayer?.run {
            if (!saveLastGroundY || (onGround || ticksExisted == 1)) {
                launchY = posY
            }
        }
    }

    val onCameraUpdate = handler<CameraPositionEvent> { event ->
        mc.thePlayer?.run {
            val currentLaunchY = launchY ?: return@handler
            if (onScaffold && !Scaffold.handleEvents()) return@handler
            if (onF5 && mc.gameSettings.thirdPersonView == 0) return@handler

            // 获取目标Y位置
            val targetY = currentLaunchY + customY

            // 检查相机是否激活并获取插值
            val camera = Camera
            val useSmoothing = camera.isActive && camera.state
            currentInterpolation = if (useSmoothing) camera.interpolation.toDouble() else smoothness.toDouble()

            // 应用平滑处理
            prevRenderY += (targetY - prevRenderY) * currentInterpolation

            event.withY(prevRenderY)
        }
    }
}