package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(S32PacketConfirmTransaction.class)
public class MixinS32PacketConfirmTransaction {

    @Shadow
    private int windowId;

    @Shadow
    private short actionNumber;

    @Shadow
    private boolean field_148893_c;

    @Inject(method = "readPacketData", at = @At("HEAD"), cancellable = true)
    private void onReadPacketData(PacketBuffer buf, CallbackInfo info) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            this.windowId = buf.readInt();
        } else {
            this.windowId = buf.readUnsignedByte();
            this.actionNumber = buf.readShort();
            this.field_148893_c = buf.readBoolean();
        }
        info.cancel();
    }
    }