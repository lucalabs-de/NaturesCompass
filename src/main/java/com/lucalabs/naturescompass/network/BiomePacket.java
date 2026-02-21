package com.lucalabs.naturescompass.network;

import com.lucalabs.naturescompass.NaturesCompass;
import com.lucalabs.naturescompass.screens.BiomeChoiceScreenHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BiomePacket extends PacketByteBuf {

    public static final Identifier ID = new Identifier(NaturesCompass.MODID, "biomedata");

    public BiomePacket(Set<RegistryEntry<Biome>> biomes) {
        super(Unpooled.buffer());
        writeInt(biomes.size());
        for (RegistryEntry<Biome> biome : biomes) {
            writeIdentifier(biome.getKey().orElseThrow().getValue());
        }
    }

    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        if (client.world == null || client.player == null) {
            return;
        }

        int size = buf.readInt();
        List<Biome> biomes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            client.execute(() -> {
                var biomeRegistry = client.world.getRegistryManager().get(RegistryKeys.BIOME);
                Biome biome = biomeRegistry.get(id);

                biomes.add(biome);
            });
        }

        client.execute(() -> {
            if (client.player.currentScreenHandler instanceof BiomeChoiceScreenHandler screenHandler) {
                screenHandler.setAvailableBiomes(biomes);
            }
        });
    }
}
