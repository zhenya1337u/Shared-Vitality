package net.sharedvitality.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.sharedvitality.SharedVitalityMod;

public record SelectRolePayload(String roleName) implements CustomPacketPayload {
    public static final Type<SelectRolePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SharedVitalityMod.MOD_ID, "select_role"));

    public static final StreamCodec<ByteBuf, SelectRolePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SelectRolePayload::roleName,
            SelectRolePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
