package com.lucalabs.naturescompass.events;

import com.lucalabs.naturescompass.NaturesCompass;
import com.lucalabs.naturescompass.utils.BiomeUtils;
import com.lucalabs.naturescompass.utils.ItemUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Optional;

public class ServerEventHandler {

    private static void onDimensionChange(ServerPlayerEntity player, ServerWorld origin, ServerWorld dest) {

        var pois = dest.getPointOfInterestStorage();

//        Optional<BlockPos> nearestPortal = pois.getNearestPosition(
//                t -> t.matchesKey(PointOfInterestTypes.NETHER_PORTAL),
//                player.getBlockPos(),
//                100,
//                PointOfInterestStorage.OccupationStatus.ANY);

        Optional<BlockPos> nearestPortal = Optional.empty();

        for (ItemStack compass : ItemUtils.getNatureCompassesInInventory(player)) {
            Identifier playerDimensionId = player.getWorld().getDimensionKey().getValue();
            Identifier dimensionId = NaturesCompass.NATURES_COMPASS_ITEM.getDimensionID(compass);

            if (dimensionId == null) {
                continue;
            }

            if (BiomeUtils.isIdNether(dimensionId) ^ BiomeUtils.isIdNether(playerDimensionId)) {
                NaturesCompass.NATURES_COMPASS_ITEM.setValid(compass, nearestPortal.isPresent());

                nearestPortal.ifPresent(pos ->
                        NaturesCompass.NATURES_COMPASS_ITEM.setFound(compass, pos.getX(), pos.getZ(), 0, player));
            } else if (!playerDimensionId.equals(dimensionId)) {
                NaturesCompass.NATURES_COMPASS_ITEM.setValid(compass, false);
            } else {
                NaturesCompass.NATURES_COMPASS_ITEM.searchForBiome(
                        player.getServerWorld(),
                        player,
                        compass,
                        NaturesCompass.NATURES_COMPASS_ITEM.getBiomeID(compass),
                        player.getBlockPos());
            }
        }
    }

    public static void initialize() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(ServerEventHandler::onDimensionChange);
    }
}
