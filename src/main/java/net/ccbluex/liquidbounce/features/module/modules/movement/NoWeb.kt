/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.grim.NoWebOldGrim
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.intave.*
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other.*

object NoWeb : Module("NoWeb", Category.MOVEMENT) {

    val mode = choices("Mode", arrayOf(
        NoWebNone,
        NoWebAAC,
        NoWebLAAC,
        NoWebIntaveOld,
        NoWebIntaveNew,
        NoWebOldGrim,
        NoWebRewi
    ), NoWebNone)

    override val tag
        get() = mode.select
}
