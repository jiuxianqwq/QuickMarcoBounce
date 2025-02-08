package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.IOException;

@Mixin(C0FPacketConfirmTransaction.class)
public class MixinC0FPacketConfirmTransaction {

    @Shadow
    private int windowId;

    @Shadow
    private short uid;

    @Shadow
    private boolean accepted;

    @Inject(method = "writePacketData", at = @At("HEAD"), cancellable = true)
    private void onWritePacketData(PacketBuffer buf, CallbackInfo info) throws IOException {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_17)) {
            buf.writeInt(this.windowId);
        } else {
            buf.writeByte(this.windowId);
            buf.writeShort(this.uid);
            buf.writeByte(this.accepted ? 1 : 0);
        }
        info.cancel();
    }
}