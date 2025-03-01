package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.NaturesCompass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class ItemUtils {
	
	public static boolean verifyNBT(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() != NaturesCompass.NATURES_COMPASS_ITEM) {
			return false;
		} else if (!stack.hasNbt()) {
			stack.setNbt(new NbtCompound());
		}

		return true;
	}

	public static ItemStack getHeldNatureCompass(PlayerEntity player) {
		return getHeldItem(player, NaturesCompass.NATURES_COMPASS_ITEM);
	}

	public static ItemStack getInventoryNatureCompass(PlayerEntity player, UUID compassId) {
		PlayerInventory inv = player.getInventory();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack cur = inv.getStack(i);
			NaturesCompass.LOGGER.error(cur.getItem().getName());
			if (verifyNBT(cur)) {
				if (cur.getNbt().contains("ID") && cur.getNbt().getUuid("ID").equals(compassId)) {
					return cur;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public static ItemStack getHeldItem(PlayerEntity player, Item item) {
		if (!player.getMainHandStack().isEmpty() && player.getMainHandStack().getItem() == item) {
			return player.getMainHandStack();
		} else if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() == item) {
			return player.getOffHandStack();
		}

		return ItemStack.EMPTY;
	}

}
