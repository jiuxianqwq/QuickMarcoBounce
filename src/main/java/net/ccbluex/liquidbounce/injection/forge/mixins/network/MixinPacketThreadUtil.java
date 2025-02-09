package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler;
import net.ccbluex.liquidbounce.utils.client.PPSCounter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(PacketThreadUtil.class)
public class MixinPacketThreadUtil {
/*
屌，注入不明白，我直接重写得了。
    @Inject(method = "checkThreadAndEnqueue",at = @At(value = "INVOKE", target = ""))*/

    /**
     * @author NikoIsNtCat
     * @reason 屌，注入不明白，我直接重写得了。
     */
    @Overwrite
    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> p_checkThreadAndEnqueue_0_, final T p_checkThreadAndEnqueue_1_, IThreadListener p_checkThreadAndEnqueue_2_) throws ThreadQuickExitException {
        if (!p_checkThreadAndEnqueue_2_.isCallingFromMinecraftThread()) {
            p_checkThreadAndEnqueue_2_.addScheduledTask(new Runnable() {
                public void run() {
                    if (Disabler.INSTANCE.getGrimPost() && Disabler.INSTANCE.getState() && p_checkThreadAndEnqueue_1_ == Minecraft.getMinecraft().getNetHandler()){
                        Disabler.INSTANCE.getPostPackets().add((Packet<INetHandlerPlayClient>) p_checkThreadAndEnqueue_0_);
                        return;
                    } else {
                        final PacketEvent event = new PacketEvent(p_checkThreadAndEnqueue_0_, EventState.RECEIVE);
                        EventManager.INSTANCE.call(event);
                        if (event.isCancelled()) {
                            return;
                        }

                        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
                    }
                    p_checkThreadAndEnqueue_0_.processPacket(p_checkThreadAndEnqueue_1_);
                }
            });
            throw ThreadQuickExitException.INSTANCE;
        }
    }
}
