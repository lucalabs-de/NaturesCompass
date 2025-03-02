package com.lucalabs.naturescompass.utils;

import com.lucalabs.naturescompass.config.NaturesCompassConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BiomeUtils {

    public static Registry<Biome> getBiomeRegistry(World world) {
        return world.getRegistryManager().get(RegistryKeys.BIOME);
    }

    public static Identifier getIdentifierForBiome(World world, Biome biome) {
        return getBiomeRegistry(world).getId(biome);
    }

    public static Optional<Biome> getBiomeForIdentifier(World world, Identifier id) {
        return getBiomeRegistry(world).getOrEmpty(id);
    }

    public static List<Biome> getAllowedBiomes(World world) {
        Registry<Biome> biomeRegistry = getBiomeRegistry(world);
        List<Biome> biomes = new ArrayList<>();

        for (RegistryKey<Biome> k : biomeRegistry.getKeys()) {
            Biome biome = biomeRegistry.get(k);
            Identifier biomeId = getIdentifierForBiome(world, biome);
            RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(k).orElseThrow();

            if (!biomeIdIsBlacklisted(world, biomeId) && biomeEntry.isIn(BiomeTags.IS_OVERWORLD)) {
                biomes.add(biome);
            }
        }

        return biomes;
    }

    public static int getBiomeSize(World world) {
        // TODO
        return 4;
    }

    public static int getDistanceToBiome(BlockPos startPos, int biomeX, int biomeZ) {
        return (int) MathHelper.sqrt((float) startPos.getSquaredDistance(new BlockPos(biomeX, startPos.getY(), biomeZ)));
    }

    @Environment(EnvType.CLIENT)
    public static String getBiomeNameForDisplay(World world, Biome biome) {
        if (biome != null) {
            if (NaturesCompassConfig.fixBiomeNames) {
                final String original = getBiomeName(world, biome);
                StringBuilder fixed = new StringBuilder();
                char pre = ' ';
                for (int i = 0; i < original.length(); i++) {
                    final char c = original.charAt(i);
                    if (Character.isUpperCase(c) && Character.isLowerCase(pre) && Character.isAlphabetic(pre)) {
                        fixed.append(" ");
                    }
                    fixed.append(String.valueOf(c));
                    pre = c;
                }

                return fixed.toString();
            }

            if (getIdentifierForBiome(world, biome) != null) {
                return I18n.translate(getIdentifierForBiome(world, biome).toString());
            }
        }

        return "";
    }

    @Environment(EnvType.CLIENT)
    public static String getBiomeName(World world, Biome biome) {
        return I18n.translate(Util.createTranslationKey("biome", getIdentifierForBiome(world, biome)));
    }

    public static boolean biomeIdIsBlacklisted(World world, Identifier biomeID) {
        final List<String> biomeBlacklist = NaturesCompassConfig.biomeBlacklist;
        for (String biomeKey : biomeBlacklist) {
            if (biomeID.toString().matches(convertToRegex(biomeKey))) {
                return true;
            }
        }
        return false;
    }

    private static String convertToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        for (char i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                regex.append(".*");
            } else if (c == '?') {
                regex.append(".");
            } else if (c == '.') {
                regex.append("\\.");
            } else {
                regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }

}