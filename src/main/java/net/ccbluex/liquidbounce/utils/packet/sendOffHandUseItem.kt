package net.ccbluex.liquidbounce.utils.packet

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper
import com.viaversion.viaversion.api.type.Types
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9
import net.minecraft.client.Minecraft

object sendOffHandUseItem {

    val mc = Minecraft.getMinecraft()

    fun sendOffHandUseItem() {
        val connection =
            Via.getManager().getConnectionManager().getConnections().stream().findFirst().orElse(null)
        val packet: PacketWrapper = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM, connection)
        packet.write(Types.VAR_INT, 1)
        mc.playerController.syncCurrentPlayItem()
        packet.sendToServer(Protocol1_9To1_8::class.java)
    }
}