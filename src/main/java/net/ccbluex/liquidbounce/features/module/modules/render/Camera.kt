package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.config.*

object Camera : Module("MotionCamera", Category.RENDER, gameDetecting = false) {
    val motionCamera = BoolValue("MotionCamera", true)
    val interpolation = FloatValue("MotionInterpolation", 0.05f, 0.01f..0.5f)
}
