package net.ccbluex.liquidbounce.injection.forge.mixins.item;

import de.florianmichael.viamcp.fixes.FixedSoundEngine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlock.class)
public class MixinItemBlock {
    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void onItemUseMixin(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(FixedSoundEngine.onItemUse((ItemBlock) (Object) this, stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ));
    }
}