package com.chaosthedude.naturescompass.workers;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.config.NaturesCompassConfig;
import com.chaosthedude.naturescompass.utils.BiomeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;

import java.util.UUID;

public class BiomeSearchWorker implements WorldWorkerManager.IWorker {

    public final int maxRadius;
    private final int sampleSpace;
    private final int maxSamples;
    private final ServerWorld world;
    private final Identifier biomeId;
    private final UUID compassId;
    private final BlockPos startPos;
    private final PlayerEntity player;
    private Direction direction;
    private int samples;
    private int nextLength;

    private int x;
    private int z;
    private int[] yValues;
    private int length;
    private boolean finished;
    private int lastRadiusThreshold;

    public BiomeSearchWorker(ServerWorld world, PlayerEntity player, UUID compassId, Biome biome, BlockPos startPos) {
        this.world = world;
        this.player = player;
        this.compassId = compassId;
        this.startPos = startPos;
        x = startPos.getX();
        z = startPos.getZ();
        yValues = MathHelper.stream(startPos.getY(), world.getBottomY() + 1, world.getTopY(), 64).toArray();
        sampleSpace = NaturesCompassConfig.sampleSpaceModifier * BiomeUtils.getBiomeSize(world);
        maxSamples = NaturesCompassConfig.maxSamples;
        maxRadius = NaturesCompassConfig.radiusModifier * BiomeUtils.getBiomeSize(world);
        nextLength = sampleSpace;
        length = 0;
        samples = 0;
        direction = Direction.UP;
        finished = false;
        biomeId = BiomeUtils.getIdentifierForBiome(world, biome);
        lastRadiusThreshold = 0;
    }

    public void start() {
        NaturesCompass.LOGGER.error("starting biome search worker");
        if (maxRadius > 0 && sampleSpace > 0) {
            NaturesCompass.LOGGER.info("Starting search: " + sampleSpace + " sample space, " + maxSamples + " max samples, " + maxRadius + " max radius");
            WorldWorkerManager.addWorker(this);
        } else {
            fail();
        }
    }

    @Override
    public boolean hasWork() {
        return !finished && getRadius() <= maxRadius && samples <= maxSamples;
    }

    @Override
    public boolean doWork() {
        if (hasWork()) {
            if (direction == Direction.NORTH) {
                z -= sampleSpace;
            } else if (direction == Direction.EAST) {
                x += sampleSpace;
            } else if (direction == Direction.SOUTH) {
                z += sampleSpace;
            } else if (direction == Direction.WEST) {
                x -= sampleSpace;
            }

            int sampleX = BiomeCoords.fromBlock(x);
            int sampleZ = BiomeCoords.fromBlock(z);

            for (int y : yValues) {
                int sampleY = BiomeCoords.fromBlock(y);
                final Biome biomeAtPos = world.getChunkManager().getChunkGenerator().getBiomeSource().getBiome(sampleX, sampleY, sampleZ, world.getChunkManager().getNoiseConfig().getMultiNoiseSampler()).value();
                final Identifier biomeAtPosID = BiomeUtils.getIdentifierForBiome(world, biomeAtPos);
                if (biomeAtPosID != null && biomeAtPosID.equals(biomeId)) {
                    succeed();
                    return false;
                }
            }

            samples++;
            length += sampleSpace;
            if (length >= nextLength) {
                if (direction != Direction.UP) {
                    nextLength += sampleSpace;
                    direction = direction.rotateYClockwise();
                } else {
                    direction = Direction.NORTH;
                }
                length = 0;
            }
            int radius = getRadius();
            if (radius > 500 && radius / 500 > lastRadiusThreshold) {
                lastRadiusThreshold = radius / 500;
            }
        }
        if (hasWork()) {
            return true;
        }
        if (!finished) {
            fail();
        }
        return false;
    }

    private void succeed() {
        NaturesCompass.LOGGER.info("Search succeeded: " + getRadius() + " radius, " + samples + " samples");
        NaturesCompass.NATURES_COMPASS_ITEM.succeed(player, x, z, samples);
        finished = true;
    }

    private void fail() {
        NaturesCompass.LOGGER.info("Search failed: " + getRadius() + " radius, " + samples + " samples");
        NaturesCompass.NATURES_COMPASS_ITEM.fail(roundRadius(getRadius(), 500), samples);
        finished = true;
    }

    public void stop() {
        NaturesCompass.LOGGER.info("Search stopped: " + getRadius() + " radius, " + samples + " samples");
        finished = true;
    }

    private int getRadius() {
        return BiomeUtils.getDistanceToBiome(startPos, x, z);
    }

    private int roundRadius(int radius, int roundTo) {
        return (radius / roundTo) * roundTo;
    }

}
