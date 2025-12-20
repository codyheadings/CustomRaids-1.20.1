package archivist.customraids.util.raidcontext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface RaidContext {
    ServerLevel level();

    BlockPos getBasePos();

    Collection<ServerPlayer> getParticipants();

    boolean isValid();

    String getDebugName();

    void markAttempted(long day);

    boolean wasAttemptedToday(long day);

    default boolean hasParticipant(ServerPlayer player) {
        return getParticipants().contains(player);
    }

    default void addParticipant(ServerPlayer player) {
        getParticipants().add(player);
    }

    default void removeParticipant(ServerPlayer player) {
        getParticipants().remove(player);
    }

    default boolean hasLivingParticipants() {
        return getParticipants().stream()
                .anyMatch(p -> !p.isDeadOrDying());
    }
}