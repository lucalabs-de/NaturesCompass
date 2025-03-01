package com.chaosthedude.naturescompass.utils;

import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

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

    @Environment(EnvType.CLIENT)
    public static BiomeVegetation getVegetationType(Biome biome) {
        GenerationSettings genSettings = biome.getGenerationSettings();
        int vegetationFeatureIndex = GenerationStep.Feature.VEGETAL_DECORATION.ordinal();

        if (GenerationStep.Feature.VEGETAL_DECORATION.ordinal() >= genSettings.getFeatures().size()) {
            return BiomeVegetation.UNKNOWN;
        }

        RegistryEntryList<PlacedFeature> vegetationFeatures = genSettings.getFeatures().get(vegetationFeatureIndex);

        for (RegistryEntry<PlacedFeature> f : vegetationFeatures) {
            if (f.matchesKey(VegetationPlacedFeatures.TREES_JUNGLE)
                    || f.matchesKey(VegetationPlacedFeatures.TREES_SPARSE_JUNGLE)) {
                return BiomeVegetation.JUNGLE;
            } else if (f.matchesKey(VegetationPlacedFeatures.MUSHROOM_ISLAND_VEGETATION)) {
                return BiomeVegetation.MUSHROOM;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_CHERRY)) {
                return BiomeVegetation.CHERRY;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_TAIGA)
                    || f.matchesKey(VegetationPlacedFeatures.TREES_SNOWY)) {
                return BiomeVegetation.SPRUCE;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_BADLANDS)) {
                return BiomeVegetation.DEAD;
            } else if (f.matchesKey(VegetationPlacedFeatures.BAMBOO)) {
                return BiomeVegetation.BAMBOO;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_SAVANNA)) {
                return BiomeVegetation.ACACIA;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_GROVE)) {
                return BiomeVegetation.DARK_OAK;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_BIRCH)) {
                return BiomeVegetation.BIRCH;
            } else if (f.matchesKey(VegetationPlacedFeatures.TREES_BIRCH_AND_OAK)) {
                return BiomeVegetation.OAK;
            } else if (f.matchesKey(VegetationPlacedFeatures.PATCH_CACTUS_DESERT)) {
                return BiomeVegetation.CACTUS;
            } else {
                return BiomeVegetation.UNKNOWN;
            }
        }

        return BiomeVegetation.UNKNOWN;
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