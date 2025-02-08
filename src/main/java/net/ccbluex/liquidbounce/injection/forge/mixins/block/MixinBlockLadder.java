/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.ccbluex.liquidbounce.features.module.modules.movement.FastClimb;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockLadder.class)
@SideOnly(Side.CLIENT)
public abstract class MixinBlockLadder extends MixinBlock {

    @Shadow
    @Final
    public static PropertyDirection FACING;

    @ModifyConstant(method = "setBlockBoundsBasedOnState", constant = @Constant(floatValue = 0.125F))
    private float injectAACWallClimb(float constant) {
        FastClimb fastClimb = FastClimb.INSTANCE;

        return fastClimb.handleEvents() && fastClimb.getMode().equals("AAC3.0.0") ? 0.99f : constant;
    }

    @Inject(method = "setBlockBoundsBasedOnState", at = @At("HEAD"), cancellable = true)
    public void setBlockBoundsBasedOnState(IBlockAccess p_setBlockBoundsBasedOnState_1_, BlockPos p_setBlockBoundsBasedOnState_2_, CallbackInfo ci) {
        IBlockState iblockstate = p_setBlockBoundsBasedOnState_1_.getBlockState(p_setBlockBoundsBasedOnState_2_);

        if (iblockstate.getBlock() == (Block)(Object)this) {
            float f = 0.125F;
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_12_2)) {
                f = 0.1875f;
            }

            switch ((EnumFacing)iblockstate.getValue(FACING)) {
                case NORTH:
                    this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
                    break;
                case SOUTH:
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
                    break;
                case WEST:
                    this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
                    break;
                case EAST:
                default:
                    this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
            }

            ci.cancel();
        }
    }
}
