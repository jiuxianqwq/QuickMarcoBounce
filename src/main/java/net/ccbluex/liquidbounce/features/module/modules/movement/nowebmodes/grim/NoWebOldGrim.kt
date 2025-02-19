package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.grim

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.loopHandler
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.math.abs

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
//        BlockUtils.searchBlocks(2, setOf(Blocks.web))
        searchWeb(range)
            .forEach { (pos, _) ->
                sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.DOWN))
                sendPacket(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
                mc.theWorld.setBlockToAir(pos)
            }
    }

    private fun searchWeb(range: Int): MutableMap<BlockPos, Block> {
        val player = mc.thePlayer ?: return mutableMapOf()
        val pos = player.position
        val blocks = mutableMapOf<BlockPos, Block>()

        for (r in 0..range) {
            for (dx in -r..r) {
                for (dy in -r..r) {
                    for (dz in -r..r) {
                        if (abs(dx) + abs(dy) + abs(dz) > r) {
                            continue
                        }

                        val next = BlockPos(pos.x + dx, pos.y + dy, pos.z + dz)

                        val block = mc.theWorld.getBlockState(next).block

                        if (block == Blocks.web) {
                            blocks[next] = block
                        }
                    }
                }
            }
        }

        return blocks
    }
}