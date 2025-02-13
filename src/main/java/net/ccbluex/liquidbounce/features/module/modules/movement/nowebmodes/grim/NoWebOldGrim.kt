package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.grim

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.loopHandler
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.EnumFacing

/**
 * @author yuchenxue
 * @date 2025/02/13
 */

object NoWebOldGrim : NoWebMode("OldGrim") {

    private val range by int("Range", 2, 2..5)

    private val onUpdate = handler<UpdateEvent>{
        mc.thePlayer.isInWeb = false
    }

    private val onTick = loopHandler {
        BlockUtils.searchBlocks(2, setOf(Blocks.web)).forEach { (pos, _) ->
            sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.DOWN))
            sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
            mc.theWorld.setBlockToAir(pos)
        }
    }
}