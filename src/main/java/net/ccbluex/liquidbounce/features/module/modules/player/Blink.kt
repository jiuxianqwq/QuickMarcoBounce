/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarProgress
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarShouldRender
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarText
import net.ccbluex.liquidbounce.ui.client.hud.HUD.progressBarTitle
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import java.util.*
import kotlin.collections.ArrayList

object Blink : Module("Blink", Category.PLAYER) {

    private val packets = LinkedList<ArrayList<Packet<*>>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var ticks = 0

    private val modeValue by choices("Mode", arrayOf("Simple", "Slow Release", "Delay"), "Slow Release")
    private val autoclose by boolean("AutoClose", false)
    private val auraValue by boolean("Aura Support", true)

    override fun onEnable() {
        if (mc.thePlayer == null) return
        synchronized(packets) {
            packets.clear()
            packets.add(ArrayList())
        }
        ticks = 0

        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile).apply {
            clonePlayer(mc.thePlayer, true)
            copyLocationAndAnglesFrom(mc.thePlayer)
            rotationYawHead = mc.thePlayer.rotationYawHead
        }
        mc.theWorld.addEntityToWorld(-1337, fakePlayer)

        progressBarTitle = "Blink"
        progressBarProgress = 0f
    }

    override fun onDisable() {
        synchronized(packets) {
            packets.forEach { sendTick(it)}
            packets.clear()
        }

        progressBarTitle = ""
        progressBarProgress = 0f
        progressBarText = ""

        try {
            fakePlayer?.let { mc.theWorld.removeEntity(it) }
        } catch (e: Exception) {
            // Ignored
        }
    }

    val onWorld = handler<WorldEvent> {
        state = false
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (PacketUtils.isCPacket(packet)) {
            synchronized(packets) {
                mc.addScheduledTask { packets.last.add(packet) }
            }
            event.cancelEvent()
        }
    }

    val onTick = handler<GameTickEvent> {
        ticks++
        packets.add(ArrayList())

        progressBarProgress = ticks.toFloat() / 500f
        progressBarText = "$ticks / 500"

        when (modeValue) {
            "Delay" -> {
                synchronized(packets) {
                    if (packets.size > 100) poll()
                }
            }
            "Slow Release" -> {
                synchronized(packets) {
                    if (packets.size > 100 || ticks % 5 == 0) poll()
                }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer.hurtTime > 0 && autoclose) {
            state = false
//            NotificationManager.post(NotificationType.SUCCESS, "Blink", "AutoClose!", 5.0f)
            hud.addNotification(Notification.informative(this, "AutoClose!", 500L))
        }
    }

    private fun poll() {
        synchronized(packets) {
            if (packets.isEmpty()) return

            sendTick(packets.first)
            packets.removeFirst()
        }
    }

    override val tag: String
        get() = modeValue

    private fun sendTick(tick: ArrayList<Packet<*>>) {
        synchronized(packets) {
            tick.forEach { packet ->
                sendPacket(packet, false)
                handleFakePlayerPacket(packet)
            }
        }
    }

    private fun handleFakePlayerPacket(packet: Packet<*>) {
        when (packet) {
            is C03PacketPlayer.C04PacketPlayerPosition -> {
                fakePlayer?.apply {
                    setPositionAndRotation2(packet.x, packet.y, packet.z, rotationYaw, rotationPitch, 3, true)
                    onGround = packet.isOnGround
                }
            }
            is C03PacketPlayer.C05PacketPlayerLook -> {
                fakePlayer?.apply {
                    setPositionAndRotation2(posX, posY, posZ, packet.getYaw(), packet.getPitch(), 3, true)
                    onGround = packet.isOnGround
                    rotationYawHead = packet.getYaw()
                    rotationYaw = packet.getYaw()
                    rotationPitch = packet.getPitch()
                }
            }
            is C03PacketPlayer.C06PacketPlayerPosLook -> {
                fakePlayer?.apply {
                    setPositionAndRotation2(packet.x, packet.y, packet.z, packet.getYaw(), packet.getPitch(), 3, true)
                    onGround = packet.isOnGround
                    rotationYawHead = packet.getYaw()
                    rotationYaw = packet.getYaw()
                    rotationPitch = packet.getPitch()
                }
            }
            is C0BPacketEntityAction -> {
                fakePlayer?.apply {
                    when (packet.action) {
                        C0BPacketEntityAction.Action.START_SPRINTING -> isSprinting = true
                        C0BPacketEntityAction.Action.STOP_SPRINTING -> isSprinting = false
                        C0BPacketEntityAction.Action.START_SNEAKING -> isSneaking = true
                        C0BPacketEntityAction.Action.STOP_SNEAKING -> isSneaking = false
                        else -> {}
                    }
                }
            }
            is C0APacketAnimation -> {
                fakePlayer?.swingItem()
            }
        }
    }
}