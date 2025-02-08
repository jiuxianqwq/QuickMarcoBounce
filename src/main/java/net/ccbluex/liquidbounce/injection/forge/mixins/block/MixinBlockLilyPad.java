package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLilyPad.class)
@SideOnly(Side.CLIENT)
public abstract class MixinBlockLilyPad extends MixinBlock {



    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    private void getCollisionBoundingBox(World p_getCollisionBoundingBox_1_, BlockPos p_getCollisionBoundingBox_2_, IBlockState p_getCollisionBoundingBox_3_, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_12_2)) {
            cir.setReturnValue(new AxisAlignedBB((double) p_getCollisionBoundingBox_2_.getX() + 0.0625, (double) p_getCollisionBoundingBox_2_.getY(), (double) p_getCollisionBoundingBox_2_.getZ() + this.minZ + 0.0625, (double) p_getCollisionBoundingBox_2_.getX() + 0.9375, (double) p_getCollisionBoundingBox_2_.getY() + 0.09375, (double) p_getCollisionBoundingBox_2_.getZ() + 0.9375));
        } else {
            cir.setReturnValue(new AxisAlignedBB((double) p_getCollisionBoundingBox_2_.getX() + this.minX, (double) p_getCollisionBoundingBox_2_.getY() + this.minY, (double) p_getCollisionBoundingBox_2_.getZ() + this.minZ, (double) p_getCollisionBoundingBox_2_.getX() + this.maxX, (double) p_getCollisionBoundingBox_2_.getY() + this.maxY, (double) p_getCollisionBoundingBox_2_.getZ() + this.maxZ));
        }
    }
}
