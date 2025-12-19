package archivist.customraids.util.raidcontext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ServerRaidContext implements RaidContext {

    private final ServerLevel level;
    private BlockPos basePos;

    public ServerRaidContext(ServerLevel level) {
        this.level = level;
        this.basePos = level.getSharedSpawnPos(); // or cached server base
    }

    @Override
    public ServerLevel level() {
        return level;
    }

    @Override
    public BlockPos getBasePos() {
        return basePos;
    }

    @Override
    public Collection<ServerPlayer> getParticipants() {
        return level.players();
    }

    @Override
    public boolean isValid() {
        return !level.players().isEmpty();
    }

    @Override
    public String getDebugName() {
        return "ServerRaid";
    }
}
