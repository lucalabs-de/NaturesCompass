package com.chaosthedude.naturescompass.screens;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class BiomeChoiceScreenHandler extends ScreenHandler {
    final Slot inputSlot;
    final Slot outputSlot;

    private final Inventory input;
    private final CraftingResultInventory output;

    private final World world;
    private final Property selectedBiome;
    private final List<Biome> availableBiomes;
    private boolean areBiomesChoosable;

    Runnable contentsChangedListener;

    public BiomeChoiceScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(NaturesCompass.BIOME_SCREEN_HANDLER, syncId);

        this.world = playerInventory.player.getWorld();
        this.contentsChangedListener = () -> {
        };

        this.selectedBiome = Property.create();
        this.availableBiomes = BiomeUtils.getAllowedBiomes(world);
        this.areBiomesChoosable = false;

        this.output = new CraftingResultInventory();
        this.input = new SimpleInventory(1) {
            public void markDirty() {
                super.markDirty();
                onContentChanged(this);
                contentsChangedListener.run();
            }
        };

        this.inputSlot = this.addSlot(new Slot(this.input, 0, 20, 33));
        this.outputSlot = this.addSlot(new Slot(this.output, 1, 143, 33) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                // TODO start biome search based on item metadata
                ItemStack itemStack = BiomeChoiceScreenHandler.this.inputSlot.takeStack(1);
                if (!itemStack.isEmpty()) {
                    populateResult(player);
                }

                super.onTakeItem(player, stack);
            }
        });

        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addProperty(selectedBiome);
    }

    public void onContentChanged(Inventory inventory) {
        ItemStack itemStack = this.inputSlot.getStack();
        this.areBiomesChoosable = itemStack.getItem() instanceof NaturesCompassItem;
        this.selectedBiome.set(-1);
        this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
    }

    public boolean onButtonClick(PlayerEntity player, int id) {
        if (this.isInBounds(id)) {
            this.selectedBiome.set(id);
            this.populateResult(player);
        }

        return true;
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        if (slot.inventory == this.output) {
            return false;
        }

        if (slot.inventory == this.input) {
           return stack.getItem() instanceof NaturesCompassItem;
        }

        return super.canInsertIntoSlot(stack, slot);
    }

    void populateResult(PlayerEntity player) {
        if (!this.availableBiomes.isEmpty() && this.isInBounds(this.selectedBiome.get())) {
            Biome biome = this.availableBiomes.get(this.selectedBiome.get());
            ItemStack newCompass = inputSlot.getStack().copy();
            NaturesCompass.NATURES_COMPASS_ITEM.setSearching(
                    newCompass,
                    BiomeUtils.getIdentifierForBiome(world, biome),
                    player
            );
            this.outputSlot.setStackNoCallbacks(newCompass);
        } else {
            this.outputSlot.setStackNoCallbacks(ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            Item item = originalStack.getItem();
            newStack = originalStack.copy();
            if (invSlot == 1) { // took output
                if (!this.insertItem(originalStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickTransfer(originalStack, newStack);
            } else if (invSlot == 0) { // took input
                if (!this.insertItem(originalStack, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot >= 2 && invSlot < 29) { // moved inventory item?
                if (!this.insertItem(originalStack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot >= 29 && invSlot < 38 && !this.insertItem(originalStack, 2, 29, false)) {
                // moved hotbar item?
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            }

            slot.markDirty();
            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
            this.sendContentUpdates();
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(ScreenHandlerContext.EMPTY, player, Blocks.CARTOGRAPHY_TABLE);
    }

    public List<Biome> getAvailableBiomes() {
        return this.availableBiomes;
    }

    public int getAvailableBiomesCount() {
        return this.availableBiomes.size();
    }

    public int getSelectedBiome() {
        return this.selectedBiome.get();
    }

    public boolean getAreBiomesChoosable() {
        return this.areBiomesChoosable;
    }

    public void setContentsChangedListener(Runnable contentsChangedListener) {
        this.contentsChangedListener = contentsChangedListener;
    }

    private boolean isInBounds(int id) {
        return id >= 0 && id < this.availableBiomes.size();
    }

    private void searchForBiome() {
//        if (BiomeUtils.getKeyForBiome(level, biome).isPresent()) {
//            NaturesCompass.network.send(new CompassSearchPacket(BiomeUtils.getKeyForBiome(level, biome).get(), player.blockPosition()), PacketDistributor.SERVER.noArg());
//        }
//        minecraft.setScreen(null);
    }
}
