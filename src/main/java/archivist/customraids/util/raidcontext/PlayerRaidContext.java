package archivist.customraids.util.raidcontext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class PlayerRaidContext implements RaidContext {

    private final ServerPlayer player;

    public PlayerRaidContext(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public ServerLevel level() {
        return player.serverLevel();
    }

    @Override
    public BlockPos getBasePos() {
        BlockPos respawn = player.getRespawnPosition();
        return respawn != null ? respawn : player.blockPosition();
    }

    @Override
    public Collection<ServerPlayer> getParticipants() {
        return List.of(player);
    }

    @Override
    public boolean isValid() {
        return player.isAlive();
    }

    @Override
    public String getDebugName() {
        return "PlayerRaid:" + player.getGameProfile().getName();
    }

    private long raidDay = -1;

    @Override
    public void markAttempted(long day) {
        this.raidDay = day;
    }

    @Override
    public boolean wasAttemptedToday(long day) {
        return this.raidDay == day;
    }
}

