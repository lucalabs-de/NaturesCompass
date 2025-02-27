package com.chaosthedude.naturescompass.items;

import com.chaosthedude.naturescompass.utils.BiomeUtils;
import com.chaosthedude.naturescompass.utils.CompassState;
import com.chaosthedude.naturescompass.utils.ItemUtils;
import com.chaosthedude.naturescompass.workers.BiomeSearchWorker;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class NaturesCompassItem extends Item {

    private BiomeSearchWorker worker;

    public NaturesCompassItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext type) {
        Identifier associatedBiomeId = getBiomeID(stack);
        Optional<Biome> associatedBiome = BiomeUtils.getBiomeForIdentifier(world, associatedBiomeId);

        if (associatedBiome.isPresent()) {
            tooltip.add(Text.literal(BiomeUtils.getBiomeNameForDisplay(world, associatedBiome.get()))
                    .formatted(Formatting.GOLD));
        }
    }

    public void searchForBiome(ServerWorld world, PlayerEntity player, Identifier biomeID, BlockPos pos, ItemStack stack) {
        setSearching(stack, biomeID, player);
        Optional<Biome> optionalBiome = BiomeUtils.getBiomeForIdentifier(world, biomeID);
        if (optionalBiome.isPresent()) {
            if (worker != null) {
                worker.stop();
            }
            worker = new BiomeSearchWorker(world, player, stack, optionalBiome.get(), pos);
            worker.start();
        }
    }

    public void succeed(ItemStack stack, PlayerEntity player, int x, int z, int samples, boolean displayCoordinates) {
        setFound(stack, x, z, samples, player);
        worker = null;
    }

    public void fail(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
        setNotFound(stack, player, searchRadius, samples);
        worker = null;
    }

    public boolean isActive(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return getState(stack) != CompassState.INACTIVE;
        }

        return false;
    }

    public void setSearching(ItemStack stack, Identifier biomeID, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putString("BiomeID", biomeID.toString());
            stack.getNbt().putInt("State", CompassState.SEARCHING.getID());
            stack.getNbt().putInt("SearchRadius", 0);
        }
    }

    public void setFound(ItemStack stack, int x, int z, int samples, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("State", CompassState.FOUND.getID());
            stack.getNbt().putInt("FoundX", x);
            stack.getNbt().putInt("FoundZ", z);
            stack.getNbt().putInt("Samples", samples);
        }
    }

    public void setNotFound(ItemStack stack, PlayerEntity player, int searchRadius, int samples) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("State", CompassState.NOT_FOUND.getID());
            stack.getNbt().putInt("SearchRadius", searchRadius);
            stack.getNbt().putInt("Samples", samples);
        }
    }

    public void setInactive(ItemStack stack, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("State", CompassState.INACTIVE.getID());
        }
    }

    public void setState(ItemStack stack, BlockPos pos, CompassState state, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("State", state.getID());
        }
    }

    public void setFoundBiomeZ(ItemStack stack, int z, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("FoundZ", z);
        }
    }

    public void setBiomeID(ItemStack stack, Identifier biomeID, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putString("BiomeID", biomeID.toString());
        }
    }

    public void setSearchRadius(ItemStack stack, int searchRadius, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("SearchRadius", searchRadius);
        }
    }

    public void setSamples(ItemStack stack, int samples, PlayerEntity player) {
        if (ItemUtils.verifyNBT(stack)) {
            stack.getNbt().putInt("Samples", samples);
        }
    }

    public CompassState getState(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return CompassState.fromID(stack.getNbt().getInt("State"));
        }

        return null;
    }

    public int getFoundBiomeX(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return stack.getNbt().getInt("FoundX");
        }

        return 0;
    }

    public int getFoundBiomeZ(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return stack.getNbt().getInt("FoundZ");
        }

        return 0;
    }

    public Identifier getBiomeID(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return new Identifier(stack.getNbt().getString("BiomeID"));
        }

        return new Identifier("");
    }

    public int getSearchRadius(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return stack.getNbt().getInt("SearchRadius");
        }

        return -1;
    }

    public int getSamples(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack)) {
            return stack.getNbt().getInt("Samples");
        }

        return -1;
    }

    public int getDistanceToBiome(PlayerEntity player, ItemStack stack) {
        return BiomeUtils.getDistanceToBiome(player, getFoundBiomeX(stack), getFoundBiomeZ(stack));
    }

    public boolean shouldDisplayCoordinates(ItemStack stack) {
        if (ItemUtils.verifyNBT(stack) && stack.getNbt().contains("DisplayCoordinates")) {
            return stack.getNbt().getBoolean("DisplayCoordinates");
        }

        return true;
    }

}
