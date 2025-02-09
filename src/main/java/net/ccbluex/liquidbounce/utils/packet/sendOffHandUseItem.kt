package net.ccbluex.liquidbounce.utils.packet

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import de.florianmichael.vialoadingbase.ViaLoadingBase
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos

object sendOffHandUseItem {

    val mc = Minecraft.getMinecraft()

    fun sendOffHandUseItem() {
        if (ViaLoadingBase.getInstance().targetVersion.newerThanOrEqualTo(ProtocolVersion.v1_12_2)) mc.netHandler.addToSendQueue(
            C08PacketPlayerBlockPlacement(BlockPos(-1, -2, -1), 255, null, 0.0f, 0.0f, 0.0f)
        )
    }
}