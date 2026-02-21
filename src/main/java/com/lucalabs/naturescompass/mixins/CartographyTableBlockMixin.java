package com.lucalabs.naturescompass.mixins;

import com.lucalabs.naturescompass.items.NaturesCompassItem;
import com.lucalabs.naturescompass.network.BiomePacket;
import com.lucalabs.naturescompass.screens.BiomeChoiceScreenHandler;
import com.lucalabs.naturescompass.utils.BiomeUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(CartographyTableBlock.class)
public class CartographyTableBlockMixin {
    @Unique
    private static final Text BIOME_TITLE = Text.translatable("string.naturescompass.biome_table");

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir) {
        if (!world.isClient) {
            if (player.getMainHandStack().getItem() instanceof NaturesCompassItem) {
                NamedScreenHandlerFactory s = new SimpleNamedScreenHandlerFactory((
                        (syncId, playerInventory, p) -> new BiomeChoiceScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))), BIOME_TITLE);
                player.openHandledScreen(s);

                ServerPlayNetworking.send(
                        (ServerPlayerEntity) player,
                        BiomePacket.ID,
                        new BiomePacket(BiomeUtils.getAllowedBiomeEntries((ServerWorld) player.getWorld())));

                cir.setReturnValue(ActionResult.CONSUME);
            }
        }
    }

}
