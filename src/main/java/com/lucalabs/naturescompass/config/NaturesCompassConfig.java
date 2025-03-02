package com.lucalabs.naturescompass.config;

import com.lucalabs.naturescompass.NaturesCompass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NaturesCompassConfig {

    public static int maxSamples = 50000;
    public static int radiusModifier = 2500;
    public static int sampleSpaceModifier = 16;
    public static List<String> biomeBlacklist = new ArrayList<String>();
    public static boolean fixBiomeNames = true;

    private static Path configFilePath;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void load() {
        Reader reader;
        if (getFilePath().toFile().exists()) {
            try {
                reader = Files.newBufferedReader(getFilePath());

                Data data = gson.fromJson(reader, Data.class);

                maxSamples = data.common.maxSamples;
                radiusModifier = data.common.radiusModifier;
                sampleSpaceModifier = data.common.sampleSpaceModifier;
                biomeBlacklist = data.common.biomeBlacklist;

                fixBiomeNames = data.client.fixBiomeNames;

                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        save();
    }

    public static void save() {
        try {
            Writer writer = Files.newBufferedWriter(getFilePath());
            Data data = new Data(
                    new Data.Common(
                            maxSamples,
                            radiusModifier,
                            sampleSpaceModifier,
                            biomeBlacklist),
                    new Data.Client(fixBiomeNames));
            gson.toJson(data, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getFilePath() {
        if (configFilePath == null) {
            configFilePath = FabricLoader.getInstance().getConfigDir().resolve(NaturesCompass.MODID + ".json");
        }
        return configFilePath;
    }

    private static class Data {

        private final Common common;
        private final Client client;

        public Data(Common common, Client client) {
            this.common = common;
            this.client = client;
        }

        private static class Common {
            private final String maxSamplesComment = "The maximum number of samples to be taken when searching for a biome.";
            private final int maxSamples;

            private final String radiusModifierComment = "biomeSize * radiusModifier = maxSearchRadius. Raising this value will increase search accuracy but will potentially make the process more resource .";
            private final int radiusModifier;

            private final String sampleSpaceModifierComment = "biomeSize * sampleSpaceModifier = sampleSpace. Lowering this value will increase search accuracy but will make the process more resource intensive.";
            private final int sampleSpaceModifier;

            private final String biomeBlacklistComment = "A list of biomes that the compass will not be able to search for, specified by resource location. The wildcard character * can be used to match any number of characters, and ? can be used to match one character. Ex (ignore backslashes): [\"minecraft:savanna\", \"minecraft:desert\", \"minecraft:*ocean*\"]";
            private final List<String> biomeBlacklist;

            private Common() {
                maxSamples = 50000;
                radiusModifier = 2500;
                sampleSpaceModifier = 16;
                biomeBlacklist = new ArrayList<String>();
            }

            private Common(int maxSamples, int radiusModifier, int sampleSpaceModifier, List<String> biomeBlacklist) {
                this.maxSamples = maxSamples;
                this.radiusModifier = radiusModifier;
                this.sampleSpaceModifier = sampleSpaceModifier;
                this.biomeBlacklist = biomeBlacklist;
            }
        }

        private static class Client {
            private final String fixBiomeNamesComment = "Fixes biome names by adding missing spaces. Ex: ForestHills becomes Forest Hills";
            private final boolean fixBiomeNames;

            private Client() {
                fixBiomeNames = true;
            }

            private Client(boolean fixBiomeNames) {
                this.fixBiomeNames = fixBiomeNames;
            }
        }
    }

}
