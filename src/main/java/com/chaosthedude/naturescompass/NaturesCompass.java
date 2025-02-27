package com.chaosthedude.naturescompass;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.naturescompass.screens.BiomeChoiceScreenHandler;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.network.SearchPacket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class NaturesCompass implements ModInitializer {

    public static final String MODID = "naturescompass";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final NaturesCompassItem NATURES_COMPASS_ITEM = new NaturesCompassItem();
    public static final ScreenHandlerType<BiomeChoiceScreenHandler> BIOME_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of("naturescompass", "biome_choice"),
                    new ScreenHandlerType<>(BiomeChoiceScreenHandler::new, FeatureSet.empty()));

    public static List<Identifier> allowedBiomes;
    public static ListMultimap<Identifier, Identifier> dimensionIDsForAllowedBiomeIDs;

    @Override
    public void onInitialize() {
        NaturesCompassConfig.load();

        Registry.register(Registries.ITEM, new Identifier(MODID, "naturescompass"), NATURES_COMPASS_ITEM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(NATURES_COMPASS_ITEM));

        ServerPlayNetworking.registerGlobalReceiver(SearchPacket.ID, SearchPacket::apply);

        allowedBiomes = new ArrayList<Identifier>();
        dimensionIDsForAllowedBiomeIDs = ArrayListMultimap.create();
    }

}
