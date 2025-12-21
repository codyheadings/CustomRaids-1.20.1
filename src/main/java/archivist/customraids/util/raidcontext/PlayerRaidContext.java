package archivist.customraids.util.raidcontext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerRaidContext implements RaidContext {

    private final ServerPlayer player;
    private final Set<ServerPlayer> participants = new HashSet<>();

    public PlayerRaidContext(ServerPlayer player) {
        this.player = player;
        participants.add(player);
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
        return participants;
    }

    @Override
    public void addParticipant(ServerPlayer player) {
        participants.add(player);
    }

    @Override
    public void removeParticipant(ServerPlayer player) {
        participants.remove(player);
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

