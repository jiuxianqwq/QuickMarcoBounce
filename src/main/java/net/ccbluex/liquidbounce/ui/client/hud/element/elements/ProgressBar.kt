/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarProgress
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarText
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarTitle
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.attack.CooldownHelper.getAttackCooldownProgress
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import java.awt.Color

/**
 * CustomHUD cooldown element
 *
 * Shows simulated attack cooldown
 */
@ElementInfo(name = "ProgressBar")
class ProgressBar(
    x: Double = 0.0, y: Double = -14.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)
) : Element("ProgressBar", x, y, scale, side) {

    private val font by font("Font", Fonts.fontSemibold35)
    private val shadow by boolean("Shadow", true)

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        val progress = progressBarProgress
        val stringWidth = font.getStringWidth(progressBarTitle).toFloat()
        val height = ((font as? GameFontRenderer)?.height ?: font.FONT_HEIGHT).toFloat()

        if (progress < 1.0 && progress > 0.0) {
            font.drawString(progressBarTitle, (-stringWidth / 2).toInt(), (- (height + 5)).toInt(), Color(255, 255, 255).rgb)
            drawRect(-25f, 0f, 25f, 3f, Color(0, 0, 0, 150).rgb)
            drawRect(-25f, 0f, progress * 50 - 25, 3f, Color(0, 111, 255, 200).rgb)
            font.drawString(progressBarText, -25, 6, Color(255, 255, 255).rgb)
        }

        return Border(-25F, 0F, 25F, 3F)
    }
}