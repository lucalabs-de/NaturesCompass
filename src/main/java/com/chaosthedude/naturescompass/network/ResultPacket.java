package com.chaosthedude.naturescompass.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ResultPacket extends PacketByteBuf {

    public static final Identifier ID = new Identifier(NaturesCompass.MODID, "result");

    public ResultPacket(UUID compassId, BlockPos biomePos) {
       super(Unpooled.buffer());
       writeUuid(compassId);
       writeBlockPos(biomePos);
    }

    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        final BlockPos pos = buf.readBlockPos();
        final UUID compassId = buf.readUuid();

        client.execute(() -> {

        });

        client.player.currentScreenHandler.getCursorStack();
    }

}
