package net.sharedvitality.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.sharedvitality.SharedVitalityMod;

public record SyncPoolPayload(
        float currentHp,
        float maxHp,
        String roleName,
        int warningLevel,
        boolean hasChosenRole,
        int readyCount,
        int totalPlayers,
        String takenRolesJson
) implements CustomPacketPayload {
    public static final Type<SyncPoolPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "sync_pool"));

    public static final StreamCodec<ByteBuf, SyncPoolPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncPoolPayload::currentHp,
            ByteBufCodecs.FLOAT, SyncPoolPayload::maxHp,
            ByteBufCodecs.STRING_UTF8, SyncPoolPayload::roleName,
            ByteBufCodecs.INT, SyncPoolPayload::warningLevel,
            ByteBufCodecs.BOOL, SyncPoolPayload::hasChosenRole,
            ByteBufCodecs.INT, SyncPoolPayload::readyCount,
            ByteBufCodecs.INT, SyncPoolPayload::totalPlayers,
            ByteBufCodecs.STRING_UTF8, SyncPoolPayload::takenRolesJson,
            SyncPoolPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
