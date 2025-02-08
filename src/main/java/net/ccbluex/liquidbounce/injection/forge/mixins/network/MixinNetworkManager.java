/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.event.CompressionReorderEvent;
import de.florianmichael.viamcp.MCPVLBPipeline;
import de.florianmichael.viamcp.ViaMCP;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.ccbluex.liquidbounce.utils.client.PPSCounter;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;

import static net.minecraft.network.NetworkManager.CLIENT_EPOLL_EVENTLOOP;
import static net.minecraft.network.NetworkManager.CLIENT_NIO_EVENTLOOP;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Shadow
    private Channel channel;

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.RECEIVE);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.RECEIVED);
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, CallbackInfo callback) {
        final PacketEvent event = new PacketEvent(packet, EventState.SEND);
        EventManager.INSTANCE.call(event);

        if (event.isCancelled()) {
            callback.cancel();
            return;
        }

        PPSCounter.INSTANCE.registerType(PPSCounter.PacketType.SEND);
    }

    @Inject(method = "createNetworkManagerAndConnect", at = @At("HEAD"), cancellable = true)
    private static void createNetworkManagerAndConnect(InetAddress p_createNetworkManagerAndConnect_0_, int p_createNetworkManagerAndConnect_1_, boolean p_createNetworkManagerAndConnect_2_, CallbackInfoReturnable<NetworkManager> cir) {
        NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);
        Class<?> oclass;
        LazyLoadBase<?> lazyloadbase;

        if (Epoll.isAvailable() && p_createNetworkManagerAndConnect_2_) {
            oclass = EpollSocketChannel.class;
            lazyloadbase = CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadbase = CLIENT_NIO_EVENTLOOP;
        }

        new Bootstrap()
                .group((EventLoopGroup)lazyloadbase.getValue())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel p_initChannel_1_) throws Exception {
                        try {
                            p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, true);
                        } catch (ChannelException var3) { }
                        p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                                .addLast("splitter", new MessageDeserializer2())
                                .addLast("decoder", new MessageDeserializer(EnumPacketDirection.CLIENTBOUND))
                                .addLast("prepender", new MessageSerializer2())
                                .addLast("encoder", new MessageSerializer(EnumPacketDirection.SERVERBOUND))
                                .addLast("packet_handler", networkmanager);
                        if (p_initChannel_1_ instanceof SocketChannel && ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaMCP.NATIVE_VERSION) {
                            UserConnection user = new UserConnectionImpl(p_initChannel_1_, true);
                            new ProtocolPipelineImpl(user);
                            p_initChannel_1_.pipeline().addLast(new MCPVLBPipeline(user));
                        }
                    }
                })
                .channel((Class<? extends Channel>) oclass)
                .connect(p_createNetworkManagerAndConnect_0_, p_createNetworkManagerAndConnect_1_)
                .syncUninterruptibly();

        cir.setReturnValue(networkmanager);
    }

    @Inject(method = "setCompressionTreshold", at = @At("TAIL"))
    private void fireCompression(int p_setCompressionTreshold_1_, CallbackInfo ci) {
        channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());
    }
}

