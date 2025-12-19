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
}