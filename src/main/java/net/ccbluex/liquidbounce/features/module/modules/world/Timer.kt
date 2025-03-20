/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.minecraft.network.play.server.S00PacketKeepAlive
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.status.server.S01PacketPong

object Timer : Module("Timer", Category.WORLD, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("OnMove", "NoMove", "Always", "Balance"), "Balance")
    private val speed by float("Speed", 2F, 0.1F..10F)
    var balance = 0
    override fun onDisable() {
        if (mc.thePlayer == null)
            return
        balance = 0
        mc.timer.timerSpeed = 1F
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if(mode != "Balance") {
            if (mode == "Always" || mode == "OnMove" && player.isMoving || mode == "NoMove" && !player.isMoving) {
                mc.timer.timerSpeed = speed
                return@handler
            }
            mc.timer.timerSpeed = 1F
        }
    }

    val onTick = handler<GameTickEvent>{
        val player = mc.thePlayer ?: return@handler

        if(mode == "Balance") {
            if(!player.isMoving){
                mc.timer.timerSpeed = 0.1F
                balance += 5
            }

            if(player.isMoving){
                mc.timer.timerSpeed = speed
                balance--
            }

            if(balance <= 0){
                balance = 0
                mc.timer.timerSpeed = 1F
                this.state = false
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        if (it.worldClient == null)
            state = false
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        var packet = event.packet
        if (!(packet is S00PacketKeepAlive || packet is S02PacketChat || packet is S01PacketPong) && !player.isMoving) {
            event.cancelEvent()
        }
    }
}
