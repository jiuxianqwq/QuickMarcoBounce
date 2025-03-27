package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object Camera : Module("MotionCamera", Category.RENDER, gameDetecting = false) {
//    val motionCamera by boolean("MotionCamera", true)
    val interpolation by float("MotionInterpolation", 0.05f, 0.01f..0.5f)
}
