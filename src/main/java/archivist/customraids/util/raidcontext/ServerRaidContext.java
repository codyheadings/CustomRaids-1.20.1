package archivist.customraids.util.raidcontext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerRaidContext implements RaidContext {

    private final ServerLevel level;
    private BlockPos basePos;
    private final Set<ServerPlayer> participants = new HashSet<>();

    public ServerRaidContext(ServerLevel level) {
        this.level = level;
        selectBasePos();
        participants.addAll(level.players());
    }

    private static BlockPos getPlayerRespawnOrPosition(ServerPlayer player) {
        BlockPos respawn = player.getRespawnPosition();
        if (respawn != null) {
            return respawn;
        }

        return player.blockPosition();
    }

    private void selectBasePos() {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            basePos = level.getSharedSpawnPos();
            return;
        }

        List<ServerPlayer> withBeds = players.stream()
                .filter(p -> p.getRespawnPosition() != null)
                .toList();

        ServerPlayer victim = withBeds.isEmpty()
                ? players.get(level.random.nextInt(players.size()))
                : withBeds.get(level.random.nextInt(withBeds.size()));

        basePos = getPlayerRespawnOrPosition(victim);
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
        return !level.players().isEmpty();
    }

    @Override
    public String getDebugName() {
        return "ServerRaid";
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
